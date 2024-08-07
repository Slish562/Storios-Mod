package com.io.storiosmod.entity.client;

import com.io.storiosmod.block.entity.TestGeoBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TestGeoBlockRenderer extends GeoBlockRenderer<TestGeoBlockEntity> {
    public TestGeoBlockRenderer() {
        super(new TestGeoBlockModel());
    }
}
