package io.github.maste.customclans.models;

public record ClanCreateResult(Status status, Clan clan) {

    public enum Status {
        CREATED,
        ALREADY_IN_CLAN,
        NAME_TAKEN
    }
}
