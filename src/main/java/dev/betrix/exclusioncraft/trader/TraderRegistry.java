package dev.betrix.exclusioncraft.trader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Registry that loads and stores all trader definitions from JSON files.
 */
public class TraderRegistry extends SimplePreparableReloadListener<Map<String, TraderData>> {

    private static final Logger LOGGER = LogUtils.getLogger();
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
    @Nonnull
    protected Map<String, TraderData> prepare(@Nonnull ResourceManager resourceManager,
            @Nonnull ProfilerFiller profiler) {
        Map<String, TraderData> loadedTraders = new HashMap<>();
        String directory = TRADERS_PATH;

        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager
                .listResources(directory, path -> path.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                TraderData trader = parseTrader(json);
                if (trader != null) {
                    loadedTraders.put(trader.getId(), trader);
                    LOGGER.info("Loaded trader: {}", trader.getId());
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load trader from {}: {}", resourceLocation, e.getMessage());
            }
        }

        return loadedTraders;
    }

    @Override
    protected void apply(@Nonnull Map<String, TraderData> prepared, @Nonnull ResourceManager resourceManager,
            @Nonnull ProfilerFiller profiler) {
        this.traders = prepared;
        LOGGER.info("Loaded {} traders", traders.size());
    }

    private TraderData parseTrader(JsonObject json) {
        try {
            String id = json.get("id").getAsString();
            String name = json.get("name").getAsString();
            String skinPath = json.has("skin") ? json.get("skin").getAsString() : "textures/entity/steve.png";
            ResourceLocation skinTexture = skinPath.contains(":")
                    ? new ResourceLocation(skinPath)
                    : new ResourceLocation("minecraft", skinPath);

            List<TraderOffer> sellOffers = new ArrayList<>();
            if (json.has("sells")) {
                JsonArray sellsArray = json.getAsJsonArray("sells");
                for (JsonElement element : sellsArray) {
                    JsonObject offer = element.getAsJsonObject();
                    ResourceLocation itemId = new ResourceLocation(offer.get("item").getAsString());
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
                        ResourceLocation itemId = new ResourceLocation(buy.get("item").getAsString());
                        buyEntries.add(TraderBuyEntry.forItem(itemId, price));
                    } else if (buy.has("tag")) {
                        ResourceLocation tagId = new ResourceLocation(buy.get("tag").getAsString());
                        buyEntries.add(TraderBuyEntry.forTag(tagId, price));
                    } else {
                        LOGGER.warn("Buy entry missing both 'item' and 'tag' field, skipping");
                    }
                }
            }

            int restockTimeSeconds = json.has("restock_time") ? json.get("restock_time").getAsInt() : 3600;

            return new TraderData(id, name, skinTexture, sellOffers, buyEntries, restockTimeSeconds);
        } catch (Exception e) {
            LOGGER.error("Failed to parse trader JSON: {}", e.getMessage());
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
            LOGGER.info("No traders loaded from data packs, registering defaults");
        }
    }
}
