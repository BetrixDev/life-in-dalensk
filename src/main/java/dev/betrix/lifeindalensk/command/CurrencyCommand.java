package dev.betrix.lifeindalensk.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import dev.betrix.lifeindalensk.currency.CurrencyHelper;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CurrencyCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("roubles")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            CurrencyHelper.addRoubles(target, amount);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Added " + amount + " roubles to " + target.getName().getString()),
                                                    true);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            if (CurrencyHelper.subtractRoubles(target, amount)) {
                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Removed " + amount + " roubles from " + target.getName().getString()),
                                                        true);
                                            } else {
                                                context.getSource().sendError(
                                                        Text.literal(target.getName().getString() + " doesn't have enough roubles!"));
                                            }
                                            return 1;
                                        }))))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(0))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            CurrencyHelper.setRoubles(target, amount);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Set " + target.getName().getString() + "'s roubles to " + amount),
                                                    true);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("check")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    long roubles = CurrencyHelper.getRoubles(target);
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(target.getName().getString() + " has " + roubles + " roubles"),
                                            false);
                                    return 1;
                                }))));
    }
}
