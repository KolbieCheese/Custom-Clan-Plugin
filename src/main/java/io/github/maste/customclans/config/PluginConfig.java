package io.github.maste.customclans.config;

import io.github.maste.customclans.util.ValidationUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {

    private static final Map<String, NamedTextColor> NAMED_COLORS = Map.of(
            "red", NamedTextColor.RED,
            "gold", NamedTextColor.GOLD,
            "yellow", NamedTextColor.YELLOW,
            "green", NamedTextColor.GREEN,
            "aqua", NamedTextColor.AQUA,
            "blue", NamedTextColor.BLUE,
            "light_purple", NamedTextColor.LIGHT_PURPLE,
            "white", NamedTextColor.WHITE,
            "gray", NamedTextColor.GRAY
    );

    private final int maxClanNameLength;
    private final int maxClanTagLength;
    private final String defaultClanTagColorName;
    private final NamedTextColor defaultClanTagColor;
    private final Map<String, NamedTextColor> allowedClanColors;
    private final int inviteExpirationSeconds;
    private final int maxClanSize;
    private final String publicChatFormat;
    private final String clanChatFormat;
    private final boolean clanChatEnabled;
    private final boolean clanChatToggleEnabled;
    private final boolean debug;

    private PluginConfig(
            int maxClanNameLength,
            int maxClanTagLength,
            String defaultClanTagColorName,
            NamedTextColor defaultClanTagColor,
            Map<String, NamedTextColor> allowedClanColors,
            int inviteExpirationSeconds,
            int maxClanSize,
            String publicChatFormat,
            String clanChatFormat,
            boolean clanChatEnabled,
            boolean clanChatToggleEnabled,
            boolean debug
    ) {
        this.maxClanNameLength = maxClanNameLength;
        this.maxClanTagLength = maxClanTagLength;
        this.defaultClanTagColorName = defaultClanTagColorName;
        this.defaultClanTagColor = defaultClanTagColor;
        this.allowedClanColors = Map.copyOf(allowedClanColors);
        this.inviteExpirationSeconds = inviteExpirationSeconds;
        this.maxClanSize = maxClanSize;
        this.publicChatFormat = publicChatFormat;
        this.clanChatFormat = clanChatFormat;
        this.clanChatEnabled = clanChatEnabled;
        this.clanChatToggleEnabled = clanChatToggleEnabled;
        this.debug = debug;
    }

    public static PluginConfig load(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        int maxClanNameLength = Math.max(3, config.getInt("max-clan-name-length", 24));
        int maxClanTagLength = Math.max(2, config.getInt("max-clan-tag-length", 6));
        int inviteExpirationSeconds = Math.max(30, config.getInt("invite-expiration-seconds", 300));
        int maxClanSize = Math.max(2, config.getInt("max-clan-size", 20));
        String publicChatFormat = config.getString(
                "public-chat-format",
                "<tag_prefix><white><player_name></white><gray>: </gray><message>"
        );
        String clanChatFormat = config.getString(
                "clan-chat-format",
                "<dark_gray>[Clan]</dark_gray> <tag_prefix><white><player_name></white><gray>: </gray><message>"
        );

        List<String> configuredColors = config.getStringList("allowed-clan-colors");
        if (configuredColors.isEmpty()) {
            configuredColors = List.of("red", "gold", "yellow", "green", "aqua", "blue", "light_purple", "white", "gray");
        }

        Map<String, NamedTextColor> allowedColors = new LinkedHashMap<>();
        for (String configuredColor : configuredColors) {
            String normalized = ValidationUtil.normalizeColor(configuredColor);
            NamedTextColor namedTextColor = NAMED_COLORS.get(normalized);
            if (namedTextColor == null) {
                plugin.getLogger().warning("Ignoring invalid clan tag color in config.yml: " + configuredColor);
                continue;
            }
            allowedColors.put(normalized, namedTextColor);
        }

        if (allowedColors.isEmpty()) {
            allowedColors.put("gold", NamedTextColor.GOLD);
        }

        String defaultColorName = ValidationUtil.normalizeColor(config.getString("default-clan-tag-color", "gold"));
        if (!allowedColors.containsKey(defaultColorName)) {
            plugin.getLogger().warning("Default clan tag color is not allowed. Falling back to the first configured color.");
            defaultColorName = allowedColors.keySet().iterator().next();
        }

        return new PluginConfig(
                maxClanNameLength,
                maxClanTagLength,
                defaultColorName,
                allowedColors.get(defaultColorName),
                allowedColors,
                inviteExpirationSeconds,
                maxClanSize,
                publicChatFormat,
                clanChatFormat,
                config.getBoolean("clan-chat-enabled", true),
                config.getBoolean("clan-chat-toggle-enabled", true),
                config.getBoolean("debug", false)
        );
    }

    public int maxClanNameLength() {
        return maxClanNameLength;
    }

    public int maxClanTagLength() {
        return maxClanTagLength;
    }

    public String defaultClanTagColorName() {
        return defaultClanTagColorName;
    }

    public NamedTextColor defaultClanTagColor() {
        return defaultClanTagColor;
    }

    public int inviteExpirationSeconds() {
        return inviteExpirationSeconds;
    }

    public int maxClanSize() {
        return maxClanSize;
    }

    public String publicChatFormat() {
        return publicChatFormat;
    }

    public String clanChatFormat() {
        return clanChatFormat;
    }

    public boolean clanChatEnabled() {
        return clanChatEnabled;
    }

    public boolean clanChatToggleEnabled() {
        return clanChatToggleEnabled;
    }

    public boolean debug() {
        return debug;
    }

    public boolean isAllowedColor(String colorName) {
        return allowedClanColors.containsKey(ValidationUtil.normalizeColor(colorName));
    }

    public NamedTextColor resolveColor(String colorName) {
        return allowedClanColors.get(ValidationUtil.normalizeColor(colorName));
    }

    public Set<String> allowedColorNames() {
        return allowedClanColors.keySet();
    }

    public List<String> allowedColorNameList() {
        return new ArrayList<>(allowedClanColors.keySet());
    }

    public String normalizeAllowedColorName(String colorName) {
        String normalized = ValidationUtil.normalizeColor(colorName);
        return allowedClanColors.containsKey(normalized) ? normalized : "";
    }

    public String formatColorDisplayName(String colorName) {
        return ValidationUtil.normalizeColor(colorName).replace('_', ' ');
    }
}
