package com.io.storiosmod.network;

import com.io.storiosmod.StoriosMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
        private static final String PROTOCOL_VERSION = "1";
        public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
                        new ResourceLocation(StoriosMod.MODID, "main"),
                        () -> PROTOCOL_VERSION,
                        PROTOCOL_VERSION::equals,
                        PROTOCOL_VERSION::equals);

        public static void register() {
                int id = 0;
                INSTANCE.registerMessage(id++, CustomTitlePacket.class, CustomTitlePacket::encode,
                                CustomTitlePacket::decode,
                                CustomTitlePacket::handle);
                INSTANCE.registerMessage(id++, PlayMusicPacket.class, PlayMusicPacket::encode, PlayMusicPacket::decode,
                                PlayMusicPacket::handle);
                INSTANCE.registerMessage(id++, StopMusicPacket.class, StopMusicPacket::encode, StopMusicPacket::decode,
                                StopMusicPacket::handle);
                INSTANCE.registerMessage(id++, CutsceneStartPacket.class, CutsceneStartPacket::encode,
                                CutsceneStartPacket::decode, CutsceneStartPacket::handle);
                INSTANCE.registerMessage(id++, CutsceneStopPacket.class, CutsceneStopPacket::encode,
                                CutsceneStopPacket::decode, CutsceneStopPacket::handle);
                INSTANCE.registerMessage(id++, CutsceneOpenEditorPacket.class, CutsceneOpenEditorPacket::encode,
                                CutsceneOpenEditorPacket::decode, CutsceneOpenEditorPacket::handle);
                INSTANCE.registerMessage(id++, CutsceneSavePacket.class, CutsceneSavePacket::encode,
                                CutsceneSavePacket::decode, CutsceneSavePacket::handle);
                INSTANCE.registerMessage(id++, CutsceneExitEditorPacket.class, CutsceneExitEditorPacket::encode,
                                CutsceneExitEditorPacket::decode, CutsceneExitEditorPacket::handle);
                INSTANCE.registerMessage(id++, RadiusTriggerSavePacket.class, RadiusTriggerSavePacket::encode,
                                RadiusTriggerSavePacket::decode, RadiusTriggerSavePacket::handle);
                INSTANCE.registerMessage(id++, RadiusTriggerResetPacket.class, RadiusTriggerResetPacket::encode,
                                RadiusTriggerResetPacket::decode, RadiusTriggerResetPacket::handle);
                INSTANCE.registerMessage(id++, CutsceneFinishedPacket.class, CutsceneFinishedPacket::encode,
                                CutsceneFinishedPacket::decode, CutsceneFinishedPacket::handle);
        }
}
