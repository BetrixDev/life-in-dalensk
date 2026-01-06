package dev.betrix.lifeindalensk.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.betrix.lifeindalensk.entity.TraderNPC;
import dev.betrix.lifeindalensk.registry.ModEntityTypes;
import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SummonTraderCommand {

    private static final SuggestionProvider<ServerCommandSource> TRADER_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(TraderRegistry.getInstance().getAllTraderIds(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("summon_trader")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("trader_id", StringArgumentType.string())
                        .suggests(TRADER_SUGGESTIONS)
                        .executes(SummonTraderCommand::summonTrader)));
    }

    private static int summonTrader(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String traderId = StringArgumentType.getString(context, "trader_id");

        TraderData traderData = TraderRegistry.getInstance().getTrader(traderId);
        if (traderData == null) {
            source.sendError(Text.literal("Unknown trader ID: " + traderId));
            return 0;
        }

        ServerWorld world = source.getWorld();
        Vec3d pos = source.getPosition();

        TraderNPC trader = ModEntityTypes.TRADER_NPC.create(world);
        if (trader != null) {
            trader.setTraderId(traderId);
            trader.refreshPositionAndAngles(pos.x, pos.y, pos.z, source.getRotation().y, 0.0F);
            world.spawnEntity(trader);

            source.sendFeedback(() -> Text.literal("Summoned trader: " + traderData.getName()), true);
            return 1;
        }

        source.sendError(Text.literal("Failed to create trader entity"));
        return 0;
    }
}
