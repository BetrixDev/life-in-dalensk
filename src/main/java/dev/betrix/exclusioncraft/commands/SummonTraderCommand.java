package dev.betrix.exclusioncraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.betrix.exclusioncraft.entity.TraderNPC;
import dev.betrix.exclusioncraft.registry.ModEntityTypes;
import dev.betrix.exclusioncraft.trader.TraderData;
import dev.betrix.exclusioncraft.trader.TraderRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

public class SummonTraderCommand {

    private static final SuggestionProvider<CommandSourceStack> TRADER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(TraderRegistry.getInstance().getAllTraderIds(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("summon_trader")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("trader_id", StringArgumentType.string())
                        .suggests(TRADER_SUGGESTIONS)
                        .executes(SummonTraderCommand::summonTrader)));
    }

    private static int summonTrader(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String traderId = StringArgumentType.getString(context, "trader_id");

        TraderData traderData = TraderRegistry.getInstance().getTrader(traderId);
        if (traderData == null) {
            source.sendFailure(Component.literal("Unknown trader ID: " + traderId));
            return 0;
        }

        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        TraderNPC trader = ModEntityTypes.TRADER_NPC.get().create(level);
        if (trader != null) {
            trader.setTraderId(traderId);
            trader.moveTo(pos.x, pos.y, pos.z, source.getRotation().y, 0.0F);
            trader.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(pos)),
                    MobSpawnType.COMMAND, null, null);
            level.addFreshEntity(trader);

            source.sendSuccess(() -> Component.literal("Summoned trader: " + traderData.getName()), true);
            return 1;
        }

        source.sendFailure(Component.literal("Failed to create trader entity"));
        return 0;
    }
}
