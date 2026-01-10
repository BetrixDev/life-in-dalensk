package dev.betrix.lifeindalensk.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.betrix.lifeindalensk.extraction.PlayerExtractionComponent;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Command for testing the extraction system.
 * Allows manually starting extraction countdown for testing.
 */
public class ExtractionTestCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("extraction")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("start")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    PlayerExtractionComponent extraction = PlayerExtractionComponent.KEY.get(target);
                                    extraction.startExtraction(target.getBlockPos());
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Started extraction for " + target.getName().getString()),
                                            true);
                                    return 1;
                                })))
                .then(CommandManager.literal("cancel")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    PlayerExtractionComponent extraction = PlayerExtractionComponent.KEY.get(target);
                                    extraction.cancelExtraction();
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Canceled extraction for " + target.getName().getString()),
                                            true);
                                    return 1;
                                })))
                .then(CommandManager.literal("status")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    PlayerExtractionComponent extraction = PlayerExtractionComponent.KEY.get(target);
                                    if (extraction.isExtracting()) {
                                        int ticks = extraction.getExtractionTicks();
                                        double seconds = ticks / 20.0;
                                        context.getSource().sendFeedback(
                                                () -> Text.literal(target.getName().getString() + " is extracting: " 
                                                        + String.format("%.1f", seconds) + " seconds remaining"),
                                                false);
                                    } else {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal(target.getName().getString() + " is not extracting"),
                                                false);
                                    }
                                    return 1;
                                }))));
    }
}
