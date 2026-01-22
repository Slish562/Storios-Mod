package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.item.drinks.ReusableWaterBottle;
import com.io.storiosmod.item.food.CannedFood;
import com.io.storiosmod.item.magic.crystals.MythrilClusterBlockItem;
import com.io.storiosmod.item.medic.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.io.storiosmod.registries.BlockRegistry.COOLER;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StoriosMod.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
    public static final RegistryObject<Item> MYTHRIL_CLUSTER_SMALL_ITEM = ITEMS.register("mythril_cluster_small", () -> new MythrilClusterBlockItem(BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    //public static final RegistryObject<Item> TEST_GEOB = ITEMS.register("test_geob", () -> new BlockItem(BlockRegistry.TESTGEOBLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> BANDAGE = ITEMS.register("bandage", () ->
            new MedicalItem(
                    4, 6.0f, 40, 60, 0, 0,
                    false,
                    new Item.Properties().rarity(Rarity.COMMON)
            )
    );

    public static final RegistryObject<Item> PAINKILLER = ITEMS.register("painkiller", () ->
            new MedicalItem(
                    6, 10.0f, 60, 60, 100, 0,
                    true,
                    new Item.Properties().rarity(Rarity.COMMON)
            )
    );

    public static final RegistryObject<Item> MORPHINE = ITEMS.register("morphine", () ->
            new MedicalItem(
                    4, 0.0f, 50, 60, 500, 0,
                    true,
                    new Item.Properties().rarity(Rarity.COMMON)
            )
    );

    public static final RegistryObject<Item> MEDKIT = ITEMS.register("medkit", () ->
            new MedicalItem(
                    8, 20.0f, 80, 60, 800, 1,
                    false,
                    new Item.Properties().rarity(Rarity.UNCOMMON)
            )
    );

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
    public static final RegistryObject<Item> UNKNOWN_POTION = ITEMS.register("unknown_potion", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> NOTE = ITEMS.register("note", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> LETTER = ITEMS.register("letter", () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> EMPTY_CAN = ITEMS.register("empty_can", () ->
            new CannedFood.Builder(new Item.Properties().stacksTo(1)).build()
    );
    public static final RegistryObject<Item> CANNED_FOOD = ITEMS.register("canned_food", () ->
            new CannedFood.Builder(new Item.Properties().stacksTo(1))
                    .uses(4)
                    .cooldown(60)
                    .container(EMPTY_CAN.get())
                    .standardFood(4, 1f)
                    .build()
    );
    public static final RegistryObject<Item> REUSABLE_BOTTLE_EMPTY = ITEMS.register("reusable_bottle_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> REUSABLE_BOTTLE = ITEMS.register("reusable_bottle_pure",
            () -> new ReusableWaterBottle.Builder(new Item.Properties().stacksTo(1))
                    .uses(1)  // бесконечная
                    .container(REUSABLE_BOTTLE_EMPTY.get())
                    .purity(4)
                    .quench(30)
                    .hydration(1.0f)
                    .duration(32)
                    .cooldown(20)
                    .build()
    );

    public static final RegistryObject<Item> COOLER_ITEM = ITEMS.register("cooler_block", () -> new BlockItem(COOLER.get(), new Item.Properties()));


}
