package com.io.storiosmod.item.drinks;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class ReusableWaterBottle extends Item {

    private final int maxUses;
    private final int useDuration;
    private final int cooldownTicks;
    private final Item containerItem;

    private final int purity;
    private final int quenchAmount;
    private final float hydration;

    private ReusableWaterBottle(Builder builder) {
        super(builder.properties
                .durability(builder.maxUses > 1 ? builder.maxUses : 0)
                .setNoRepair());

        this.maxUses = builder.maxUses;
        this.useDuration = builder.useDuration;
        this.cooldownTicks = builder.cooldownTicks;
        this.containerItem = builder.containerItem;

        this.purity = builder.purity;
        this.quenchAmount = builder.quenchAmount;
        this.hydration = builder.hydration;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        applyThirstNBT(stack);
        return stack;
    }

    private void applyThirstNBT(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag thirstTag = new CompoundTag();
        thirstTag.putInt("purity", purity);
        thirstTag.putInt("quench", quenchAmount);
        thirstTag.putFloat("hydration", hydration);
        tag.put("thirst", thirstTag);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
                    com.io.storiosmod.compat.ThirstCompat.drink(player, purity, quenchAmount, hydration);
                }

                player.awardStat(Stats.ITEM_USED.get(this));

                if (maxUses > 1) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }

                if (cooldownTicks > 0) {
                    player.getCooldowns().addCooldown(this, cooldownTicks);
                }
            }

            if (containerItem != null && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                return new ItemStack(containerItem);
            }
        }

        return stack;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isEdible() {
        return true;
    }

    public static class Builder {
        private final Item.Properties properties;
        private int maxUses = 1;
        private int useDuration = 32;
        private int cooldownTicks = 0;
        private Item containerItem = null;

        private int purity = 3;
        private int quenchAmount = 25;
        private float hydration = 0.8f;

        public Builder(Item.Properties properties) {
            this.properties = properties;
        }

        public Builder uses(int uses) {
            this.maxUses = uses;
            return this;
        }

        public Builder duration(int ticks) {
            this.useDuration = ticks;
            return this;
        }

        public Builder cooldown(int ticks) {
            this.cooldownTicks = ticks;
            return this;
        }

        public Builder container(Item emptyBottle) {
            this.containerItem = emptyBottle;
            return this;
        }

        public Builder purity(int level) { // 0-3
            this.purity = Math.max(0, Math.min(3, level));
            return this;
        }

        public Builder quench(int amount) {
            this.quenchAmount = amount;
            return this;
        }

        public Builder hydration(float value) {
            this.hydration = value;
            return this;
        }

        public ReusableWaterBottle build() {
            return new ReusableWaterBottle(this);
        }
    }
}