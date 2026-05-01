package com.io.storiosmod.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ChatHeadMarker {

    public static final String MARKER_KEY = "storios_head:";

    public static MutableComponent create(UUID playerUuid) {
        MutableComponent marker = Component.literal("  ");
        Style style = Style.EMPTY.withInsertion(MARKER_KEY + playerUuid.toString());
        return marker.withStyle(style);
    }

    public static UUID extractFromSequence(FormattedCharSequence sequence) {
        AtomicReference<UUID> result = new AtomicReference<>(null);
        sequence.accept((index, style, codepoint) -> {
            String insertion = style.getInsertion();
            if (insertion != null && insertion.startsWith(MARKER_KEY)) {
                try {
                    result.set(UUID.fromString(insertion.substring(MARKER_KEY.length())));
                } catch (IllegalArgumentException ignored) {
                }
                return false;
            }
            return true;
        });
        return result.get();
    }

    public static MutableComponent stripMarker(Component component) {
        MutableComponent result = Component.empty();
        component.visit((Style style, String text) -> {
            String insertion = style.getInsertion();
            if (insertion == null || !insertion.startsWith(MARKER_KEY)) {
                result.append(Component.literal(text).withStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }
}

