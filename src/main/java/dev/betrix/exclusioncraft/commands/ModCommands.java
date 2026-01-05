package dev.betrix.exclusioncraft.commands;

import dev.betrix.exclusioncraft.ExclusionCraft;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CurrencyCommand.register(event.getDispatcher());
        SummonTraderCommand.register(event.getDispatcher());
    }
}
