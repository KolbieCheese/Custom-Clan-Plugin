package io.github.maste.customclans.services;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Dispatches public plugin events on the main thread.
 *
 * <p>Service-layer lifecycle operations should call this dispatcher only after persistence work has
 * completed so listeners observe durable state.
 */
public final class ServiceEventDispatcher {

    private final JavaPlugin plugin;

    public ServiceEventDispatcher(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> dispatch(Event event) {
        if (!plugin.isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        if (Bukkit.isPrimaryThread()) {
            plugin.getServer().getPluginManager().callEvent(event);
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                plugin.getServer().getPluginManager().callEvent(event);
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }
}
