package io.github.maste.customclans.models;

public record InviteAcceptResult(Status status, Clan clan) {

    public enum Status {
        ACCEPTED,
        NO_INVITE,
        EXPIRED,
        CLAN_MISSING,
        ALREADY_IN_CLAN,
        CLAN_FULL
    }
}
