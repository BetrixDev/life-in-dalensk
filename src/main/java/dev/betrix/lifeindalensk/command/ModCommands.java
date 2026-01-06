package dev.betrix.lifeindalensk.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class ModCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        CurrencyCommand.register(dispatcher);
        SummonTraderCommand.register(dispatcher);
    }
}
