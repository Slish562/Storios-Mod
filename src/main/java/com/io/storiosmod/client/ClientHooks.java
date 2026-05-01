package com.io.storiosmod.client;

import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity;
import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openRadiusTriggerScreen(RadiusTriggerBlockEntity triggerBE) {
        Minecraft.getInstance().setScreen(new RadiusTriggerScreen(triggerBE));
    }
}

