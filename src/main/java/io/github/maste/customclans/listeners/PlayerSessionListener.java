package io.github.maste.customclans.listeners;

import io.github.maste.customclans.services.ChatService;
import io.github.maste.customclans.services.ClanService;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerSessionListener implements Listener {

    private final JavaPlugin plugin;
    private final ClanService clanService;
    private final ChatService chatService;

    public PlayerSessionListener(JavaPlugin plugin, ClanService clanService, ChatService chatService) {
        this.plugin = plugin;
        this.clanService = clanService;
        this.chatService = chatService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        clanService.touchPlayerName(event.getPlayer()).exceptionally(throwable -> {
            plugin.getLogger().log(Level.WARNING, "Failed to update clan member name on join", throwable);
            return null;
        });
        chatService.refreshSnapshot(event.getPlayer().getUniqueId()).exceptionally(throwable -> {
            plugin.getLogger().log(Level.WARNING, "Failed to refresh clan snapshot on join", throwable);
            return null;
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        chatService.clearPlayerState(event.getPlayer().getUniqueId());
    }
}
