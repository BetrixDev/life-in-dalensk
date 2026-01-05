package dev.betrix.exclusioncraft.trader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks per-player stock levels and restock timers for all traders.
 */
public class PlayerTraderStock implements INBTSerializable<CompoundTag> {

    private final Map<String, TraderStockData> traderStocks = new HashMap<>();

    public TraderStockData getOrCreateTraderStock(String traderId) {
        return traderStocks.computeIfAbsent(traderId, id -> {
            TraderData trader = TraderRegistry.getInstance().getTrader(id);
            return new TraderStockData(trader);
        });
    }

    public int getStock(String traderId, int offerIndex) {
        TraderStockData stock = traderStocks.get(traderId);
        if (stock == null) {
            TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
            if (trader != null && offerIndex >= 0 && offerIndex < trader.getSellOffers().size()) {
                return trader.getSellOffers().get(offerIndex).getMaxStock();
            }
            return 0;
        }
        return stock.getStock(offerIndex);
    }

    public void decrementStock(String traderId, int offerIndex, int amount) {
        TraderStockData stock = getOrCreateTraderStock(traderId);
        stock.decrementStock(offerIndex, amount);
    }

    public long getRestockTime(String traderId) {
        TraderStockData stock = traderStocks.get(traderId);
        return stock != null ? stock.getNextRestockTime() : 0;
    }

    public void tick(long gameTime) {
        for (Map.Entry<String, TraderStockData> entry : traderStocks.entrySet()) {
            entry.getValue().tick(gameTime);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag tradersList = new ListTag();

        for (Map.Entry<String, TraderStockData> entry : traderStocks.entrySet()) {
            CompoundTag traderTag = new CompoundTag();
            traderTag.putString("TraderId", entry.getKey());
            traderTag.put("StockData", entry.getValue().serializeNBT());
            tradersList.add(traderTag);
        }

        tag.put("Traders", tradersList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        traderStocks.clear();
        ListTag tradersList = tag.getList("Traders", Tag.TAG_COMPOUND);

        for (int i = 0; i < tradersList.size(); i++) {
            CompoundTag traderTag = tradersList.getCompound(i);
            String traderId = traderTag.getString("TraderId");
            TraderStockData stockData = new TraderStockData(TraderRegistry.getInstance().getTrader(traderId));
            stockData.deserializeNBT(traderTag.getCompound("StockData"));
            traderStocks.put(traderId, stockData);
        }
    }

    /**
     * Stock data for a single trader.
     */
    public static class TraderStockData implements INBTSerializable<CompoundTag> {
        private final Map<Integer, Integer> stocks = new HashMap<>();
        private long nextRestockTime = 0;
        private final TraderData traderData;

        public TraderStockData(TraderData traderData) {
            this.traderData = traderData;
            initializeStocks();
        }

        private void initializeStocks() {
            if (traderData == null) return;
            for (int i = 0; i < traderData.getSellOffers().size(); i++) {
                stocks.put(i, traderData.getSellOffers().get(i).getMaxStock());
            }
        }

        public int getStock(int offerIndex) {
            if (traderData == null) return 0;
            return stocks.getOrDefault(offerIndex, 
                    offerIndex < traderData.getSellOffers().size() 
                            ? traderData.getSellOffers().get(offerIndex).getMaxStock() 
                            : 0);
        }

        public void decrementStock(int offerIndex, int amount) {
            int current = getStock(offerIndex);
            stocks.put(offerIndex, Math.max(0, current - amount));
        }

        public long getNextRestockTime() {
            return nextRestockTime;
        }

        public void setNextRestockTime(long time) {
            this.nextRestockTime = time;
        }

        public void tick(long gameTime) {
            if (traderData == null) return;
            
            if (nextRestockTime == 0) {
                nextRestockTime = gameTime + traderData.getRestockTimeTicks();
            } else if (gameTime >= nextRestockTime) {
                // Restock all items
                initializeStocks();
                nextRestockTime = gameTime + traderData.getRestockTimeTicks();
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            
            CompoundTag stocksTag = new CompoundTag();
            for (Map.Entry<Integer, Integer> entry : stocks.entrySet()) {
                stocksTag.putInt(String.valueOf(entry.getKey()), entry.getValue());
            }
            tag.put("Stocks", stocksTag);
            tag.putLong("NextRestock", nextRestockTime);
            
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            stocks.clear();
            CompoundTag stocksTag = tag.getCompound("Stocks");
            for (String key : stocksTag.getAllKeys()) {
                try {
                    stocks.put(Integer.parseInt(key), stocksTag.getInt(key));
                } catch (NumberFormatException ignored) {}
            }
            nextRestockTime = tag.getLong("NextRestock");
        }
    }
}
