package com.io.storiosmod.network;

import com.io.storiosmod.StoriosMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = StoriosMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(StoriosMod.MODID);

        registrar.playToClient(CustomTitlePacket.TYPE, CustomTitlePacket.STREAM_CODEC, CustomTitlePacket::handle);
        registrar.playToClient(PlayMusicPacket.TYPE, PlayMusicPacket.STREAM_CODEC, PlayMusicPacket::handle);
        registrar.playToClient(StopMusicPacket.TYPE, StopMusicPacket.STREAM_CODEC, StopMusicPacket::handle);
        registrar.playToClient(CutsceneStartPacket.TYPE, CutsceneStartPacket.STREAM_CODEC, CutsceneStartPacket::handle);
        registrar.playToClient(CutsceneStopPacket.TYPE, CutsceneStopPacket.STREAM_CODEC, CutsceneStopPacket::handle);
        registrar.playToClient(CutsceneOpenEditorPacket.TYPE, CutsceneOpenEditorPacket.STREAM_CODEC, CutsceneOpenEditorPacket::handle);

        registrar.playToServer(CutsceneSavePacket.TYPE, CutsceneSavePacket.STREAM_CODEC, CutsceneSavePacket::handle);
        registrar.playToServer(CutsceneExitEditorPacket.TYPE, CutsceneExitEditorPacket.STREAM_CODEC, CutsceneExitEditorPacket::handle);
        registrar.playToServer(RadiusTriggerSavePacket.TYPE, RadiusTriggerSavePacket.STREAM_CODEC, RadiusTriggerSavePacket::handle);
        registrar.playToServer(RadiusTriggerResetPacket.TYPE, RadiusTriggerResetPacket.STREAM_CODEC, RadiusTriggerResetPacket::handle);
        registrar.playToServer(CutsceneFinishedPacket.TYPE, CutsceneFinishedPacket.STREAM_CODEC, CutsceneFinishedPacket::handle);
    }
}


