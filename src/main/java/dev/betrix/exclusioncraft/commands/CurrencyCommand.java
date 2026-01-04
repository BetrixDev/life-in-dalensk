package dev.betrix.exclusioncraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import dev.betrix.exclusioncraft.currency.CurrencyHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CurrencyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("roubles")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            CurrencyHelper.addRoubles(target, amount);
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("Added ₽" + amount + " to " + target.getName().getString()), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            if (CurrencyHelper.subtractRoubles(target, amount)) {
                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("Removed ₽" + amount + " from " + target.getName().getString()), true);
                                            } else {
                                                context.getSource().sendFailure(
                                                        Component.literal(target.getName().getString() + " doesn't have enough roubles!"));
                                            }
                                            return 1;
                                        }))))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            CurrencyHelper.setRoubles(target, amount);
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("Set " + target.getName().getString() + "'s roubles to ₽" + amount), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("check")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    long roubles = CurrencyHelper.getRoubles(target);
                                    context.getSource().sendSuccess(() ->
                                            Component.literal(target.getName().getString() + " has ₽" + roubles), false);
                                    return 1;
                                }))));
    }
}
