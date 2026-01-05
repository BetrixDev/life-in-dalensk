package dev.betrix.exclusioncraft.inventory;

import dev.betrix.exclusioncraft.registry.ModMenuTypes;
import dev.betrix.exclusioncraft.trader.PlayerTraderStockProvider;
import dev.betrix.exclusioncraft.trader.TraderBuyEntry;
import dev.betrix.exclusioncraft.trader.TraderData;
import dev.betrix.exclusioncraft.trader.TraderEvents;
import dev.betrix.exclusioncraft.trader.TraderRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Menu for trader interactions. Manages sell slots that allow players
 * to place items they want to sell, with proper item return on close.
 */
public class TraderMenu extends AbstractContainerMenu {

    public static final int SELL_SLOT_COUNT = 9;
    private static final int PLAYER_INV_START = SELL_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final String traderId;
    private final int entityId;
    private final TraderSellContainer sellContainer;
    private final Set<ResourceLocation> buyableItems;
    private final Set<ResourceLocation> buyableTags;

    public TraderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readUtf(), extraData.readVarInt(), 
             readResourceLocations(extraData), readResourceLocations(extraData));
    }

    private static Set<ResourceLocation> readResourceLocations(FriendlyByteBuf buf) {
        Set<ResourceLocation> result = new HashSet<>();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            result.add(buf.readResourceLocation());
        }
        return result;
    }

    public TraderMenu(int containerId, Inventory playerInventory, String traderId, int entityId) {
        this(containerId, playerInventory, traderId, entityId, 
             loadBuyableItemsFromRegistry(traderId), loadBuyableTagsFromRegistry(traderId));
    }

    private static Set<ResourceLocation> loadBuyableItemsFromRegistry(String traderId) {
        TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
        if (trader != null) {
            Set<ResourceLocation> items = new HashSet<>();
            for (TraderBuyEntry entry : trader.getBuyEntries()) {
                if (!entry.isTagBased() && entry.getItemId() != null) {
                    items.add(entry.getItemId());
                }
            }
            return items;
        }
        return new HashSet<>();
    }

    private static Set<ResourceLocation> loadBuyableTagsFromRegistry(String traderId) {
        TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
        if (trader != null) {
            Set<ResourceLocation> tags = new HashSet<>();
            for (TraderBuyEntry entry : trader.getBuyEntries()) {
                if (entry.isTagBased() && entry.getTagId() != null) {
                    tags.add(entry.getTagId());
                }
            }
            return tags;
        }
        return new HashSet<>();
    }

    public TraderMenu(int containerId, Inventory playerInventory, String traderId, int entityId, 
                      Set<ResourceLocation> buyableItems, Set<ResourceLocation> buyableTags) {
        super(ModMenuTypes.TRADER_MENU.get(), containerId);
        this.traderId = traderId;
        this.entityId = entityId;
        this.sellContainer = new TraderSellContainer(SELL_SLOT_COUNT);
        this.buyableItems = buyableItems;
        this.buyableTags = buyableTags;

        // Initialize player's stock for this trader and sync to client
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            // Ensure stock data is initialized
            serverPlayer.getCapability(PlayerTraderStockProvider.PLAYER_TRADER_STOCK).ifPresent(stock -> {
                stock.getOrCreateTraderStock(traderId);
                stock.tick(serverPlayer.level().getGameTime());
            });
            // Sync stock to client
            TraderEvents.syncStockToClient(serverPlayer, traderId);
        }

        // Sell slots (slots 0-8) - positions will be set by screen
        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            this.addSlot(new Slot(sellContainer, i, 0, 0) {
                @Override
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    return willBuyItem(stack);
                }
            });
        }

        // Player main inventory (slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 0, 0));
            }
        }

        // Player hotbar (slots 36-44)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 0, 0));
        }

        // Armor slots (slots 45-48)
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(playerInventory, 36 + (3 - i), 0, 0));
        }
    }

    public String getTraderId() {
        return traderId;
    }

    public int getEntityId() {
        return entityId;
    }

    public TraderData getTraderData() {
        return TraderRegistry.getInstance().getTrader(traderId);
    }

    public TraderSellContainer getSellContainer() {
        return sellContainer;
    }

    /**
     * Check if this trader will buy the given item stack.
     * Uses the cached buyable items and tags lists which work on both client and server.
     */
    public boolean willBuyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && buyableItems.contains(itemId)) {
            return true;
        }
        
        for (ResourceLocation tagId : buyableTags) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
            if (stack.is(tag)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Calculate the total value of items in the sell container.
     */
    public long calculateSellValue() {
        TraderData trader = getTraderData();
        if (trader == null) return 0;

        long total = 0;
        for (int i = 0; i < sellContainer.getContainerSize(); i++) {
            ItemStack stack = sellContainer.getItem(i);
            if (!stack.isEmpty()) {
                long price = trader.getBuyPriceFor(stack);
                if (price > 0) {
                    total += price * stack.getCount();
                }
            }
        }
        return total;
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (slotIndex < SELL_SLOT_COUNT) {
            // Move from sell slots to player inventory/hotbar
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < HOTBAR_END) {
            // From player inventory or hotbar
            // Try to move to sell slots if trader will buy
            if (willBuyItem(stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, 0, SELL_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < PLAYER_INV_END) {
                // Move from main inventory to hotbar
                if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_END, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from hotbar to main inventory
                if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            // Armor slots (45-48) - move to main inventory
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return result;
    }

    @Override
    public void removed(@Nonnull Player player) {
        super.removed(player);
        returnItemsToPlayer(player);
    }

    /**
     * Returns all items in the sell container back to the player's inventory.
     */
    private void returnItemsToPlayer(Player player) {
        if (player.level().isClientSide) return;

        for (int i = 0; i < sellContainer.getContainerSize(); i++) {
            ItemStack stack = sellContainer.getItem(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                sellContainer.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }
}
