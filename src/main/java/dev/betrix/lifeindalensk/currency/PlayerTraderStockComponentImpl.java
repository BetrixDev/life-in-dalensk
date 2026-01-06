package dev.betrix.lifeindalensk.currency;

import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of PlayerTraderStockComponent.
 * Tracks per-player stock levels and restock timers for all traders.
 */
public class PlayerTraderStockComponentImpl implements PlayerTraderStockComponent {

    private final PlayerEntity player;
    private final Map<String, TraderStockData> traderStocks = new HashMap<>();

    public PlayerTraderStockComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
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

    @Override
    public void decrementStock(String traderId, int offerIndex, int amount) {
        TraderStockData stock = getOrCreateTraderStock(traderId);
        stock.decrementStock(offerIndex, amount);
    }

    @Override
    public long getRestockTime(String traderId) {
        TraderStockData stock = traderStocks.get(traderId);
        return stock != null ? stock.getNextRestockTime() : 0;
    }

    @Override
    public int[] getAllStocks(String traderId) {
        TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
        if (trader == null) {
            return new int[0];
        }

        int[] stocks = new int[trader.getSellOffers().size()];
        for (int i = 0; i < stocks.length; i++) {
            stocks[i] = getStock(traderId, i);
        }
        return stocks;
    }

    private TraderStockData getOrCreateTraderStock(String traderId) {
        return traderStocks.computeIfAbsent(traderId, id -> {
            TraderData trader = TraderRegistry.getInstance().getTrader(id);
            return new TraderStockData(trader);
        });
    }

    @Override
    public void serverTick() {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            long gameTime = serverWorld.getTime();
            for (Map.Entry<String, TraderStockData> entry : traderStocks.entrySet()) {
                entry.getValue().tick(gameTime);
            }
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        traderStocks.clear();
        NbtList tradersList = tag.getList("Traders", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < tradersList.size(); i++) {
            NbtCompound traderTag = tradersList.getCompound(i);
            String traderId = traderTag.getString("TraderId");
            TraderStockData stockData = new TraderStockData(TraderRegistry.getInstance().getTrader(traderId));
            stockData.readFromNbt(traderTag.getCompound("StockData"));
            traderStocks.put(traderId, stockData);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList tradersList = new NbtList();

        for (Map.Entry<String, TraderStockData> entry : traderStocks.entrySet()) {
            NbtCompound traderTag = new NbtCompound();
            traderTag.putString("TraderId", entry.getKey());
            traderTag.put("StockData", entry.getValue().writeToNbt());
            tradersList.add(traderTag);
        }

        tag.put("Traders", tradersList);
    }

    /**
     * Stock data for a single trader.
     */
    public static class TraderStockData {
        private final Map<Integer, Integer> stocks = new HashMap<>();
        private long nextRestockTime = 0;
        private final TraderData traderData;

        public TraderStockData(TraderData traderData) {
            this.traderData = traderData;
            initializeStocks();
        }

        private void initializeStocks() {
            if (traderData == null)
                return;
            for (int i = 0; i < traderData.getSellOffers().size(); i++) {
                stocks.put(i, traderData.getSellOffers().get(i).getMaxStock());
            }
        }

        public int getStock(int offerIndex) {
            if (traderData == null)
                return 0;
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

        public void tick(long gameTime) {
            if (traderData == null)
                return;

            if (nextRestockTime == 0) {
                nextRestockTime = gameTime + traderData.getRestockTimeTicks();
            } else if (gameTime >= nextRestockTime) {
                // Restock all items
                initializeStocks();
                nextRestockTime = gameTime + traderData.getRestockTimeTicks();
            }
        }

        public void readFromNbt(NbtCompound tag) {
            stocks.clear();
            NbtCompound stocksTag = tag.getCompound("Stocks");
            for (String key : stocksTag.getKeys()) {
                try {
                    stocks.put(Integer.parseInt(key), stocksTag.getInt(key));
                } catch (NumberFormatException ignored) {
                }
            }
            nextRestockTime = tag.getLong("NextRestock");
        }

        public NbtCompound writeToNbt() {
            NbtCompound tag = new NbtCompound();

            NbtCompound stocksTag = new NbtCompound();
            for (Map.Entry<Integer, Integer> entry : stocks.entrySet()) {
                stocksTag.putInt(String.valueOf(entry.getKey()), entry.getValue());
            }
            tag.put("Stocks", stocksTag);
            tag.putLong("NextRestock", nextRestockTime);

            return tag;
        }
    }
}
