package io.github.maste.customclans.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ClanMember(long clanId, UUID playerUuid, String lastKnownName, ClanRole role, Instant joinedAt) {

    public ClanMember {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(lastKnownName, "lastKnownName");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(joinedAt, "joinedAt");
    }
}
