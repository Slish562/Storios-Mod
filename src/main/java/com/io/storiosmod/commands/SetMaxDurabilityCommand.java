package com.io.storiosmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SetMaxDurabilityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setmaxdamage")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(SetMaxDurabilityCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            int amount = IntegerArgumentType.getInteger(context, "amount");
            ItemStack item = context.getSource().getPlayerOrException().getMainHandItem();

            if (item.isEmpty()) {
                context.getSource().sendFailure(Component.literal("Hold an item in your main hand!"));
                return 0;
            }

            CompoundTag tag = item.getOrCreateTag();
            tag.putInt("storiosmod:max_damage", amount);
            item.setTag(tag);

            context.getSource().sendSuccess(() -> Component.literal("Max durability set to " + amount), true);
            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
