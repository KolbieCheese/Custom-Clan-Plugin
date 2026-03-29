package io.github.maste.customclans.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ClanInvite(long clanId, UUID invitedPlayerUuid, UUID invitedByUuid, Instant expiresAt) {

    public ClanInvite {
        Objects.requireNonNull(invitedPlayerUuid, "invitedPlayerUuid");
        Objects.requireNonNull(invitedByUuid, "invitedByUuid");
        Objects.requireNonNull(expiresAt, "expiresAt");
    }
}
