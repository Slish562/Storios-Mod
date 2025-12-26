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
    public void actuallyRender(PoseStack poseStack, GeoDirectionalBlockEntity animatable,
                               BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {

        BlockState blockState = animatable.getBlockState();
        Direction facing = Direction.UP;

        if (blockState.getBlock() instanceof GeoDirectionalBlock) {
            facing = blockState.getValue(GeoDirectionalBlock.FACING);
        }

        poseStack.pushPose();

        applyRotationForFacing(facing, poseStack);

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay,
                red, green, blue, alpha);

        poseStack.popPose();
    }

    private void applyRotationForFacing(Direction facing, PoseStack poseStack) {
        poseStack.translate(0.5, 0.5, 0.5);

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

        poseStack.translate(-0.5, -0.5, -0.5);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
    }
}