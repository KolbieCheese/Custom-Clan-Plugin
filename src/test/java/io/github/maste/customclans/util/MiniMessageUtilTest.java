package io.github.maste.customclans.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

class MiniMessageUtilTest {

    @Test
    void rendersClanTagAsLiteralComponent() {
        Component rendered = MiniMessageUtil.renderChatLine(
                MiniMessage.miniMessage(),
                "<tag_prefix><player_name><gray>: </gray><message>",
                MiniMessageUtil.clanTagPrefix("CK<red>", NamedTextColor.RED),
                "Alice",
                Component.text("hello")
        );

        assertEquals(
                "[CK<red>] Alice: hello",
                PlainTextComponentSerializer.plainText().serialize(rendered)
        );
    }
}
