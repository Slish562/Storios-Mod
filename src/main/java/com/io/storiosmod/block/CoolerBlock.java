package com.io.storiosmod.block;

import com.io.storiosmod.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.ModList;
import net.minecraft.nbt.CompoundTag;

public class CoolerBlock extends Block {
    public CoolerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (!ModList.get().isLoaded("thirst")) {
            return InteractionResult.PASS;
        }

        boolean isGlassBottle = stack.is(Items.GLASS_BOTTLE);
        boolean isReusableEmpty = stack.is(ItemRegistry.REUSABLE_BOTTLE_EMPTY.get());

        if (!isGlassBottle && !isReusableEmpty) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack filledBottle;

        if (isGlassBottle) {
            filledBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        } else {
            filledBottle = new ItemStack(ItemRegistry.REUSABLE_BOTTLE.get());
        }

        CompoundTag tag = filledBottle.getOrCreateTag();
        CompoundTag thirstTag = new CompoundTag();
        thirstTag.putInt("purity", 3);
        tag.put("thirst", thirstTag);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, filledBottle);
            } else if (!player.getInventory().add(filledBottle)) {
                player.drop(filledBottle, false);
            }
        } else {
            player.getInventory().add(filledBottle);
        }

        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(1002, pos, 0);

        return InteractionResult.CONSUME;
    }
}