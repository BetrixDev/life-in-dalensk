package dev.betrix.exclusioncraft.inventory;

import dev.betrix.exclusioncraft.blocks.entity.SearchableContainerBlockEntity;
import dev.betrix.exclusioncraft.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public class SearchableContainerMenu extends AbstractContainerMenu {

    private final SearchableContainerBlockEntity container;
    private final BlockPos containerPos;

    public SearchableContainerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public SearchableContainerMenu(int containerId, Inventory playerInventory, SearchableContainerBlockEntity container) {
        super(ModMenuTypes.SEARCHABLE_CONTAINER.get(), containerId);
        this.container = container;
        this.containerPos = container.getBlockPos();

        // Container slots (9 slots in 3x3 grid) - these will be repositioned by the screen
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(container, row * 3 + col, 0, 0));
            }
        }

        // Player inventory slots (27 slots) - will be repositioned by the screen
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 0, 0));
            }
        }

        // Player hotbar (9 slots) - will be repositioned by the screen
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 0, 0));
        }

        // Armor slots (4 slots) - will be repositioned by the screen
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(playerInventory, 36 + (3 - i), 0, 0));
        }
    }

    private static SearchableContainerBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SearchableContainerBlockEntity searchable) {
            return searchable;
        }
        throw new IllegalStateException("Block entity is not a SearchableContainerBlockEntity at " + pos);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            // Container slots are 0-8
            if (slotIndex < 9) {
                // Move from container to player inventory (slots 9-44)
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to container (slots 0-8)
                if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return container.stillValid(player);
    }

    public SearchableContainerBlockEntity getContainer() {
        return container;
    }

    public BlockPos getContainerPos() {
        return containerPos;
    }
}
