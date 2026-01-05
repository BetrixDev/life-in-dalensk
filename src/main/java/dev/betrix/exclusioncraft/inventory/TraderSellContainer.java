package dev.betrix.exclusioncraft.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Container for items the player wants to sell to a trader.
 * Extends SimpleContainer to track original slot positions for item return.
 */
public class TraderSellContainer extends SimpleContainer {

    private final int[] originalSlots;

    public TraderSellContainer(int size) {
        super(size);
        this.originalSlots = new int[size];
        for (int i = 0; i < size; i++) {
            originalSlots[i] = -1;
        }
    }

    public void setOriginalSlot(int containerSlot, int inventorySlot) {
        if (containerSlot >= 0 && containerSlot < originalSlots.length) {
            originalSlots[containerSlot] = inventorySlot;
        }
    }

    public int getOriginalSlot(int containerSlot) {
        if (containerSlot >= 0 && containerSlot < originalSlots.length) {
            return originalSlots[containerSlot];
        }
        return -1;
    }

    public void clearOriginalSlot(int containerSlot) {
        if (containerSlot >= 0 && containerSlot < originalSlots.length) {
            originalSlots[containerSlot] = -1;
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (stack.isEmpty()) {
            clearOriginalSlot(slot);
        }
    }
}
