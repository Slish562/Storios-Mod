package com.io.storiosmod.entity.client;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.item.magic.crystals.MythrilClusterBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MythrilClusterBlockItemModel extends GeoModel<MythrilClusterBlockItem> {
    @Override
    public ResourceLocation getModelResource(MythrilClusterBlockItem animatable) {
        return new ResourceLocation(StoriosMod.MODID, "geo/mythril_cluster_small.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MythrilClusterBlockItem animatable) {
        return new ResourceLocation(StoriosMod.MODID, "textures/block/mythril_cluster_small.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MythrilClusterBlockItem animatable) {
        return null;
    }
}
