package io.github.maste.customclans.api;

import java.util.List;
import java.util.Objects;

public record ClanBannerSnapshot(
        long clanId,
        String material,
        List<PatternSnapshot> patterns
) {

    public ClanBannerSnapshot {
        Objects.requireNonNull(material, "material");
        patterns = List.copyOf(Objects.requireNonNull(patterns, "patterns"));
    }

    public record PatternSnapshot(String pattern, String color) {

        public PatternSnapshot {
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(color, "color");
        }
    }
}
