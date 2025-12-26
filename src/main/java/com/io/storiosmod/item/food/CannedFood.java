package com.io.storiosmod.item.food;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CannedFood extends Item {

    private final int maxUses;
    private final float healAmount;
    private final int useDuration;
    private final int cooldownTicks;
    private final List<MobEffectInstance> effects;
    private final Item containerItem;

    private final FoodProperties foodProperties;

    private CannedFood(Builder builder) {
        super(builder.properties
                .durability(builder.maxUses > 1 ? builder.maxUses : 0)
                .setNoRepair());

        this.maxUses = builder.maxUses;
        this.healAmount = builder.healAmount;
        this.useDuration = builder.useDuration;
        this.cooldownTicks = builder.cooldownTicks;
        this.effects = List.copyOf(builder.effects);
        this.containerItem = builder.containerItem;
        this.foodProperties = builder.foodProperties;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return useDuration;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public boolean isEdible() {
        return true;
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties() {
        return foodProperties;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {

                if (foodProperties != null) {
                    player.getFoodData().eat(foodProperties.getNutrition(), foodProperties.getSaturationModifier());
                }

                if (healAmount != 0.0f) {
                    if (healAmount > 0) {
                        player.heal(healAmount);
                    } else {
                        player.hurt(level.damageSources().generic(), Math.abs(healAmount));
                    }
                }

                for (MobEffectInstance effect : effects) {
                    player.addEffect(new MobEffectInstance(effect));
                }

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        boolean canUse = player.canEat(foodProperties == null || foodProperties.canAlwaysEat());

        if (canUse) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }


    public static class Builder {
        private final Item.Properties properties;
        private int maxUses = 1;
        private float healAmount = 0.0f;
        private int useDuration = 32;
        private int cooldownTicks = 60;
        private final List<MobEffectInstance> effects = new ArrayList<>();
        private Item containerItem = null;
        private FoodProperties foodProperties = null;

        public Builder(Item.Properties properties) {
            this.properties = properties;
        }

        public Builder uses(int uses) {
            this.maxUses = uses;
            return this;
        }

        public Builder heal(float amount) {
            this.healAmount = amount;
            return this;
        }

        public Builder damage(float amount) {
            this.healAmount = -amount;
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

        public Builder effect(MobEffectInstance effect) {
            this.effects.add(effect);
            return this;
        }

        public Builder effects(MobEffectInstance... effects) {
            this.effects.addAll(List.of(effects));
            return this;
        }

        public Builder container(Item item) {
            this.containerItem = item;
            return this;
        }

        public Builder standardFood(FoodProperties food) {
            this.foodProperties = food;
            return this;
        }

        public Builder standardFood(int nutrition, float saturation) {
            this.foodProperties = (new FoodProperties.Builder())
                    .nutrition(nutrition)
                    .saturationMod(saturation)
                    .build();
            return this;
        }

        public CannedFood build() {
            return new CannedFood(this);
        }
    }
}