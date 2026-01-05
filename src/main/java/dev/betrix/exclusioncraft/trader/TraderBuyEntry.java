package dev.betrix.exclusioncraft.trader;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Represents a buy entry that can match either a specific item or a tag.
 */
public class TraderBuyEntry {

    @Nullable
    private final ResourceLocation itemId;
    @Nullable
    private final ResourceLocation tagId;
    private final long price;

    private TraderBuyEntry(@Nullable ResourceLocation itemId, @Nullable ResourceLocation tagId, long price) {
        this.itemId = itemId;
        this.tagId = tagId;
        this.price = price;
    }

    public static TraderBuyEntry forItem(ResourceLocation itemId, long price) {
        return new TraderBuyEntry(itemId, null, price);
    }

    public static TraderBuyEntry forTag(ResourceLocation tagId, long price) {
        return new TraderBuyEntry(null, tagId, price);
    }

    public boolean isTagBased() {
        return tagId != null;
    }

    @Nullable
    public ResourceLocation getItemId() {
        return itemId;
    }

    @Nullable
    public ResourceLocation getTagId() {
        return tagId;
    }

    public long getPrice() {
        return price;
    }

    /**
     * Check if this entry matches the given item.
     */
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        if (itemId != null) {
            ResourceLocation stackItemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return itemId.equals(stackItemId);
        }
        
        if (tagId != null) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
            return stack.is(tag);
        }
        
        return false;
    }

    /**
     * Check if this entry matches the given item ID (for exact item matches only).
     */
    public boolean matchesItemId(ResourceLocation checkItemId) {
        if (itemId != null) {
            return itemId.equals(checkItemId);
        }
        
        if (tagId != null) {
            Item item = ForgeRegistries.ITEMS.getValue(checkItemId);
            if (item != null) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                return new ItemStack(item).is(tag);
            }
        }
        
        return false;
    }
}
