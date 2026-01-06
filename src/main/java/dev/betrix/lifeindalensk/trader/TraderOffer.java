package dev.betrix.lifeindalensk.trader;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Represents an item a trader can sell.
 */
public class TraderOffer {

    private final Identifier itemId;
    private final long price;
    private final int maxStock;

    public TraderOffer(Identifier itemId, long price, int maxStock) {
        this.itemId = itemId;
        this.price = price;
        this.maxStock = maxStock;
    }

    public Identifier getItemId() {
        return itemId;
    }

    public Item getItem() {
        return Registries.ITEM.get(itemId);
    }

    public long getPrice() {
        return price;
    }

    public int getMaxStock() {
        return maxStock;
    }
}
