package dev.betrix.lifeindalensk.trader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.betrix.lifeindalensk.LifeInDalensk;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Registry that loads and stores all trader definitions from JSON files.
 */
public class TraderRegistry implements SimpleSynchronousResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final String TRADERS_PATH = "traders";

    private static TraderRegistry instance;
    private Map<String, TraderData> traders = new HashMap<>();

    public static TraderRegistry getInstance() {
        if (instance == null) {
            instance = new TraderRegistry();
        }
        return instance;
    }

    private TraderRegistry() {
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(LifeInDalensk.MOD_ID, "traders");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<String, TraderData> loadedTraders = new HashMap<>();

        for (Map.Entry<Identifier, Resource> entry : manager
                .findResources(TRADERS_PATH, path -> path.getPath().endsWith(".json")).entrySet()) {
            Identifier resourceLocation = entry.getKey();
            try (BufferedReader reader = entry.getValue().getReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                TraderData trader = parseTrader(json);
                if (trader != null) {
                    loadedTraders.put(trader.getId(), trader);
                    LifeInDalensk.LOGGER.info("Loaded trader: {}", trader.getId());
                }
            } catch (IOException e) {
                LifeInDalensk.LOGGER.error("Failed to load trader from {}: {}", resourceLocation, e.getMessage());
            }
        }

        this.traders = loadedTraders;
        LifeInDalensk.LOGGER.info("Loaded {} traders", traders.size());
    }

    private TraderData parseTrader(JsonObject json) {
        try {
            String id = json.get("id").getAsString();
            String name = json.get("name").getAsString();
            String skinPath = json.has("skin") ? json.get("skin").getAsString() : "textures/entity/steve.png";
            Identifier skinTexture = skinPath.contains(":")
                    ? Identifier.of(skinPath)
                    : Identifier.ofVanilla(skinPath);

            List<TraderOffer> sellOffers = new ArrayList<>();
            if (json.has("sells")) {
                JsonArray sellsArray = json.getAsJsonArray("sells");
                for (JsonElement element : sellsArray) {
                    JsonObject offer = element.getAsJsonObject();
                    Identifier itemId = Identifier.of(offer.get("item").getAsString());
                    long price = offer.get("price").getAsLong();
                    int maxStock = offer.has("max_stock") ? offer.get("max_stock").getAsInt() : 64;
                    sellOffers.add(new TraderOffer(itemId, price, maxStock));
                }
            }

            List<TraderBuyEntry> buyEntries = new ArrayList<>();
            if (json.has("buys")) {
                JsonArray buysArray = json.getAsJsonArray("buys");
                for (JsonElement element : buysArray) {
                    JsonObject buy = element.getAsJsonObject();
                    long price = buy.get("price").getAsLong();

                    if (buy.has("item")) {
                        Identifier itemId = Identifier.of(buy.get("item").getAsString());
                        buyEntries.add(TraderBuyEntry.forItem(itemId, price));
                    } else if (buy.has("tag")) {
                        Identifier tagId = Identifier.of(buy.get("tag").getAsString());
                        buyEntries.add(TraderBuyEntry.forTag(tagId, price));
                    } else {
                        LifeInDalensk.LOGGER.warn("Buy entry missing both 'item' and 'tag' field, skipping");
                    }
                }
            }

            int restockTimeSeconds = json.has("restock_time") ? json.get("restock_time").getAsInt() : 3600;

            return new TraderData(id, name, skinTexture, sellOffers, buyEntries, restockTimeSeconds);
        } catch (Exception e) {
            LifeInDalensk.LOGGER.error("Failed to parse trader JSON: {}", e.getMessage());
            return null;
        }
    }

    public TraderData getTrader(String id) {
        return traders.get(id);
    }

    public Collection<String> getAllTraderIds() {
        return traders.keySet();
    }

    public boolean hasTrader(String id) {
        return traders.containsKey(id);
    }

    public void registerDefaultTraders() {
        // Fallback traders if no JSON files are loaded
        if (traders.isEmpty()) {
            LifeInDalensk.LOGGER.info("No traders loaded from data packs, registering defaults");
        }
    }
}
