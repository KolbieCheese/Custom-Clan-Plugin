package io.github.maste.customclans.models;

import java.util.List;
import java.util.Objects;

public record ClanInfo(Clan clan, String presidentName, List<ClanMember> members) {

    public ClanInfo {
        Objects.requireNonNull(clan, "clan");
        Objects.requireNonNull(presidentName, "presidentName");
        members = List.copyOf(members);
    }
}
