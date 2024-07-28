package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = StoriosMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabRegistry {

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StoriosMod.MODID);

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    public static final RegistryObject<CreativeModeTab> EQUIPMENT_TAB = TABS.register("story_items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + StoriosMod.MODID + ".story_items_tab"))
            .icon(() -> new ItemStack(ItemRegistry.COPPER_GEAR.get()))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(ItemRegistry.MYTHRIL_CLASTER.get());
                entries.accept(ItemRegistry.MYTHRIL.get());
                entries.accept(ItemRegistry.FIORELLITE.get());
                entries.accept(ItemRegistry.LUMINARIS.get());
                entries.accept(ItemRegistry.COPPER_GEAR.get());
                entries.accept(ItemRegistry.STEEL_GEAR.get());
                entries.accept(ItemRegistry.COBALT_GEAR.get());
                entries.accept(ItemRegistry.JADE_GEAR.get());
                entries.accept(ItemRegistry.IRIDIUM_GEAR.get());
                entries.accept(ItemRegistry.NOTE.get());
                entries.accept(ItemRegistry.LETTER.get());
                entries.accept(ItemRegistry.SCROLL.get());
            })
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .build());
}

