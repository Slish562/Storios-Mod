package com.io.storiosmod.entity.client;

import com.io.storiosmod.block.entity.GeoDirectionalBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GeoDirectionalBlockRenderer extends GeoBlockRenderer<GeoDirectionalBlockEntity> {
    public GeoDirectionalBlockRenderer() {
        super(new GeoDirectionalBlockModel());
    }
}
