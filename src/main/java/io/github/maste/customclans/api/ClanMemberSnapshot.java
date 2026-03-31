package io.github.maste.customclans.api;

import io.github.maste.customclans.models.ClanRole;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ClanMemberSnapshot(
        long clanId,
        UUID playerUuid,
        String lastKnownName,
        ClanRole role,
        Instant joinedAt
) {

    public ClanMemberSnapshot {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(lastKnownName, "lastKnownName");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(joinedAt, "joinedAt");
    }
}
