package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.item.medic.*;
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
    public static final RegistryObject<Item> MYTHRIL_CLUSTER_SMALL_ITEM = ITEMS.register("mythril_cluster_small", () -> new MythrilClusterBlockItem(BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    //public static final RegistryObject<Item> TEST_GEOB = ITEMS.register("test_geob", () -> new BlockItem(BlockRegistry.TESTGEOBLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> BANDAGE = ITEMS.register("bandage", () -> new BandageItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PAINKILLER = ITEMS.register("painkiller", () -> new PainKillerItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MORPHINE = ITEMS.register("morphine", () -> new MorphineItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MEDKIT = ITEMS.register("medkit", () -> new MedKitItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> CREDIT_1 = ITEMS.register("credit_1", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> CREDIT_10 = ITEMS.register("credit_10", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> CREDIT_100 = ITEMS.register("credit_100", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> CREDIT_1000 = ITEMS.register("credit_1000", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CREDIT_10000 = ITEMS.register("credit_10000", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BATTERY_SMALL = ITEMS.register("battery_small", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BATTERY_NORMAL = ITEMS.register("battery_normal", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MONITOR_DEVICE_ACTIVE = ITEMS.register("monitor_device_active", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> MONITOR_DEVICE_INACTIVE = ITEMS.register("monitor_device_inactive", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));


    public static final RegistryObject<Item> GREEN_FILM = ITEMS.register("green_film", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PINK_FILM = ITEMS.register("pink_film", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PURPLE_FILM = ITEMS.register("purple_film", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PURPLE_MONITOR = ITEMS.register("purple_monitor", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PURPLE_SHARD = ITEMS.register("purple_shard", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> BIG_TEST_TUBE = ITEMS.register("big_test_tube", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));

    public static final RegistryObject<Item> ROUGH_MYTHRIL = ITEMS.register("rough_mythril", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MYTHRIL = ITEMS.register("mythril", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FIORELLITE = ITEMS.register("fiorellite", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LUMINARIS = ITEMS.register("luminaris", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NOTE = ITEMS.register("note", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> LETTER = ITEMS.register("letter", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
}
