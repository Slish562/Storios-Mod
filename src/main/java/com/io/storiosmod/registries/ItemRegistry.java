package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.item.medic.BandageItem;
import com.io.storiosmod.item.medic.MedKitItem;
import com.io.storiosmod.item.medic.MorphineItem;
import com.io.storiosmod.item.medic.PainKillerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StoriosMod.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
    public static final RegistryObject<Item> MYTHRIL_CLASTER = ITEMS.register("mythril_cluster", () -> new BlockItem(BlockRegistry.MYTHRIL_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    //public static final RegistryObject<Item> TEST_GEOB = ITEMS.register("test_geob", () -> new BlockItem(BlockRegistry.TESTGEOBLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> BANDAGE = ITEMS.register("bandage", () -> new BandageItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PAINKILLER = ITEMS.register("painkiller", () -> new PainKillerItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MORPHINE = ITEMS.register("morphine", () -> new MorphineItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MEDKIT = ITEMS.register("medkit", () -> new MedKitItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> COPPER_GEAR = ITEMS.register("copper_gear", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> STEEL_GEAR = ITEMS.register("steel_gear", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> COBALT_GEAR = ITEMS.register("cobalt_gear", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> JADE_GEAR = ITEMS.register("jade_gear", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> IRIDIUM_GEAR = ITEMS.register("iridium_gear", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> MYTHRIL = ITEMS.register("mythril", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FIORELLITE = ITEMS.register("fiorellite", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LUMINARIS = ITEMS.register("luminaris", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NOTE = ITEMS.register("note", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> LETTER = ITEMS.register("letter", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
}
