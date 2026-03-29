package io.github.maste.customclans.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationUtilTest {

    @Test
    void validatesClanNamesAndTags() {
        assertTrue(ValidationUtil.isValidClanName("Crimson Knights", 24));
        assertTrue(ValidationUtil.isValidClanTag("CK", 6));

        assertFalse(ValidationUtil.isValidClanName("Crimson@Knights", 24));
        assertFalse(ValidationUtil.isValidClanName("This Clan Name Is Way Too Long For MVP", 24));
        assertFalse(ValidationUtil.isValidClanTag("C-K", 6));
        assertFalse(ValidationUtil.isValidClanTag("TOOLONG", 4));
    }

    @Test
    void derivesDefaultTagsFromInitialsAndFallbackCharacters() {
        assertEquals("CK", ValidationUtil.deriveDefaultTag("Crimson Knights", 6));
        assertEquals("ABC", ValidationUtil.deriveDefaultTag("Alpha Beta Core", 3));
        assertEquals("SOLO", ValidationUtil.deriveDefaultTag("Solo", 6));
    }
}
