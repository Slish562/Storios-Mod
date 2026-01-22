package com.io.storiosmod.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    @Nullable
    private CompoundTag tag;

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void storiosmod$getMaxDamage(CallbackInfoReturnable<Integer> cir) {
        if (this.tag != null && this.tag.contains("storiosmod:max_damage")) {
            cir.setReturnValue(this.tag.getInt("storiosmod:max_damage"));
        }
    }
}
