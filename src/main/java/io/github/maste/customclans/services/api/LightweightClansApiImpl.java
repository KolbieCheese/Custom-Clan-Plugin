package io.github.maste.customclans.services.api;

import io.github.maste.customclans.api.ClanBannerSnapshot;
import io.github.maste.customclans.api.ClanMemberSnapshot;
import io.github.maste.customclans.api.ClanSnapshot;
import io.github.maste.customclans.api.LightweightClansApi;
import io.github.maste.customclans.models.Clan;
import io.github.maste.customclans.models.ClanBannerData;
import io.github.maste.customclans.models.ClanMember;
import io.github.maste.customclans.repositories.ClanMemberRepository;
import io.github.maste.customclans.repositories.ClanRepository;
import io.github.maste.customclans.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class LightweightClansApiImpl implements LightweightClansApi {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository clanMemberRepository;

    public LightweightClansApiImpl(ClanRepository clanRepository, ClanMemberRepository clanMemberRepository) {
        this.clanRepository = clanRepository;
        this.clanMemberRepository = clanMemberRepository;
    }

    @Override
    public Optional<ClanSnapshot> getClanById(long clanId) {
        return clanRepository.findById(clanId).join().map(this::mapClanSnapshot);
    }

    @Override
    public Optional<ClanSnapshot> getClanByName(String name) {
        return clanRepository.findByName(name).join().map(this::mapClanSnapshot);
    }

    @Override
    public Optional<ClanSnapshot> getClanByNormalizedName(String normalizedName) {
        return clanRepository.findByNormalizedName(normalizedName).join().map(this::mapClanSnapshot);
    }

    @Override
    public List<ClanSnapshot> getAllClans() {
        return clanRepository.findAll().join().stream().map(this::mapClanSnapshot).toList();
    }

    @Override
    public List<ClanMemberSnapshot> getMembersForClan(long clanId) {
        return clanMemberRepository.findByClanId(clanId).join().stream().map(this::mapClanMemberSnapshot).toList();
    }

    @Override
    public Optional<ClanBannerSnapshot> getBannerForClan(long clanId) {
        return clanRepository.findClanBanner(clanId).join().map(data -> mapClanBannerSnapshot(clanId, data));
    }

    @Override
    public Optional<ClanSnapshot> getClanForPlayer(UUID playerUuid) {
        return clanMemberRepository.findByPlayerUuid(playerUuid).join()
                .flatMap(member -> clanRepository.findById(member.clanId()).join())
                .map(this::mapClanSnapshot);
    }

    private ClanSnapshot mapClanSnapshot(Clan clan) {
        return new ClanSnapshot(
                clan.id(),
                clan.name(),
                ValidationUtil.normalizeClanName(clan.name()),
                clan.tag(),
                clan.tagColor(),
                clan.description(),
                clan.presidentUuid(),
                clan.createdAt()
        );
    }

    private ClanMemberSnapshot mapClanMemberSnapshot(ClanMember member) {
        return new ClanMemberSnapshot(
                member.clanId(),
                member.playerUuid(),
                member.lastKnownName(),
                member.role(),
                member.joinedAt()
        );
    }

    private ClanBannerSnapshot mapClanBannerSnapshot(long clanId, ClanBannerData data) {
        List<ClanBannerSnapshot.PatternSnapshot> patternSnapshots = data.patterns().stream()
                .map(pattern -> new ClanBannerSnapshot.PatternSnapshot(
                        pattern.pattern().name(),
                        pattern.color().name()
                ))
                .toList();

        return new ClanBannerSnapshot(clanId, data.material().name(), patternSnapshots);
    }
}
