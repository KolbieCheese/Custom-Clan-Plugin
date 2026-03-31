package io.github.maste.customclans.api.event;

import io.github.maste.customclans.api.model.ClanSnapshot;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after a clan banner has been created, changed, or removed.
 *
 * <p>This event is dispatched on the main server thread only after persistence completes, so banner
 * changes and the updated clan state are durable and available through the public API.
 */
public final class ClanBannerUpdatedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ClanSnapshot before;
    private final ClanSnapshot after;

    public ClanBannerUpdatedEvent(ClanSnapshot before, ClanSnapshot after) {
        super(false);
        this.before = Objects.requireNonNull(before, "before");
        this.after = Objects.requireNonNull(after, "after");
    }

    public ClanSnapshot getBefore() {
        return before;
    }

    public ClanSnapshot getAfter() {
        return after;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
