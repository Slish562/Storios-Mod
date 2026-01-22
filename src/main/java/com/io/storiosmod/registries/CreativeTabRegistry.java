package com.yourmod.block;  // Замени на свой пакет

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

        // Проверяем, загружен ли Thirst Was Taken и в руке стеклянная бутылочка
        if (!ModList.get().isLoaded("thirst") || !stack.is(Items.GLASS_BOTTLE)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        // Создаём бутылку с водой (vanilla water potion)
        ItemStack pureWaterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);

        // Добавляем NBT для чистой воды из Thirst Was Taken
        CompoundTag tag = pureWaterBottle.getOrCreateTag();
        CompoundTag thirstTag = new CompoundTag();
        thirstTag.putInt("purity", 3);  // 3 — максимум, pure water (проверь в игре, может быть 4)
        tag.put("thirst", thirstTag);

        // Выдаём игроку (стандартная логика для fill)
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, pureWaterBottle);
            } else if (!player.getInventory().add(pureWaterBottle)) {
                player.drop(pureWaterBottle, false);
            }
        } else {
            player.getInventory().add(pureWaterBottle);
        }

        // Звук наполнения бутылки и частицы
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(1002, pos, 0);  // Частицы (опционально, можно подобрать другой)

        return InteractionResult.CONSUME;
    }
}