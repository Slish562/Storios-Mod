package com.io.storiosmod.registries;

import com.io.storiosmod.block.DirectionalBlock;
import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.block.TestGeoBlock;
import com.io.storiosmod.block.entity.TestGeoBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    /*
    public static final RegistryObject<BlockEntityType<TestGeoBlockEntity>> TESTGEOBLOCK_BE = BLOCK_ENTITIES.register("test_block_be",
            ()-> BlockEntityType.Builder.of(TestGeoBlockEntity::new, BlockRegistry.TESTGEOBLOCK.get()).build(null));

     */

    public static final RegistryObject<Block> MYTHRIL_BLOCK = BLOCKS.register("mythril_cluster", () -> new DirectionalBlock());
   // public static final RegistryObject<Block> TESTGEOBLOCK = BLOCKS.register("test_block", () -> new TestGeoBlock());
}
