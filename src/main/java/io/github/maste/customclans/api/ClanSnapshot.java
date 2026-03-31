package io.github.maste.customclans.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ClanSnapshot(
        long id,
        String name,
        String normalizedName,
        String tag,
        String tagColor,
        String description,
        UUID presidentUuid,
        Instant createdAt
) {

    public ClanSnapshot {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(normalizedName, "normalizedName");
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(tagColor, "tagColor");
        description = description == null ? "" : description;
        Objects.requireNonNull(presidentUuid, "presidentUuid");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public boolean isPresident(UUID playerUuid) {
        return presidentUuid.equals(playerUuid);
    }
}
