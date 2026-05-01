package com.io.storiosmod.registries;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Rarity;

public final class ModRarities {

    public static final Rarity MYTHIC = ofFormatting("MYTHIC", ChatFormatting.RED);

    public static final Rarity LEGENDARY = ofHex("LEGENDARY", "#FFD700");
    public static final Rarity ANCIENT = ofHex("ANCIENT", "#00CED1");

    public static Rarity ofHex(String name, String hex) {
        return Rarity.EPIC;
    }

    public static Rarity ofFormatting(String name, ChatFormatting formatting) {
        return Rarity.EPIC;
    }

    private ModRarities() {
    }
}

