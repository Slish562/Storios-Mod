package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.block.CoolerBlock;
import com.io.storiosmod.block.GeoDirectionalBlock;
import com.io.storiosmod.block.entity.GeoDirectionalBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, StoriosMod.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, StoriosMod.MODID);
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }

    public static final RegistryObject<Block> COOLER = BLOCKS.register("cooler_block",
            () -> new CoolerBlock(BlockBehaviour.Properties.of().strength(1.5f).requiresCorrectToolForDrops()));
    public static final RegistryObject<BlockEntityType<GeoDirectionalBlockEntity>> MYTHRIL_CLUSTER_SMALL_BLOCK_ENTITY = BLOCK_ENTITIES.register("mythril_cluster_small_be",
            ()-> BlockEntityType.Builder.of(GeoDirectionalBlockEntity::new, BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK.get()).build(null));

    /*
    public static final RegistryObject<BlockEntityType<TestGeoBlockEntity>> TESTGEOBLOCK_BE = BLOCK_ENTITIES.register("test_block_be",
            ()-> BlockEntityType.Builder.of(TestGeoBlockEntity::new, BlockRegistry.TESTGEOBLOCK.get()).build(null));

     */

    public static final RegistryObject<Block> MYTHRIL_CLUSTER_SMALL_BLOCK = BLOCKS.register("mythril_cluster_small", () -> new GeoDirectionalBlock());
   // public static final RegistryObject<Block> TESTGEOBLOCK = BLOCKS.register("test_block", () -> new TestGeoBlock());
}
