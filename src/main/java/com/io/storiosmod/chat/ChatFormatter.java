package com.io.storiosmod.chat;

import net.minecraft.network.chat.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatter {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://[-a-zA-Z0-9@:%._+~#=]+\\.[^ ]+)");

    private static final char FORMAT_CHAR = '&';

    public static Component format(String text) {
        ChatStyleConfig config = ChatStyleConfig.get();

        if (config.enableClickableLinks) {
            return formatWithLinks(text, config.enableFormatCodes);
        }

        if (config.enableFormatCodes) {
            return parseFormatCodes(text);
        }

        return Component.literal(text);
    }

    private static Component formatWithLinks(String text, boolean formatCodes) {
        MutableComponent root = Component.literal("");
        Matcher matcher = URL_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String before = text.substring(lastEnd, matcher.start());
            if (!before.isEmpty()) {
                if (formatCodes) {
                    root.append(parseFormatCodes(before));
                } else {
                    root.append(Component.literal(before));
                }
            }

            String url = matcher.group(1);
            root.append(Component.literal(url)
                    .setStyle(Style.EMPTY
                            .withColor(TextColor.fromRgb(0x7878FF))
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§7Click to open")))));

            lastEnd = matcher.end();
        }

        String remaining = text.substring(lastEnd);
        if (!remaining.isEmpty()) {
            if (formatCodes) {
                root.append(parseFormatCodes(remaining));
            } else {
                root.append(Component.literal(remaining));
            }
        }

        return root;
    }

    private static Component parseFormatCodes(String text) {
        MutableComponent root = Component.literal("");

        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(text);
        text = hexMatcher.replaceAll("\u00A7#$1");

        StringBuilder buffer = new StringBuilder();
        Style currentStyle = Style.EMPTY;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if ((c == FORMAT_CHAR || c == '\u00A7') && i + 1 < text.length()) {
                char next = text.charAt(i + 1);

                if (next == '#' && i + 8 <= text.length()) {
                    String hexStr = text.substring(i + 2, i + 8);
                    try {
                        int color = Integer.parseInt(hexStr, 16);
                        if (buffer.length() > 0) {
                            root.append(Component.literal(buffer.toString()).setStyle(currentStyle));
                            buffer.setLength(0);
                        }
                        currentStyle = Style.EMPTY.withColor(TextColor.fromRgb(color));
                        i += 7;
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                }

                Style newStyle = applyFormatCode(next, currentStyle);
                if (newStyle != null) {
                    if (buffer.length() > 0) {
                        root.append(Component.literal(buffer.toString()).setStyle(currentStyle));
                        buffer.setLength(0);
                    }
                    currentStyle = newStyle;
                    i++;
                    continue;
                }
            }

            buffer.append(c);
        }

        if (buffer.length() > 0) {
            root.append(Component.literal(buffer.toString()).setStyle(currentStyle));
        }

        return root;
    }

    private static Style applyFormatCode(char code, Style current) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> Style.EMPTY.withColor(TextColor.fromRgb(0x000000));
            case '1' -> Style.EMPTY.withColor(TextColor.fromRgb(0x0000AA));
            case '2' -> Style.EMPTY.withColor(TextColor.fromRgb(0x00AA00));
            case '3' -> Style.EMPTY.withColor(TextColor.fromRgb(0x00AAAA));
            case '4' -> Style.EMPTY.withColor(TextColor.fromRgb(0xAA0000));
            case '5' -> Style.EMPTY.withColor(TextColor.fromRgb(0xAA00AA));
            case '6' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFFAA00));
            case '7' -> Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA));
            case '8' -> Style.EMPTY.withColor(TextColor.fromRgb(0x555555));
            case '9' -> Style.EMPTY.withColor(TextColor.fromRgb(0x5555FF));
            case 'a' -> Style.EMPTY.withColor(TextColor.fromRgb(0x55FF55));
            case 'b' -> Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF));
            case 'c' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555));
            case 'd' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFF55FF));
            case 'e' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF55));
            case 'f' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF));
            case 'l' -> current.withBold(true);
            case 'o' -> current.withItalic(true);
            case 'n' -> current.withUnderlined(true);
            case 'm' -> current.withStrikethrough(true);
            case 'k' -> current.withObfuscated(true);
            case 'r' -> Style.EMPTY;
            default -> null;
        };
    }
}
