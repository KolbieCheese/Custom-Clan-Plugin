package io.github.maste.customclans.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageManager {

    private static final Pattern TAG_PATTERN = Pattern.compile("<([^<>]+)>");
    private static final Pattern HEX_COLOR_TAG_PATTERN = Pattern.compile("#[0-9a-f]{6}");
    private static final Set<String> FORMATTING_TAGS = Set.of(
            "black",
            "dark_blue",
            "dark_green",
            "dark_aqua",
            "dark_red",
            "dark_purple",
            "gold",
            "gray",
            "dark_gray",
            "blue",
            "green",
            "aqua",
            "red",
            "light_purple",
            "yellow",
            "white",
            "obfuscated",
            "bold",
            "strikethrough",
            "underlined",
            "italic",
            "reset",
            "newline"
    );

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration configuration;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        reload();
    }

    public void reload() {
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        YamlConfiguration loadedConfiguration = YamlConfiguration.loadConfiguration(messageFile);
        try (InputStream defaultsStream = plugin.getResource("messages.yml")) {
            if (defaultsStream != null) {
                YamlConfiguration defaultConfiguration = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultsStream, StandardCharsets.UTF_8)
                );
                loadedConfiguration.setDefaults(defaultConfiguration);
                loadedConfiguration.options().copyDefaults(true);
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to load default messages.yml from plugin resources: " + exception.getMessage());
        }

        this.configuration = loadedConfiguration;
        reconcileConfiguredLists();
    }

    public MiniMessage miniMessage() {
        return miniMessage;
    }

    public String raw(String path) {
        String message = configuration.getString(path);
        if (message == null && configuration.getDefaults() != null) {
            message = configuration.getDefaults().getString(path);
        }
        return message != null ? message : "<red>Missing message: " + path + "</red>";
    }

    public Component component(String path, TagResolver... extraResolvers) {
        return miniMessage.deserialize(raw(path), withPrefix(extraResolvers));
    }

    public List<Component> componentList(String path, TagResolver... extraResolvers) {
        List<String> rawList = configuration.getStringList(path);
        if (rawList.isEmpty() && !configuration.contains(path, true) && configuration.getDefaults() != null) {
            rawList = configuration.getDefaults().getStringList(path);
        }
        List<Component> components = new ArrayList<>(rawList.size());
        TagResolver resolver = withPrefix(extraResolvers);
        for (String line : rawList) {
            components.add(miniMessage.deserialize(line, resolver));
        }
        return components;
    }

    public void send(CommandSender sender, String path, TagResolver... extraResolvers) {
        sender.sendMessage(component(path, extraResolvers));
    }

    public void sendList(CommandSender sender, String path, TagResolver... extraResolvers) {
        for (Component component : componentList(path, extraResolvers)) {
            sender.sendMessage(component);
        }
    }

    private TagResolver withPrefix(TagResolver... extraResolvers) {
        List<TagResolver> resolvers = new ArrayList<>();
        resolvers.add(Placeholder.parsed("prefix", raw("general.prefix")));
        for (TagResolver extraResolver : extraResolvers) {
            resolvers.add(extraResolver);
        }
        return TagResolver.resolver(resolvers);
    }

    private void reconcileConfiguredLists() {
        if (configuration.getDefaults() == null) {
            return;
        }

        for (String path : configuration.getDefaults().getKeys(true)) {
            if (!configuration.getDefaults().isList(path) || !configuration.contains(path, true)) {
                continue;
            }

            List<String> defaultLines = configuration.getDefaults().getStringList(path);
            if (defaultLines.isEmpty()) {
                continue;
            }

            List<String> configuredLines = configuration.getStringList(path);
            List<String> reconciledLines = reconcileList(defaultLines, configuredLines);
            if (!configuredLines.equals(reconciledLines)) {
                configuration.set(path, reconciledLines);
                plugin.getLogger().info("Updated outdated message list '" + path + "' from bundled defaults.");
            }
        }
    }

    private List<String> reconcileList(List<String> defaultLines, List<String> configuredLines) {
        List<String> reconciledLines = new ArrayList<>(defaultLines.size() + configuredLines.size());
        Set<Integer> matchedConfiguredIndexes = new HashSet<>();

        for (String defaultLine : defaultLines) {
            int matchingConfiguredIndex = findMatchingConfiguredLine(configuredLines, matchedConfiguredIndexes, defaultLine);
            if (matchingConfiguredIndex >= 0) {
                matchedConfiguredIndexes.add(matchingConfiguredIndex);
                reconciledLines.add(configuredLines.get(matchingConfiguredIndex));
                continue;
            }
            reconciledLines.add(defaultLine);
        }

        for (int index = 0; index < configuredLines.size(); index++) {
            if (!matchedConfiguredIndexes.contains(index)) {
                reconciledLines.add(configuredLines.get(index));
            }
        }

        return reconciledLines;
    }

    private int findMatchingConfiguredLine(List<String> configuredLines, Set<Integer> matchedConfiguredIndexes, String defaultLine) {
        String defaultSignature = lineSignature(defaultLine);
        for (int index = 0; index < configuredLines.size(); index++) {
            if (matchedConfiguredIndexes.contains(index)) {
                continue;
            }
            if (defaultSignature.equals(lineSignature(configuredLines.get(index)))) {
                return index;
            }
        }
        return -1;
    }

    private String lineSignature(String line) {
        StringBuilder signature = new StringBuilder();
        Matcher matcher = TAG_PATTERN.matcher(line);
        int currentIndex = 0;
        while (matcher.find()) {
            signature.append(line, currentIndex, matcher.start());
            String token = matcher.group(1).trim().toLowerCase(Locale.ROOT);
            if (isPlaceholderToken(token)) {
                signature.append('<').append(token).append('>');
            }
            currentIndex = matcher.end();
        }
        signature.append(line.substring(currentIndex));
        return normalizeVisibleText(signature.toString());
    }

    private boolean isPlaceholderToken(String token) {
        return !token.isEmpty()
                && !token.startsWith("/")
                && !token.contains(":")
                && !FORMATTING_TAGS.contains(token)
                && !HEX_COLOR_TAG_PATTERN.matcher(token).matches();
    }

    private String normalizeVisibleText(String line) {
        return line.replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
