package io.github.maste.customclans.util;

import org.bukkit.plugin.java.JavaPlugin;

public final class SchedulerUtil {

    private SchedulerUtil() {
    }

    public static void runSync(JavaPlugin plugin, Runnable runnable) {
        if (!plugin.isEnabled()) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
}
