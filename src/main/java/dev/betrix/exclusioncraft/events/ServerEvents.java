package dev.betrix.exclusioncraft.events;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.trader.TraderRegistry;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(TraderRegistry.getInstance());
    }
}
