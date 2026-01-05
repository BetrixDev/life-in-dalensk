package dev.betrix.exclusioncraft.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side cache for trader stock data received from the server.
 */
public class ClientTraderStockCache {

    private static final Map<String, TraderStockInfo> stockCache = new HashMap<>();

    public static void setStock(String traderId, int[] stocks, long restockTime) {
        stockCache.put(traderId, new TraderStockInfo(stocks, restockTime));
    }

    public static int getStock(String traderId, int offerIndex) {
        TraderStockInfo info = stockCache.get(traderId);
        if (info == null || offerIndex < 0 || offerIndex >= info.stocks.length) {
            return -1; // Unknown, will show "?"
        }
        return info.stocks[offerIndex];
    }

    public static long getRestockTime(String traderId) {
        TraderStockInfo info = stockCache.get(traderId);
        return info != null ? info.restockTime : 0;
    }

    public static void clear() {
        stockCache.clear();
    }

    public static void clearTrader(String traderId) {
        stockCache.remove(traderId);
    }

    private static class TraderStockInfo {
        final int[] stocks;
        final long restockTime;

        TraderStockInfo(int[] stocks, long restockTime) {
            this.stocks = stocks;
            this.restockTime = restockTime;
        }
    }
}
