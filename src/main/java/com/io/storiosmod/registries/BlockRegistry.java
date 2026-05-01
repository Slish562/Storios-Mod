package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.block.CoolerBlock;
import com.io.storiosmod.block.GeoDirectionalBlock;
import com.io.storiosmod.block.RadiusTriggerBlock;
import com.io.storiosmod.block.entity.GeoDirectionalBlockEntity;
import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity;
import com.io.storiosmod.module.WastelandModule;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.registries.Registries;

public class BlockRegistry {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK,
                        StoriosMod.MODID);
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(Registries.BLOCK_ENTITY_TYPE, StoriosMod.MODID);

        public static void register(IEventBus eventBus) {
                BLOCKS.register(eventBus);
                BLOCK_ENTITIES.register(eventBus);
        }

        public static final DeferredHolder<Block, Block> MYTHRIL_CLUSTER_SMALL_BLOCK = BLOCKS.register("mythril_cluster_small",
                        () -> new GeoDirectionalBlock());
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeoDirectionalBlockEntity>> MYTHRIL_CLUSTER_SMALL_BLOCK_ENTITY = BLOCK_ENTITIES
                        .register("mythril_cluster_small_be",
                                        () -> BlockEntityType.Builder
                                                        .of(GeoDirectionalBlockEntity::new,
                                                                        BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK.get())
                                                        .build(null));

        public static final DeferredHolder<Block, Block> COOLER = WastelandModule.ENABLED ? BLOCKS.register("cooler_block",
                        () -> new CoolerBlock(
                                        BlockBehaviour.Properties.of().strength(1.5f).requiresCorrectToolForDrops()))
                        : null;

        public static final DeferredHolder<Block, Block> RADIUS_TRIGGER_BLOCK = BLOCKS.register("radius_trigger",
                        RadiusTriggerBlock::new);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadiusTriggerBlockEntity>> RADIUS_TRIGGER_BLOCK_ENTITY = BLOCK_ENTITIES
                        .register("radius_trigger_be",
                                        () -> BlockEntityType.Builder
                                                        .of(RadiusTriggerBlockEntity::new,
                                                                        BlockRegistry.RADIUS_TRIGGER_BLOCK.get())
                                                        .build(null));
}

