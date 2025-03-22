package com.io.storiosmod.entity.client;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.block.entity.GeoDirectionalBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GeoDirectionalBlockModel extends GeoModel<GeoDirectionalBlockEntity> {
    @Override
    public ResourceLocation getModelResource(GeoDirectionalBlockEntity animatable) {
        return new ResourceLocation(StoriosMod.MODID, "geo/mythril_cluster_small.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeoDirectionalBlockEntity animatable) {
        return new ResourceLocation(StoriosMod.MODID, "textures/block/mythril_cluster_small.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeoDirectionalBlockEntity animatable) {
        return null;
    }
}
