package io.github.maste.customclans.repositories;

import io.github.maste.customclans.models.ClanInvite;
import io.github.maste.customclans.models.InviteAcceptResult;
import io.github.maste.customclans.models.InviteCreateResult;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClanInviteRepository {

    CompletableFuture<Optional<ClanInvite>> findByInvitedPlayerUuid(UUID playerUuid);

    CompletableFuture<InviteCreateResult> createInvite(ClanInvite invite, Instant now);

    CompletableFuture<Void> deleteByInvitedPlayerUuid(UUID playerUuid);

    CompletableFuture<Integer> deleteExpiredInvites(Instant now);

    CompletableFuture<InviteAcceptResult> acceptInvite(UUID playerUuid, String playerName, int maxClanSize, Instant now);
}
