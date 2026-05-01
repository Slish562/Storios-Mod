package com.io.storiosmod.block;

import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity;
import com.io.storiosmod.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class RadiusTriggerBlock extends Block implements EntityBlock {

    public RadiusTriggerBlock() {
        super(BlockBehaviour.Properties.of().strength(2.0f).requiresCorrectToolForDrops());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadiusTriggerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (!level.isClientSide && type == BlockRegistry.RADIUS_TRIGGER_BLOCK_ENTITY.get()) {
            return (lvl, pos, st, be) -> RadiusTriggerBlockEntity.serverTick(lvl, pos, st,
                    (RadiusTriggerBlockEntity) be);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        if (!player.hasPermissions(2)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RadiusTriggerBlockEntity triggerBE) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> com.io.storiosmod.client.ClientHooks.openRadiusTriggerScreen(triggerBE));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
