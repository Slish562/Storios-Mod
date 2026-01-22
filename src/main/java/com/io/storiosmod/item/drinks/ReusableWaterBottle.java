package com.io.storiosmod.item.food;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

public class ReusableWaterBottle extends Item {
    private final Item emptyContainer;

    public ReusableWaterBottle(Item.Properties properties, Item emptyContainer) {
        super(properties);
        this.emptyContainer = emptyContainer;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player) entity : null;

        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, stack);
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        if (player == null || !player.getAbilities().instabuild) {
            if (stack.isEmpty()) {
                return new ItemStack(emptyContainer);
            }
            if (player != null) {
                player.getInventory().add(new ItemStack(emptyContainer));
            }
            return stack;
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public boolean isEdible() {
        return true;
    }
}