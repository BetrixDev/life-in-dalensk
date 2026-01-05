package dev.betrix.exclusioncraft.trader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Represents an item a trader can buy or sell.
 */
public class TraderOffer {

    private final ResourceLocation itemId;
    private final long price;
    private final int maxStock;

    public TraderOffer(ResourceLocation itemId, long price, int maxStock) {
        this.itemId = itemId;
        this.price = price;
        this.maxStock = maxStock;
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public Item getItem() {
        return ForgeRegistries.ITEMS.getValue(itemId);
    }

    public long getPrice() {
        return price;
    }

    public int getMaxStock() {
        return maxStock;
    }
}
