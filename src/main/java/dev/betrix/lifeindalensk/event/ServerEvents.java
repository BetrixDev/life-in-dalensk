package dev.betrix.lifeindalensk.event;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.dimension.UndergroundDimensionManager;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerEvents {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerEvents.tickPlayer(player);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TraderRegistry.getInstance().registerDefaultTraders();
        });

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey().equals(UndergroundDimensionManager.UNDERGROUND_DIMENSION)) {
                UndergroundDimensionManager.initializeDimension(world);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.execute(() -> {
                UndergroundDimensionManager.teleportToSpawn(handler.getPlayer());
            });
        });
    }
}
