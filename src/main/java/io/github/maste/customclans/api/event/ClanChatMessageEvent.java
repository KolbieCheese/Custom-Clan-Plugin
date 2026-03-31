package io.github.maste.customclans.api.event;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired before Lightweight Clans broadcasts a clan chat message to online clan members.
 *
 * <p>This public integration event is fired for both direct {@code /clan chat <message>} usage and
 * clan-chat toggle messages rerouted from {@code AsyncChatEvent}. The event is always fired on the
 * main server thread so external plugins can inspect or cancel clan chat delivery safely. Unlike
 * lifecycle events, this event is about message delivery and is not tied to persistence completion.
 */
public final class ClanChatMessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player sender;
    private final UUID senderUuid;
    private final String clanName;
    private final String clanTag;
    private final String plainMessage;
    private final Component messageComponent;
    private final boolean toggleRouted;
    private final List<UUID> recipientUuids;
    private boolean cancelled;

    /**
     * Creates a new clan chat integration event.
     *
     * @param sender the player who sent the clan chat message
     * @param clanName the sender's clan display name
     * @param clanTag the sender's clan tag
     * @param plainMessage the trimmed plain-text form of the message
     * @param messageComponent the Adventure component inserted into the clan chat format
     * @param toggleRouted whether the message came from clan-chat toggle rerouting
     * @param recipientUuids the immutable UUID list of online recipients selected for delivery
     */
    public ClanChatMessageEvent(
            Player sender,
            String clanName,
            String clanTag,
            String plainMessage,
            Component messageComponent,
            boolean toggleRouted,
            List<UUID> recipientUuids
    ) {
        super(false);
        this.sender = Objects.requireNonNull(sender, "sender");
        this.senderUuid = sender.getUniqueId();
        this.clanName = Objects.requireNonNull(clanName, "clanName");
        this.clanTag = Objects.requireNonNull(clanTag, "clanTag");
        this.plainMessage = Objects.requireNonNull(plainMessage, "plainMessage");
        this.messageComponent = Objects.requireNonNull(messageComponent, "messageComponent");
        this.toggleRouted = toggleRouted;
        this.recipientUuids = List.copyOf(Objects.requireNonNull(recipientUuids, "recipientUuids"));
    }

    public Player getSender() {
        return sender;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getClanName() {
        return clanName;
    }

    public String getClanTag() {
        return clanTag;
    }

    public String getPlainMessage() {
        return plainMessage;
    }

    public Component getMessageComponent() {
        return messageComponent;
    }

    public boolean isToggleRouted() {
        return toggleRouted;
    }

    public List<UUID> getRecipientUuids() {
        return recipientUuids;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
