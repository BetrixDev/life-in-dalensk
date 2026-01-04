package dev.betrix.exclusioncraft.currency;

import dev.betrix.exclusioncraft.network.ModNetworking;
import dev.betrix.exclusioncraft.network.packets.CurrencyChangePacket;
import dev.betrix.exclusioncraft.network.packets.SyncCurrencyPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side helper for manipulating player currency.
 * All methods should be called from the server side.
 */
public class CurrencyHelper {

    /**
     * Add roubles to a player and sync to client.
     * Shows floating +amount animation.
     */
    public static void addRoubles(ServerPlayer player, long amount) {
        if (amount <= 0)
            return;

        player.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(currency -> {
            currency.addRoubles(amount);
            syncToClient(player);
            ModNetworking.sendToPlayer(new CurrencyChangePacket(amount, true), player);
        });
    }

    /**
     * Subtract roubles from a player and sync to client.
     * Shows floating -amount animation.
     * 
     * @return true if the player had enough roubles
     */
    public static boolean subtractRoubles(ServerPlayer player, long amount) {
        if (amount <= 0)
            return true;

        boolean[] success = { false };
        player.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(currency -> {
            if (currency.subtractRoubles(amount)) {
                success[0] = true;
                syncToClient(player);
                ModNetworking.sendToPlayer(new CurrencyChangePacket(amount, false), player);
            }
        });
        return success[0];
    }

    /**
     * Set the player's roubles to an exact amount and sync.
     * Does not show floating number animation.
     */
    public static void setRoubles(ServerPlayer player, long amount) {
        player.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(currency -> {
            currency.setRoubles(amount);
            syncToClient(player);
        });
    }

    /**
     * Check if a player can afford a certain amount.
     */
    public static boolean canAfford(Player player, long amount) {
        return PlayerCurrencyProvider.getRoubles(player) >= amount;
    }

    /**
     * Get the player's current rouble count.
     */
    public static long getRoubles(Player player) {
        return PlayerCurrencyProvider.getRoubles(player);
    }

    private static void syncToClient(ServerPlayer player) {
        player.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(currency -> {
            ModNetworking.sendToPlayer(new SyncCurrencyPacket(currency.getRoubles()), player);
        });
    }
}
