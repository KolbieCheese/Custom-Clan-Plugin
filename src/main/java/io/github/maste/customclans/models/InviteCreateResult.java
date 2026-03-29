package io.github.maste.customclans.models;

public record InviteCreateResult(Status status, ClanInvite existingInvite) {

    public enum Status {
        CREATED,
        ACTIVE_INVITE_EXISTS,
        DUPLICATE_FROM_SAME_CLAN
    }
}
