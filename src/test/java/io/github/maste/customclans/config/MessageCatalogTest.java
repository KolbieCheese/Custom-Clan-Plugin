package io.github.maste.customclans.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class MessageCatalogTest {

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"([A-Za-z0-9_.-]+)\"");
    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile(
            "^(errors|usage|common|validation|help|create|accept|deny|leave|lookup|info|members|chat|invite|rename|description|tag|color|kick|transfer|disband|list|reload)\\."
    );

    @Test
    void allReferencedMessageKeysExistInBundledMessagesFile() throws Exception {
        YamlConfiguration messages = loadBundledMessages();
        Set<String> referencedKeys = collectReferencedMessageKeys();

        for (String key : referencedKeys) {
            assertTrue(messages.contains(key), "Missing bundled message key: " + key);
        }
    }

    private YamlConfiguration loadBundledMessages() {
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("messages.yml"),
                StandardCharsets.UTF_8
        )) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load bundled messages.yml", exception);
        }
    }

    private Set<String> collectReferencedMessageKeys() throws Exception {
        Set<String> keys = new LinkedHashSet<>();
        try (Stream<Path> paths = Files.walk(Path.of("src", "main", "java"))) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
                String content = Files.readString(path);
                Matcher matcher = STRING_LITERAL_PATTERN.matcher(content);
                while (matcher.find()) {
                    String candidate = matcher.group(1);
                    if (MESSAGE_KEY_PATTERN.matcher(candidate).find()) {
                        keys.add(candidate);
                    }
                }
            }
        }
        return keys;
    }
}
