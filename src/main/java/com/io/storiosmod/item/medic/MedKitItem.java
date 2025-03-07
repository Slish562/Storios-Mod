package com.io.storiosmod.item.medic;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class MedKitItem extends Item {
    private static final int COOLDOWN_TICKS = 60;
    private static final int MAX_USES = 8;
    private static final float HEAL_AMOUNT = 20.0f;

    public MedKitItem(Properties properties) {
        super(properties.defaultDurability(MAX_USES).setNoRepair());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 80;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }

            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(HEAL_AMOUNT);
                stack.hurt(1, level.random, null);

                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3));
               // level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 0.8F);

                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}