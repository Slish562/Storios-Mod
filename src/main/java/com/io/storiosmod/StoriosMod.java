package com.io.storiosmod;

import com.io.storiosmod.entity.client.GeoDirectionalBlockRenderer;
import com.io.storiosmod.registries.BlockRegistry;
import com.io.storiosmod.registries.CreativeTabRegistry;
import com.io.storiosmod.registries.ItemRegistry;
import com.io.storiosmod.registries.AttributesRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(StoriosMod.MODID)
public class StoriosMod {

    public static final String MODID = "storios_mod";

    private static final Logger LOGGER = LogUtils.getLogger();

    public StoriosMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        AttributesRegistry.register(modEventBus);
        GeckoLib.initialize();
        com.io.storiosmod.network.PacketHandler.register();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        com.io.storiosmod.commands.SetMaxDurabilityCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.GradientCommand.register(event.getDispatcher());
        com.io.storiosmod.commands.GradientTitleCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            com.io.storiosmod.client.ClientTitleHandler.register();
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK_ENTITY.get(),
                    context -> new GeoDirectionalBlockRenderer());
        }

    }
}
