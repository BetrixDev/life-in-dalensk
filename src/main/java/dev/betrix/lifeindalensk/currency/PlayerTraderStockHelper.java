package dev.betrix.lifeindalensk.currency;

import dev.betrix.lifeindalensk.network.packet.SyncTraderStockS2CPacket;
import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side helper for manipulating player trader stock.
 */
public class PlayerTraderStockHelper {

    /**
     * Get the current stock for a trader offer.
     */
    public static int getStock(PlayerEntity player, String traderId, int offerIndex) {
        return PlayerTraderStockComponent.KEY.get(player).getStock(traderId, offerIndex);
    }

    /**
     * Decrement stock for a trader offer.
     */
    public static void decrementStock(PlayerEntity player, String traderId, int offerIndex, int amount) {
        PlayerTraderStockComponent.KEY.get(player).decrementStock(traderId, offerIndex, amount);
    }

    /**
     * Get the next restock time for a trader.
     */
    public static long getRestockTime(PlayerEntity player, String traderId) {
        return PlayerTraderStockComponent.KEY.get(player).getRestockTime(traderId);
    }

    /**
     * Sync trader stock to the client.
     */
    public static void syncStockToClient(ServerPlayerEntity player, String traderId) {
        TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
        if (trader == null)
            return;

        int[] stocksArray = PlayerTraderStockComponent.KEY.get(player).getAllStocks(traderId);
        long restockTime = getRestockTime(player, traderId);

        // Convert array to list for packet
        List<Integer> stocksList = new ArrayList<>();
        for (int stock : stocksArray) {
            stocksList.add(stock);
        }

        ServerPlayNetworking.send(player, new SyncTraderStockS2CPacket(traderId, stocksList, restockTime));
    }
}
