package com.io.storiosmod.entity.client;

import com.io.storiosmod.block.*;
import com.io.storiosmod.block.entity.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import net.minecraft.client.renderer.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.*;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.*;

public class GeoDirectionalBlockRenderer extends GeoBlockRenderer<GeoDirectionalBlockEntity> {

    public GeoDirectionalBlockRenderer() {
        super(new GeoDirectionalBlockModel());
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        poseStack.translate(0, 0.5, 0);

        switch (facing) {
            case UP:
                break;
            case DOWN:
                poseStack.mulPose(Axis.XP.rotationDegrees(180f));
                break;
            case NORTH:
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                break;
            case SOUTH:
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                break;
            case EAST:
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90f));
                break;
            case WEST:
                poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
                break;
        }

        BlockPos pos = this.animatable != null ? this.animatable.getBlockPos() : BlockPos.ZERO;
        long seed = pos.asLong();
        seed ^= seed >>> 16;
        seed *= 0x85ebca6b;
        seed ^= seed >>> 13;
        seed *= 0xc2b2ae35;
        seed ^= seed >>> 16;
        float randomRot = (Math.abs(seed) % 360);
        poseStack.mulPose(Axis.YP.rotationDegrees(randomRot));

        poseStack.translate(0, -0.5, 0);
    }

    @Override
    protected Direction getFacing(GeoDirectionalBlockEntity animatable) {
        BlockState blockState = animatable.getBlockState();
        if (blockState.getBlock() instanceof GeoDirectionalBlock) {
            return blockState.getValue(GeoDirectionalBlock.FACING);
        }
        return Direction.UP;
    }
}
