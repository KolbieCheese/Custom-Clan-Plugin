package io.github.maste.customclans.util;

import java.util.Collections;
import java.util.Map;

public record ActionResult<T>(boolean success, String messageKey, Map<String, String> placeholders, T value) {

    public ActionResult {
        placeholders = placeholders == null ? Map.of() : Collections.unmodifiableMap(placeholders);
    }

    public static <T> ActionResult<T> success(String messageKey, T value) {
        return new ActionResult<>(true, messageKey, Map.of(), value);
    }

    public static <T> ActionResult<T> success(String messageKey, Map<String, String> placeholders, T value) {
        return new ActionResult<>(true, messageKey, placeholders, value);
    }

    public static <T> ActionResult<T> failure(String messageKey) {
        return new ActionResult<>(false, messageKey, Map.of(), null);
    }

    public static <T> ActionResult<T> failure(String messageKey, Map<String, String> placeholders) {
        return new ActionResult<>(false, messageKey, placeholders, null);
    }
}
