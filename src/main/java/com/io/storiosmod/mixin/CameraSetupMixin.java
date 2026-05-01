package com.io.storiosmod.mixin;

import com.io.storiosmod.client.CutsceneCameraHandler;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraSetupMixin {

    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Inject(method = "setup", at = @At("TAIL"))
    private void storiosmod$overrideCameraPosition(BlockGetter level, Entity entity, boolean detached,
                                                    boolean mirrored, float partialTick, CallbackInfo ci) {
        if (CutsceneCameraHandler.isPlaying()) {
            Vec3 pos = CutsceneCameraHandler.getCameraPos();
            this.setPosition(pos);
            this.detached = true;
        }
    }
}
