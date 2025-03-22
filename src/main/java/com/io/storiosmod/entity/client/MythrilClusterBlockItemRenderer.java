package com.io.storiosmod.entity.client;

import com.io.storiosmod.item.medic.MythrilClusterBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MythrilClusterBlockItemRenderer extends GeoItemRenderer<MythrilClusterBlockItem> {
    public MythrilClusterBlockItemRenderer() {
        super(new MythrilClusterBlockItemModel());
    }
}