package com.io.storiosmod.registries;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Rarity;

public final class ModRarities {

    public static final Rarity MYTHIC = ofFormatting("MYTHIC", ChatFormatting.RED);

    public static final Rarity LEGENDARY = ofHex("LEGENDARY", "#FFD700");
    public static final Rarity ANCIENT = ofHex("ANCIENT", "#00CED1");

    public static Rarity ofHex(String name, String hex) {
        int rgb = Integer.parseInt(hex.replace("#", ""), 16);
        TextColor color = TextColor.fromRgb(rgb);
        return Rarity.create(name, style -> style.withColor(color));
    }

    public static Rarity ofFormatting(String name, ChatFormatting formatting) {
        return Rarity.create(name, formatting);
    }

    private ModRarities() {
    }
}
