package dev.betrix.lifeindalensk.client;

/**
 * Client-side storage for the player's currency data.
 * Synced from server via SyncCurrencyS2CPacket.
 */
public class ClientCurrencyData {

    private static long roubles = 0;

    public static long getRoubles() {
        return roubles;
    }

    public static void setRoubles(long amount) {
        roubles = Math.max(0, amount);
    }

    public static void clear() {
        roubles = 0;
    }
}
