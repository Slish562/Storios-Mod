package com.io.storiosmod.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {

    @Accessor("tabListDisplayName")
    Component storiosmod$getTabListDisplayName();

    @Accessor("tabListDisplayName")
    void storiosmod$setTabListDisplayName(Component name);
}

