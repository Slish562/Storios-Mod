package com.io.storiosmod.item.medic;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class PainKillerItem extends Item {
    private static final int COOLDOWN_TICKS = 60;
    private static final int MAX_USES = 6;
    private static final float HEAL_AMOUNT = 10.0f;

    public PainKillerItem(Properties properties) {
        super(properties.durability(MAX_USES).setNoRepair());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 60;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                player.heal(HEAL_AMOUNT);
                stack.hurtAndBreak(1, player, (p) -> {
                    p.broadcastBreakEvent(player.getUsedItemHand());
                });

                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 0.8F);

                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            }
        }
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (player.getHealth() < player.getMaxHealth()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}