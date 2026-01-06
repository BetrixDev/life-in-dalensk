package dev.betrix.lifeindalensk.trader;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a buy entry that can match either a specific item or a tag.
 */
public class TraderBuyEntry {

    @Nullable
    private final Identifier itemId;
    @Nullable
    private final Identifier tagId;
    private final long price;

    private TraderBuyEntry(@Nullable Identifier itemId, @Nullable Identifier tagId, long price) {
        this.itemId = itemId;
        this.tagId = tagId;
        this.price = price;
    }

    public static TraderBuyEntry forItem(Identifier itemId, long price) {
        return new TraderBuyEntry(itemId, null, price);
    }

    public static TraderBuyEntry forTag(Identifier tagId, long price) {
        return new TraderBuyEntry(null, tagId, price);
    }

    public boolean isTagBased() {
        return tagId != null;
    }

    @Nullable
    public Identifier getItemId() {
        return itemId;
    }

    @Nullable
    public Identifier getTagId() {
        return tagId;
    }

    public long getPrice() {
        return price;
    }

    /**
     * Check if this entry matches the given item.
     */
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        if (itemId != null) {
            Identifier stackItemId = Registries.ITEM.getId(stack.getItem());
            return itemId.equals(stackItemId);
        }

        if (tagId != null) {
            TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), tagId);
            return stack.isIn(tag);
        }

        return false;
    }

    /**
     * Check if this entry matches the given item ID (for exact item matches only).
     */
    public boolean matchesItemId(Identifier checkItemId) {
        if (itemId != null) {
            return itemId.equals(checkItemId);
        }

        if (tagId != null) {
            Item item = Registries.ITEM.get(checkItemId);
            if (item != null) {
                TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), tagId);
                return new ItemStack(item).isIn(tag);
            }
        }

        return false;
    }
}
