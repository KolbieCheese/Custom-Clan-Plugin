package io.github.maste.customclans.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Clan(long id, String name, String tag, String tagColor, UUID presidentUuid, Instant createdAt) {

    public Clan {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(tagColor, "tagColor");
        Objects.requireNonNull(presidentUuid, "presidentUuid");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
