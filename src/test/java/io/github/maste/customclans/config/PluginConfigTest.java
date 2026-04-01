package io.github.maste.customclans.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class PluginConfigTest {

    @Test
    void defaultsClanNameLengthToThirtyAndKeepsTagLengthCappedAtFour() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("max-clan-tag-length", 6);
        yaml.set("default-clan-tag-color", "white");
        when(plugin.getConfig()).thenReturn(yaml);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("PluginConfigTest"));

        PluginConfig config = PluginConfig.load(plugin);

        assertEquals(30, config.maxClanNameLength());
        assertEquals(4, config.maxClanTagLength());
    }
}
