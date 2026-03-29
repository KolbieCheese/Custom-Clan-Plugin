package io.github.maste.customclans.repositories;

import io.github.maste.customclans.models.ClanMember;
import io.github.maste.customclans.models.PlayerClanSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClanMemberRepository {

    CompletableFuture<Optional<ClanMember>> findByPlayerUuid(UUID playerUuid);

    CompletableFuture<Optional<PlayerClanSnapshot>> findSnapshotByPlayerUuid(UUID playerUuid);

    CompletableFuture<List<ClanMember>> findByClanId(long clanId);

    CompletableFuture<Integer> countByClanId(long clanId);

    CompletableFuture<Boolean> removeMember(long clanId, UUID playerUuid);

    CompletableFuture<Void> updateLastKnownName(UUID playerUuid, String lastKnownName);
}
