package dev.betrix.exclusioncraft.client.gui;

import dev.betrix.exclusioncraft.inventory.SearchableContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;

public class SearchableContainerScreen extends AbstractContainerScreen<SearchableContainerMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int PLAYER_RENDER_WIDTH = 51;
    private static final int PLAYER_RENDER_HEIGHT = 72;
    private static final int PANEL_GAP = 4;
    private static final int PANEL_BG_COLOR = 0x40000000;

    private int leftColumnX;
    private int middleColumnX;
    private int rightColumnX;
    private int columnWidth;

    private float xMouse;
    private float yMouse;

    public SearchableContainerScreen(SearchableContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageWidth = this.width;
        this.imageHeight = this.height;
        this.leftPos = 0;
        this.topPos = 0;

        super.init();

        columnWidth = this.width / 3;
        leftColumnX = 0;
        middleColumnX = columnWidth;
        rightColumnX = columnWidth * 2;

        repositionSlots();
    }

    private void repositionSlots() {
        int playerCenterX = leftColumnX + columnWidth / 2;
        int playerCenterY = this.height / 2;

        // Container slots (0-8) - right column, 3x3 grid
        int containerWidth = 3 * SLOT_SIZE;
        int containerStartX = rightColumnX + (columnWidth - containerWidth) / 2;
        int containerStartY = this.height / 2 - (3 * SLOT_SIZE) / 2;

        for (int i = 0; i < 9; i++) {
            Slot slot = this.menu.slots.get(i);
            int row = i / 3;
            int col = i % 3;
            slot.x = containerStartX + col * SLOT_SIZE - this.leftPos + 1;
            slot.y = containerStartY + row * SLOT_SIZE - this.topPos + 1;
        }

        // Player inventory slots (9-35) - middle column
        int gridWidth = 9 * SLOT_SIZE;
        int gridStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int gridStartY = this.height / 2 - (3 * SLOT_SIZE) / 2 - SLOT_SIZE;

        for (int i = 9; i < 36; i++) {
            Slot slot = this.menu.slots.get(i);
            int invIndex = i - 9;
            int row = invIndex / 9;
            int col = invIndex % 9;
            slot.x = gridStartX + col * SLOT_SIZE - this.leftPos + 1;
            slot.y = gridStartY + row * SLOT_SIZE - this.topPos + 1;
        }

        // Hotbar slots (36-44)
        int hotbarStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        for (int i = 36; i < 45; i++) {
            Slot slot = this.menu.slots.get(i);
            int hotbarIndex = i - 36;
            slot.x = hotbarStartX + hotbarIndex * SLOT_SIZE - this.leftPos + 1;
            slot.y = hotbarStartY - this.topPos + 1;
        }

        // Armor slots (45-48) - left column
        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        for (int i = 45; i < 49; i++) {
            Slot slot = this.menu.slots.get(i);
            int armorIndex = i - 45;
            slot.x = armorSlotX - this.leftPos + 1;
            slot.y = armorTopY + (armorIndex * SLOT_SIZE) - this.topPos + 1;
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderPanelBackgrounds(guiGraphics);
        renderPlayerColumn(guiGraphics);
        renderInventoryColumn(guiGraphics);
        renderContainerColumn(guiGraphics);
    }

    private void renderPanelBackgrounds(GuiGraphics guiGraphics) {
        int panelWidth = columnWidth - PANEL_GAP;

        guiGraphics.fill(leftColumnX, 0, leftColumnX + panelWidth, this.height, PANEL_BG_COLOR);
        guiGraphics.fill(middleColumnX, 0, middleColumnX + panelWidth, this.height, PANEL_BG_COLOR);
        guiGraphics.fill(rightColumnX, 0, rightColumnX + panelWidth, this.height, PANEL_BG_COLOR);
    }

    private void renderPlayerColumn(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, "Player", leftColumnX + 8, 8, 0xFFFFFF, false);

        int playerCenterX = leftColumnX + columnWidth / 2;
        int playerCenterY = this.height / 2;

        int playerRenderX = playerCenterX;
        int playerRenderY = playerCenterY + PLAYER_RENDER_HEIGHT / 2 - 10;

        if (this.minecraft != null && this.minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    playerRenderX,
                    playerRenderY,
                    30,
                    (float) playerRenderX - this.xMouse,
                    (float) (playerCenterY - PLAYER_RENDER_HEIGHT / 2) - this.yMouse,
                    this.minecraft.player);
        }

        // Render armor slot backgrounds
        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        for (int i = 0; i < 4; i++) {
            renderSlotBackground(guiGraphics, armorSlotX, armorTopY + i * SLOT_SIZE);
        }
    }

    private void renderInventoryColumn(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, "Inventory", middleColumnX + 8, 8, 0xFFFFFF, false);

        int gridWidth = 9 * SLOT_SIZE;
        int gridStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int gridStartY = this.height / 2 - (3 * SLOT_SIZE) / 2 - SLOT_SIZE;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                renderSlotBackground(guiGraphics, gridStartX + col * SLOT_SIZE, gridStartY + row * SLOT_SIZE);
            }
        }

        int hotbarStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        for (int i = 0; i < 9; i++) {
            renderSlotBackground(guiGraphics, hotbarStartX + i * SLOT_SIZE, hotbarStartY);
        }
    }

    private void renderContainerColumn(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, this.title, rightColumnX + 8, 8, 0xFFAA00, false);

        int containerWidth = 3 * SLOT_SIZE;
        int containerStartX = rightColumnX + (columnWidth - containerWidth) / 2;
        int containerStartY = this.height / 2 - (3 * SLOT_SIZE) / 2;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                renderSlotBackground(guiGraphics, containerStartX + col * SLOT_SIZE, containerStartY + row * SLOT_SIZE);
            }
        }
    }

    private void renderSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF1a1a1a);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF3d3d3d);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF2d2d2d);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }
}
