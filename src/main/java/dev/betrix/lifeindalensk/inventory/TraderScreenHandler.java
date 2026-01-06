package dev.betrix.lifeindalensk.inventory;

import dev.betrix.lifeindalensk.registry.ModScreenHandlers;
import dev.betrix.lifeindalensk.trader.TraderBuyEntry;
import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TraderScreenHandler extends ScreenHandler {

    public static final int SELL_SLOT_COUNT = 9;
    private static final int PLAYER_INV_START = SELL_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    // Packet codec for syncing trader menu data
    public static final PacketCodec<ByteBuf, TraderMenuData> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, TraderMenuData::traderId,
            PacketCodecs.VAR_INT, TraderMenuData::entityId,
            TraderMenuData::new
    );

    public record TraderMenuData(String traderId, int entityId) {}

    private final String traderId;
    private final int entityId;
    private final SimpleInventory sellContainer;
    private final Set<Identifier> buyableItems;
    private final Set<Identifier> buyableTags;

    // Client constructor - used by ExtendedScreenHandlerType
    public TraderScreenHandler(int syncId, PlayerInventory playerInventory, TraderMenuData data) {
        this(syncId, playerInventory, data.traderId(), data.entityId(),
                loadBuyableItemsFromRegistry(data.traderId()), loadBuyableTagsFromRegistry(data.traderId()));
    }

    // Server constructor
    public TraderScreenHandler(int syncId, PlayerInventory playerInventory, String traderId, int entityId) {
        this(syncId, playerInventory, traderId, entityId,
                loadBuyableItemsFromRegistry(traderId), loadBuyableTagsFromRegistry(traderId));
    }

    private static Set<Identifier> loadBuyableItemsFromRegistry(String traderId) {
        TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
        if (trader != null) {
            Set<Identifier> items = new HashSet<>();
            for (TraderBuyEntry entry : trader.getBuyEntries()) {
                if (!entry.isTagBased() && entry.getItemId() != null) {
                    items.add(entry.getItemId());
                }
            }
            return items;
        }
        return new HashSet<>();
    }

    private static Set<Identifier> loadBuyableTagsFromRegistry(String traderId) {
        TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
        if (trader != null) {
            Set<Identifier> tags = new HashSet<>();
            for (TraderBuyEntry entry : trader.getBuyEntries()) {
                if (entry.isTagBased() && entry.getTagId() != null) {
                    tags.add(entry.getTagId());
                }
            }
            return tags;
        }
        return new HashSet<>();
    }

    public TraderScreenHandler(int syncId, PlayerInventory playerInventory, String traderId, int entityId,
            Set<Identifier> buyableItems, Set<Identifier> buyableTags) {
        super(ModScreenHandlers.TRADER_MENU, syncId);
        this.traderId = traderId;
        this.entityId = entityId;
        this.sellContainer = new SimpleInventory(SELL_SLOT_COUNT);
        this.buyableItems = buyableItems;
        this.buyableTags = buyableTags;

        // Sell slots (slots 0-8) - 3x3 grid
        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            int row = i / 3;
            int col = i % 3;
            this.addSlot(new Slot(sellContainer, i, 8 + col * 18, 18 + row * 18) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return willBuyItem(stack);
                }
            });
        }

        // Player main inventory (slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar (slots 36-44)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        // Armor slots (slots 45-48)
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(playerInventory, 36 + (3 - i), 8, 8 + i * 18));
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

    public SimpleInventory getSellContainer() {
        return sellContainer;
    }

    public boolean willBuyItem(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        if (buyableItems.contains(itemId)) {
            return true;
        }

        for (Identifier tagId : buyableTags) {
            TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), tagId);
            if (stack.isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    public long calculateSellValue() {
        TraderData trader = getTraderData();
        if (trader == null)
            return 0;

        long total = 0;
        for (int i = 0; i < sellContainer.size(); i++) {
            ItemStack stack = sellContainer.getStack(i);
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
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getStack();
        ItemStack result = stackInSlot.copy();

        if (slotIndex < SELL_SLOT_COUNT) {
            // Move from sell slots to player inventory/hotbar
            if (!this.insertItem(stackInSlot, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < HOTBAR_END) {
            // From player inventory or hotbar
            if (willBuyItem(stackInSlot)) {
                if (!this.insertItem(stackInSlot, 0, SELL_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < PLAYER_INV_END) {
                // Move from main inventory to hotbar
                if (!this.insertItem(stackInSlot, PLAYER_INV_END, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from hotbar to main inventory
                if (!this.insertItem(stackInSlot, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            // Armor slots - move to main inventory
            if (!this.insertItem(stackInSlot, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return result;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        returnItemsToPlayer(player);
    }

    private void returnItemsToPlayer(PlayerEntity player) {
        if (player.getWorld().isClient)
            return;

        for (int i = 0; i < sellContainer.size(); i++) {
            ItemStack stack = sellContainer.getStack(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false);
                }
                sellContainer.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
