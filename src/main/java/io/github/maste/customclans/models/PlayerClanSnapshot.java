package io.github.maste.customclans.models;

import java.util.Objects;
import java.util.UUID;

public record PlayerClanSnapshot(
        long clanId,
        String clanName,
        String tag,
        String tagColor,
        ClanRole role,
        UUID presidentUuid
) {

    public PlayerClanSnapshot {
        Objects.requireNonNull(clanName, "clanName");
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(tagColor, "tagColor");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(presidentUuid, "presidentUuid");
    }
}
