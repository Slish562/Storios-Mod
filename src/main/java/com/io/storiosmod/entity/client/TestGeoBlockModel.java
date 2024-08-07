package com.io.storiosmod.entity.client;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.block.entity.TestGeoBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TestGeoBlockModel extends GeoModel<TestGeoBlockEntity> {
    @Override
    public ResourceLocation getModelResource(TestGeoBlockEntity animatable) {
        return new ResourceLocation(StoriosMod.MODID, "geo/test_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TestGeoBlockEntity animatable) {
        return new ResourceLocation(StoriosMod.MODID, "textures/block/test_block.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TestGeoBlockEntity animatable) {
        return null;
    }
}
