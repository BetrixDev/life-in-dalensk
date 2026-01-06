package dev.betrix.lifeindalensk.event;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerEvents {

    public static void register() {
        // Player tick event for broken leg sprint damage
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerEvents.tickPlayer(player);
            }
        });
    }
}
