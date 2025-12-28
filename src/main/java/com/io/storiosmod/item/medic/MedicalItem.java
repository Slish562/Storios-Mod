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

public class MedicalItem extends Item {

    private final int maxUses;
    private final float healAmount;
    private final int useDuration;
    private final int cooldownTicks;
    private final int regenDuration;
    private final int regenAmplifier;
    private final boolean playDrinkSound;

    /**
     *  Medical item
     *
     * @param maxUses         Maximum number of uses (durability)
     * @param healAmount      Amount of instant healing
     * @param useDuration     Duration of use (in ticks)
     * @param cooldownTicks   Cooldown after use (in ticks)
     * @param regenDuration   Duration of regeneration effect (0 = no effect)
     * @param regenAmplifier  Regeneration effect level (0-based)
     * @param playDrinkSound  Whether to play the drinking sound
     * @param properties      Item properties (rarity, etc.)
     */
    public MedicalItem(int maxUses,
                       float healAmount,
                       int useDuration,
                       int cooldownTicks,
                       int regenDuration,
                       int regenAmplifier,
                       boolean playDrinkSound,
                       Properties properties) {
        super(properties.durability(maxUses > 0 ? maxUses : 1).setNoRepair());
        this.maxUses = maxUses;
        this.healAmount = healAmount;
        this.useDuration = useDuration;
        this.cooldownTicks = cooldownTicks;
        this.regenDuration = regenDuration;
        this.regenAmplifier = regenAmplifier;
        this.playDrinkSound = playDrinkSound;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return useDuration;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                if (healAmount > 0) {
                    player.heal(healAmount);
                }

                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));

                if (regenDuration > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDuration, regenAmplifier));
                }

                if (playDrinkSound) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 0.8F);
                }

                player.getCooldowns().addCooldown(this, cooldownTicks);
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