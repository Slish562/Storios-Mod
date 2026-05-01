package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.item.drinks.ReusableWaterBottle;
import com.io.storiosmod.item.food.CannedFood;
import com.io.storiosmod.item.magic.crystals.MythrilClusterBlockItem;
import com.io.storiosmod.item.medic.*;
import com.io.storiosmod.module.WastelandModule;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemRegistry {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM,
                        StoriosMod.MODID);

        public static void register(IEventBus eventBus) {
                ITEMS.register(eventBus);
        }

        public static final DeferredHolder<Item, Item> MYTHRIL_CLUSTER_SMALL_ITEM = ITEMS.register("mythril_cluster_small",
                        () -> new MythrilClusterBlockItem(BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK.get(),
                                        new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
        public static final DeferredHolder<Item, Item> ROUGH_MYTHRIL = ITEMS.register("rough_mythril",
                        () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
        public static final DeferredHolder<Item, Item> MYTHRIL = ITEMS.register("mythril",
                        () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
        public static final DeferredHolder<Item, Item> FIORELLITE = ITEMS.register("fiorellite",
                        () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
        public static final DeferredHolder<Item, Item> LUMINARIS = ITEMS.register("luminaris",
                        () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

        public static final DeferredHolder<Item, Item> BANDAGE = WastelandModule.ENABLED
                        ? ITEMS.register("bandage", () -> new MedicalItem(4, 6.0f, 40, 60, 0, 0, false,
                                        new Item.Properties().rarity(Rarity.COMMON)))
                        : null;

        public static final DeferredHolder<Item, Item> PAINKILLER = WastelandModule.ENABLED
                        ? ITEMS.register("painkiller", () -> new MedicalItem(6, 10.0f, 60, 60, 100, 0, true,
                                        new Item.Properties().rarity(Rarity.COMMON)))
                        : null;

        public static final DeferredHolder<Item, Item> MORPHINE = WastelandModule.ENABLED
                        ? ITEMS.register("morphine", () -> new MedicalItem(4, 0.0f, 50, 60, 500, 0, true,
                                        new Item.Properties().rarity(Rarity.COMMON)))
                        : null;

        public static final DeferredHolder<Item, Item> MEDKIT = WastelandModule.ENABLED
                        ? ITEMS.register("medkit", () -> new MedicalItem(8, 20.0f, 80, 60, 800, 1, false,
                                        new Item.Properties().rarity(Rarity.UNCOMMON)))
                        : null;

        public static final DeferredHolder<Item, Item> CREDIT_1 = WastelandModule.ENABLED
                        ? ITEMS.register("credit_1", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> CREDIT_10 = WastelandModule.ENABLED
                        ? ITEMS.register("credit_10", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> CREDIT_100 = WastelandModule.ENABLED
                        ? ITEMS.register("credit_100", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> CREDIT_1000 = WastelandModule.ENABLED
                        ? ITEMS.register("credit_1000", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> CREDIT_10000 = WastelandModule.ENABLED
                        ? ITEMS.register("credit_10000", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)))
                        : null;

        public static final DeferredHolder<Item, Item> BATTERY_SMALL = WastelandModule.ENABLED
                        ? ITEMS.register("battery_small", () -> new Item(new Item.Properties().rarity(Rarity.RARE)))
                        : null;
        public static final DeferredHolder<Item, Item> BATTERY_NORMAL = WastelandModule.ENABLED
                        ? ITEMS.register("battery_normal", () -> new Item(new Item.Properties().rarity(Rarity.RARE)))
                        : null;
        public static final DeferredHolder<Item, Item> MONITOR_DEVICE_ACTIVE = WastelandModule.ENABLED
                        ? ITEMS.register("monitor_device_active",
                                        () -> new Item(new Item.Properties().rarity(Rarity.RARE)))
                        : null;
        public static final DeferredHolder<Item, Item> MONITOR_DEVICE_INACTIVE = WastelandModule.ENABLED
                        ? ITEMS.register("monitor_device_inactive",
                                        () -> new Item(new Item.Properties().rarity(Rarity.RARE)))
                        : null;

        public static final DeferredHolder<Item, Item> GREEN_FILM = WastelandModule.ENABLED
                        ? ITEMS.register("green_film", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> PINK_FILM = WastelandModule.ENABLED
                        ? ITEMS.register("pink_film", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> PURPLE_FILM = WastelandModule.ENABLED
                        ? ITEMS.register("purple_film", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> PURPLE_MONITOR = WastelandModule.ENABLED
                        ? ITEMS.register("purple_monitor", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> PURPLE_SHARD = WastelandModule.ENABLED
                        ? ITEMS.register("purple_shard", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> BIG_TEST_TUBE = WastelandModule.ENABLED
                        ? ITEMS.register("big_test_tube", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;

        public static final DeferredHolder<Item, Item> UNKNOWN_POTION = WastelandModule.ENABLED
                        ? ITEMS.register("unknown_potion", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> NOTE = WastelandModule.ENABLED
                        ? ITEMS.register("note", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> SCROLL = WastelandModule.ENABLED
                        ? ITEMS.register("scroll", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;
        public static final DeferredHolder<Item, Item> LETTER = WastelandModule.ENABLED
                        ? ITEMS.register("letter", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)))
                        : null;

        public static final DeferredHolder<Item, Item> EMPTY_CAN = WastelandModule.ENABLED
                        ? ITEMS.register("empty_can",
                                        () -> new CannedFood.Builder(new Item.Properties().stacksTo(1)).build())
                        : null;

        public static final DeferredHolder<Item, Item> CANNED_FOOD = WastelandModule.ENABLED ? ITEMS.register("canned_food",
                        () -> new CannedFood.Builder(new Item.Properties().stacksTo(1))
                                        .uses(4)
                                        .cooldown(60)
                                        .container(EMPTY_CAN.get())
                                        .standardFood(4, 1f)
                                        .build())
                        : null;

        public static final DeferredHolder<Item, Item> REUSABLE_BOTTLE_EMPTY = WastelandModule.ENABLED
                        ? ITEMS.register("reusable_bottle_empty",
                                        () -> new Item(new Item.Properties().stacksTo(16)))
                        : null;

        public static final DeferredHolder<Item, Item> REUSABLE_BOTTLE = WastelandModule.ENABLED
                        ? ITEMS.register("reusable_bottle_pure",
                                        () -> new ReusableWaterBottle.Builder(new Item.Properties().stacksTo(1))
                                                        .uses(1)
                                                        .container(REUSABLE_BOTTLE_EMPTY.get())
                                                        .purity(4)
                                                        .quench(30)
                                                        .hydration(1.0f)
                                                        .duration(32)
                                                        .cooldown(20)
                                                        .build())
                        : null;

        public static final DeferredHolder<Item, Item> COOLER_ITEM = WastelandModule.ENABLED ? ITEMS.register("cooler_block",
                        () -> new BlockItem(BlockRegistry.COOLER.get(), new Item.Properties())) : null;

        public static final DeferredHolder<Item, Item> RADIUS_TRIGGER_ITEM = ITEMS.register("radius_trigger",
                        () -> new BlockItem(BlockRegistry.RADIUS_TRIGGER_BLOCK.get(), new Item.Properties()));
}


