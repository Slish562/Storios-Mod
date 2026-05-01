package com.io.storiosmod;

import com.io.storiosmod.entity.client.GeoDirectionalBlockRenderer;
import com.io.storiosmod.registries.BlockRegistry;
import com.io.storiosmod.registries.CreativeTabRegistry;
import com.io.storiosmod.registries.ItemRegistry;
import com.io.storiosmod.registries.AttributesRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(StoriosMod.MODID)
public class StoriosMod {

    public static final String MODID = "storios_mod";

    private static final Logger LOGGER = LogUtils.getLogger();

    public StoriosMod(IEventBus modEventBus) {

        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        AttributesRegistry.register(modEventBus);


        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        com.io.storiosmod.commands.SetMaxDurabilityCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.GradientCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.GradientTitleCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.PlayMusicCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.StopMusicCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.NicknameCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.MissedCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.ChatColorCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.ChatStyleCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.TabStyleCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.SidebarCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.CutsceneCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onPlayerChangeGameMode(
            net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity().getPersistentData().contains("storiosmod_prev_gamemode")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
        com.io.storiosmod.chat.ChatHistoryManager.init(
                net.neoforged.fml.loading.FMLPaths.GAMEDIR.get());
        com.io.storiosmod.chat.ChatStyleConfig.init(
                net.neoforged.fml.loading.FMLPaths.GAMEDIR.get());
        com.io.storiosmod.tablist.TabListManager.init(
                net.neoforged.fml.loading.FMLPaths.GAMEDIR.get());
        com.io.storiosmod.sidebar.SidebarManager.init(
                net.neoforged.fml.loading.FMLPaths.GAMEDIR.get());
        com.io.storiosmod.cutscene.CutsceneStorage.init(
                net.neoforged.fml.loading.FMLPaths.GAMEDIR.get());
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            com.io.storiosmod.client.ClientTitleHandler.register();
            com.io.storiosmod.client.ClientMusicHandler.register();
            com.io.storiosmod.client.CutsceneCameraHandler.register();
            com.io.storiosmod.client.CutscenePathRenderer.register();
            com.io.storiosmod.client.CutsceneEditorManager.register();
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK_ENTITY.get(),
                    context -> new GeoDirectionalBlockRenderer());
        }

        @SubscribeEvent
        public static void registerKeyMappings(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
            com.io.storiosmod.client.CutsceneEditorManager.registerKeyMappings(event);
        }
    }
}


