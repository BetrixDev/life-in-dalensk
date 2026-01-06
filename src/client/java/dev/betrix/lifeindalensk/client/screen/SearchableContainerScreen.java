package dev.betrix.lifeindalensk.client.screen;

import dev.betrix.lifeindalensk.inventory.SearchableContainerScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SearchableContainerScreen extends HandledScreen<SearchableContainerScreenHandler> {

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

    public SearchableContainerScreen(SearchableContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        this.backgroundWidth = this.width;
        this.backgroundHeight = this.height;
        this.x = 0;
        this.y = 0;

        super.init();

        columnWidth = this.width / 3;
        leftColumnX = 0;
        middleColumnX = columnWidth;
        rightColumnX = columnWidth * 2;

        repositionSlots();
    }

    private void repositionSlots() {
        // Note: In Fabric 1.21+, Slot.x and Slot.y are final fields.
        // The slot positions are set in the ScreenHandler constructor.
        // This screen adapts by rendering backgrounds at calculated positions
        // while the actual slot positions remain fixed from the handler.
        // The handler should set appropriate default positions.
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
    }

    @Override
    protected void drawBackground(@NotNull DrawContext context, float delta, int mouseX, int mouseY) {
        renderPanelBackgrounds(context);
        renderPlayerColumn(context);
        renderInventoryColumn(context);
        renderContainerColumn(context);
    }

    private void renderPanelBackgrounds(DrawContext context) {
        int panelWidth = columnWidth - PANEL_GAP;

        context.fill(leftColumnX, 0, leftColumnX + panelWidth, this.height, PANEL_BG_COLOR);
        context.fill(middleColumnX, 0, middleColumnX + panelWidth, this.height, PANEL_BG_COLOR);
        context.fill(rightColumnX, 0, rightColumnX + panelWidth, this.height, PANEL_BG_COLOR);
    }

    private void renderPlayerColumn(DrawContext context) {
        context.drawText(this.textRenderer, "Player", leftColumnX + 8, 8, 0xFFFFFF, false);

        int playerCenterX = leftColumnX + columnWidth / 2;
        int playerCenterY = this.height / 2;

        int playerRenderX = playerCenterX;
        int playerRenderY = playerCenterY + PLAYER_RENDER_HEIGHT / 2 - 10;

        if (this.client != null && this.client.player != null) {
            InventoryScreen.drawEntity(
                    context,
                    playerRenderX - 25,
                    playerCenterY - PLAYER_RENDER_HEIGHT / 2,
                    playerRenderX + 25,
                    playerRenderY,
                    30,
                    0.0625f,
                    this.xMouse,
                    this.yMouse,
                    this.client.player);
        }

        // Render armor slot backgrounds
        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        for (int i = 0; i < 4; i++) {
            renderSlotBackground(context, armorSlotX, armorTopY + i * SLOT_SIZE);
        }
    }

    private void renderInventoryColumn(DrawContext context) {
        context.drawText(this.textRenderer, "Inventory", middleColumnX + 8, 8, 0xFFFFFF, false);

        int gridWidth = 9 * SLOT_SIZE;
        int gridStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int gridStartY = this.height / 2 - (3 * SLOT_SIZE) / 2 - SLOT_SIZE;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                renderSlotBackground(context, gridStartX + col * SLOT_SIZE, gridStartY + row * SLOT_SIZE);
            }
        }

        int hotbarStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        for (int i = 0; i < 9; i++) {
            renderSlotBackground(context, hotbarStartX + i * SLOT_SIZE, hotbarStartY);
        }
    }

    private void renderContainerColumn(DrawContext context) {
        context.drawText(this.textRenderer, this.title, rightColumnX + 8, 8, 0xFFAA00, false);

        int containerWidth = 3 * SLOT_SIZE;
        int containerStartX = rightColumnX + (columnWidth - containerWidth) / 2;
        int containerStartY = this.height / 2 - (3 * SLOT_SIZE) / 2;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                renderSlotBackground(context, containerStartX + col * SLOT_SIZE, containerStartY + row * SLOT_SIZE);
            }
        }
    }

    private void renderSlotBackground(DrawContext context, int x, int y) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF1a1a1a);
        context.fill(x + 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF3d3d3d);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF2d2d2d);
    }

    @Override
    protected void drawForeground(@NotNull DrawContext context, int mouseX, int mouseY) {
        // Don't draw default title labels
    }
}
