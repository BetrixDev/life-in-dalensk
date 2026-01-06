package dev.betrix.lifeindalensk.trader;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Complete data definition for a trader NPC.
 */
public class TraderData {

    private final String id;
    private final String name;
    private final Identifier skinTexture;
    private final List<TraderOffer> sellOffers;
    private final List<TraderBuyEntry> buyEntries;
    private final int restockTimeSeconds;

    public TraderData(String id, String name, Identifier skinTexture,
            List<TraderOffer> sellOffers, List<TraderBuyEntry> buyEntries,
            int restockTimeSeconds) {
        this.id = id;
        this.name = name;
        this.skinTexture = skinTexture;
        this.sellOffers = sellOffers;
        this.buyEntries = buyEntries;
        this.restockTimeSeconds = restockTimeSeconds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Identifier getSkinTexture() {
        return skinTexture;
    }

    public List<TraderOffer> getSellOffers() {
        return sellOffers;
    }

    public List<TraderBuyEntry> getBuyEntries() {
        return buyEntries;
    }

    public int getRestockTimeSeconds() {
        return restockTimeSeconds;
    }

    public long getRestockTimeTicks() {
        return restockTimeSeconds * 20L;
    }

    /**
     * Get the price this trader will pay for an item.
     * 
     * @return price in roubles, or -1 if trader doesn't buy this item
     */
    public long getBuyPriceFor(ItemStack stack) {
        for (TraderBuyEntry entry : buyEntries) {
            if (entry.matches(stack)) {
                return entry.getPrice();
            }
        }
        return -1L;
    }

    /**
     * Check if this trader will buy the given item stack.
     */
    public boolean willBuy(ItemStack stack) {
        for (TraderBuyEntry entry : buyEntries) {
            if (entry.matches(stack)) {
                return true;
            }
        }
        return false;
    }
}
