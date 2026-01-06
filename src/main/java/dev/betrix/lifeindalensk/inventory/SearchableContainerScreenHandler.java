package dev.betrix.lifeindalensk.inventory;

import dev.betrix.lifeindalensk.block.entity.SearchableContainerBlockEntity;
import dev.betrix.lifeindalensk.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class SearchableContainerScreenHandler extends ScreenHandler {

    private final Inventory container;
    private final ScreenHandlerContext context;
    private final BlockPos containerPos;

    // Client constructor - used by ExtendedScreenHandlerType
    public SearchableContainerScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, new SimpleInventory(SearchableContainerBlockEntity.CONTAINER_SIZE),
                ScreenHandlerContext.EMPTY);
    }

    // Server constructor
    public SearchableContainerScreenHandler(int syncId, PlayerInventory playerInventory,
            Inventory container, ScreenHandlerContext context) {
        super(ModScreenHandlers.SEARCHABLE_CONTAINER, syncId);
        this.container = container;
        this.context = context;
        this.containerPos = BlockPos.ORIGIN;

        checkSize(container, SearchableContainerBlockEntity.CONTAINER_SIZE);

        // Container slots (9 slots in 3x3 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(container, row * 3 + col, 62 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory slots (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            // Container slots are 0-8
            if (slotIndex < 9) {
                // Move from container to player inventory (slots 9-44)
                if (!this.insertItem(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to container (slots 0-8)
                if (!this.insertItem(stackInSlot, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.container.canPlayerUse(player);
    }

    public Inventory getContainer() {
        return container;
    }

    public BlockPos getContainerPos() {
        return containerPos;
    }
}
