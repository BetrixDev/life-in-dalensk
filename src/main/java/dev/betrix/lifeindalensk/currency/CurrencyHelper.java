package dev.betrix.lifeindalensk.currency;

import dev.betrix.lifeindalensk.network.packet.CurrencyChangeS2CPacket;
import dev.betrix.lifeindalensk.network.packet.SyncCurrencyS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server-side helper for manipulating player currency.
 * All methods should be called from the server side.
 */
public class CurrencyHelper {

    /**
     * Add roubles to a player and sync to client.
     * Shows floating +amount animation.
     */
    public static void addRoubles(ServerPlayerEntity player, long amount) {
        if (amount <= 0)
            return;

        PlayerCurrencyComponent currency = PlayerCurrencyComponent.KEY.get(player);
        currency.addRoubles(amount);
        syncToClient(player);
        ServerPlayNetworking.send(player, new CurrencyChangeS2CPacket(amount, true));
    }

    /**
     * Subtract roubles from a player and sync to client.
     * Shows floating -amount animation.
     *
     * @return true if the player had enough roubles
     */
    public static boolean subtractRoubles(ServerPlayerEntity player, long amount) {
        if (amount <= 0)
            return true;

        PlayerCurrencyComponent currency = PlayerCurrencyComponent.KEY.get(player);
        if (currency.subtractRoubles(amount)) {
            syncToClient(player);
            ServerPlayNetworking.send(player, new CurrencyChangeS2CPacket(amount, false));
            return true;
        }
        return false;
    }

    /**
     * Set the player's roubles to an exact amount and sync.
     * Does not show floating number animation.
     */
    public static void setRoubles(ServerPlayerEntity player, long amount) {
        PlayerCurrencyComponent currency = PlayerCurrencyComponent.KEY.get(player);
        currency.setRoubles(amount);
        syncToClient(player);
    }

    /**
     * Check if a player can afford a certain amount.
     */
    public static boolean canAfford(PlayerEntity player, long amount) {
        return getRoubles(player) >= amount;
    }

    /**
     * Get the player's current rouble count.
     */
    public static long getRoubles(PlayerEntity player) {
        return PlayerCurrencyComponent.KEY.get(player).getRoubles();
    }

    /**
     * Sync current currency to the client (without animation).
     */
    public static void syncToClient(ServerPlayerEntity player) {
        PlayerCurrencyComponent currency = PlayerCurrencyComponent.KEY.get(player);
        ServerPlayNetworking.send(player, new SyncCurrencyS2CPacket(currency.getRoubles()));
    }
}
