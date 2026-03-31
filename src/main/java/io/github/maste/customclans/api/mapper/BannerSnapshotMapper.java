package io.github.maste.customclans.api.mapper;

import io.github.maste.customclans.api.model.BannerPatternSnapshot;
import io.github.maste.customclans.api.model.ClanBannerSnapshot;
import io.github.maste.customclans.models.ClanBannerData;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;

public final class BannerSnapshotMapper {

    public ClanBannerSnapshot map(ClanBannerData data) {
        if (data == null) {
            return null;
        }

        List<BannerPatternSnapshot> patternSnapshots = data.patterns().stream()
                .map(pattern -> new BannerPatternSnapshot(
                        normalizePatternId(pattern.patternId()),
                        pattern.color().name().toLowerCase(Locale.ROOT)
                ))
                .toList();

        return new ClanBannerSnapshot(
                toMaterialId(data.material()),
                deriveBaseColor(data.material()),
                patternSnapshots
        );
    }

    private String deriveBaseColor(Material material) {
        String materialId = toMaterialId(material);
        String materialName = materialId.startsWith("minecraft:")
                ? materialId.substring("minecraft:".length())
                : materialId;
        String suffix = "_BANNER";
        if (!materialName.toUpperCase(Locale.ROOT).endsWith(suffix)) {
            return null;
        }

        String colorToken = materialName.substring(0, materialName.length() - suffix.length());
        if (colorToken.isBlank()) {
            return null;
        }

        return colorToken.toLowerCase(Locale.ROOT);
    }

    private String normalizePatternId(String patternId) {
        String normalized = patternId.toLowerCase(Locale.ROOT);
        return normalized.contains(":") ? normalized : "minecraft:" + normalized;
    }

    private String toMaterialId(Material material) {
        try {
            java.lang.reflect.Method getKeyMethod = Material.class.getMethod("getKey");
            Object key = getKeyMethod.invoke(material);
            if (key != null) {
                java.lang.reflect.Method asStringMethod = key.getClass().getMethod("asString");
                Object result = asStringMethod.invoke(key);
                if (result instanceof String value && !value.isBlank()) {
                    return value.toLowerCase(Locale.ROOT);
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return "minecraft:" + material.toString().toLowerCase(Locale.ROOT);
    }
}
