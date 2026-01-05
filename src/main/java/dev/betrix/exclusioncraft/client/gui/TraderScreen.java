package dev.betrix.exclusioncraft.client.gui;

import dev.betrix.exclusioncraft.client.ClientTraderStockCache;
import dev.betrix.exclusioncraft.currency.PlayerCurrencyProvider;
import dev.betrix.exclusioncraft.inventory.TraderMenu;
import dev.betrix.exclusioncraft.network.ModNetworking;
import dev.betrix.exclusioncraft.network.packets.TraderBuyPacket;
import dev.betrix.exclusioncraft.network.packets.TraderSellPacket;
import dev.betrix.exclusioncraft.trader.TraderData;
import dev.betrix.exclusioncraft.trader.TraderOffer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class TraderScreen extends AbstractContainerScreen<TraderMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int PLAYER_RENDER_WIDTH = 51;
    private static final int PLAYER_RENDER_HEIGHT = 72;
    private static final int PANEL_GAP = 4;
    private static final int PANEL_BG_COLOR = 0x40000000;
    private static final int EFFECT_ICON_SIZE = 18;
    private static final int EFFECT_ENTRY_HEIGHT = 24;
    private static final int EFFECT_ENTRY_PADDING = 4;

    private static final int TAB_WIDTH = 60;
    private static final int TAB_HEIGHT = 20;
    private static final int BUY_CELL_SIZE = 40;
    private static final int BUY_GRID_COLS = 4;

    private int leftColumnX;
    private int middleColumnX;
    private int rightColumnX;
    private int columnWidth;

    private float xMouse;
    private float yMouse;

    private TraderTab currentTab = TraderTab.TRADE;
    private int selectedOfferIndex = -1;
    private int buyQuantity = 1;
    private boolean highlightSellable = false;

    private int tradeTabX;
    private int questTabX;
    private int tabY;

    public TraderScreen(TraderMenu menu, Inventory playerInventory, Component title) {
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

        tabY = 30;
        tradeTabX = rightColumnX + 8;
        questTabX = tradeTabX + TAB_WIDTH + 4;

        repositionSlots();
    }

    private void repositionSlots() {
        int playerCenterX = leftColumnX + columnWidth / 2;
        int playerCenterY = this.height / 2;
        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        // Sell slots (0-8) - 3x3 grid in right column
        int sellGridWidth = 3 * SLOT_SIZE;
        int sellStartX = rightColumnX + (columnWidth - sellGridWidth) / 2;
        int sellStartY = this.height - 120;

        for (int i = 0; i < TraderMenu.SELL_SLOT_COUNT; i++) {
            Slot slot = this.menu.slots.get(i);
            int row = i / 3;
            int col = i % 3;
            slot.x = sellStartX + col * SLOT_SIZE - this.leftPos + 1;
            slot.y = sellStartY + row * SLOT_SIZE - this.topPos + 1;
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
        renderTraderColumn(guiGraphics, mouseX, mouseY);
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

        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        for (int i = 0; i < 4; i++) {
            renderSlotBackground(guiGraphics, armorSlotX, armorTopY + i * SLOT_SIZE);
        }

        renderEffects(guiGraphics, playerCenterX, playerCenterY);
    }

    private void renderEffects(GuiGraphics guiGraphics, int playerCenterX, int playerCenterY) {
        if (this.minecraft == null || this.minecraft.player == null)
            return;

        Collection<MobEffectInstance> effects = this.minecraft.player.getActiveEffects();
        if (effects.isEmpty())
            return;

        MobEffectTextureManager textureManager = this.minecraft.getMobEffectTextures();
        int effectsStartX = playerCenterX + PLAYER_RENDER_WIDTH / 2 + 8;
        int effectsStartY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;
        int effectWidth = columnWidth - (effectsStartX - leftColumnX) - 8;

        int yOffset = 0;
        for (MobEffectInstance effect : effects) {
            int entryY = effectsStartY + yOffset;

            boolean isBeneficial = effect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL;
            int accentColor = isBeneficial ? 0xFF2d8a4e : 0xFF8a2d2d;
            int bgColor = 0xCC1a1a1a;

            guiGraphics.fill(effectsStartX, entryY, effectsStartX + effectWidth, entryY + EFFECT_ENTRY_HEIGHT, bgColor);
            guiGraphics.fill(effectsStartX, entryY, effectsStartX + 2, entryY + EFFECT_ENTRY_HEIGHT, accentColor);

            int iconX = effectsStartX + EFFECT_ENTRY_PADDING + 2;
            int iconY = entryY + (EFFECT_ENTRY_HEIGHT - EFFECT_ICON_SIZE) / 2;
            guiGraphics.blit(iconX, iconY, 0, EFFECT_ICON_SIZE, EFFECT_ICON_SIZE,
                    textureManager.get(effect.getEffect()));

            String name = effect.getEffect().getDisplayName().getString();
            if (effect.getAmplifier() > 0) {
                name += " " + toRoman(effect.getAmplifier() + 1);
            }
            int textX = iconX + EFFECT_ICON_SIZE + 4;
            int textY = entryY + 4;
            guiGraphics.drawString(this.font, name, textX, textY, 0xFFFFFF, false);

            String duration = StringUtil.formatTickDuration(effect.getDuration());
            guiGraphics.drawString(this.font, duration, textX, textY + 10, 0xAAAAAA, false);

            yOffset += EFFECT_ENTRY_HEIGHT + 2;
        }
    }

    private static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }

    private static String formatTime(long ticks) {
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    private void renderInventoryColumn(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, "Inventory", middleColumnX + 8, 8, 0xFFFFFF, false);

        int gridWidth = 9 * SLOT_SIZE;
        int gridStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int gridStartY = this.height / 2 - (3 * SLOT_SIZE) / 2 - SLOT_SIZE;

        // Main inventory (slots 9-35 in menu, which are player inv slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = 9 + row * 9 + col; // Menu slot index
                int x = gridStartX + col * SLOT_SIZE;
                int y = gridStartY + row * SLOT_SIZE;
                renderSlotBackground(guiGraphics, x, y);

                if (highlightSellable && currentTab == TraderTab.TRADE) {
                    renderSellableHighlight(guiGraphics, slotIndex, x, y);
                }
            }
        }

        int hotbarStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        // Hotbar (slots 36-44 in menu, which are player inv slots 0-8)
        for (int i = 0; i < 9; i++) {
            int slotIndex = 36 + i; // Menu slot index
            int x = hotbarStartX + i * SLOT_SIZE;
            int y = hotbarStartY;
            renderSlotBackground(guiGraphics, x, y);

            if (highlightSellable && currentTab == TraderTab.TRADE) {
                renderSellableHighlight(guiGraphics, slotIndex, x, y);
            }
        }
    }

    private void renderSellableHighlight(GuiGraphics guiGraphics, int slotIndex, int x, int y) {
        if (slotIndex >= 0 && slotIndex < menu.slots.size()) {
            Slot slot = menu.slots.get(slotIndex);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && menu.willBuyItem(stack)) {
                // Draw green highlight border
                int highlightColor = 0xFF55FF55;
                guiGraphics.fill(x, y, x + SLOT_SIZE, y + 1, highlightColor);
                guiGraphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, highlightColor);
                guiGraphics.fill(x, y, x + 1, y + SLOT_SIZE, highlightColor);
                guiGraphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, highlightColor);
            }
        }
    }

    private void renderHighlightCheckbox(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int checkboxSize = 10;
        boolean hovered = mouseX >= x && mouseX < x + checkboxSize && mouseY >= y && mouseY < y + checkboxSize;

        // Checkbox background
        int bgColor = hovered ? 0xFF4d4d4d : 0xFF3d3d3d;
        guiGraphics.fill(x, y, x + checkboxSize, y + checkboxSize, bgColor);
        guiGraphics.fill(x + 1, y + 1, x + checkboxSize - 1, y + checkboxSize - 1, 0xFF2d2d2d);

        // Checkmark if enabled
        if (highlightSellable) {
            int checkColor = 0xFF55FF55;
            guiGraphics.fill(x + 2, y + 2, x + checkboxSize - 2, y + checkboxSize - 2, checkColor);
        }

        // Label
        guiGraphics.drawString(this.font, "Highlight Sellable Items", x + checkboxSize + 4, y + 1,
                highlightSellable ? 0x55FF55 : 0x888888, false);
    }

    private boolean isHighlightCheckboxClicked(double mouseX, double mouseY) {
        int sellSectionY = this.height - 140;
        int checkboxX = rightColumnX + 40;
        int checkboxY = sellSectionY - 2;
        int checkboxSize = 10;

        return mouseX >= checkboxX && mouseX < checkboxX + checkboxSize &&
                mouseY >= checkboxY && mouseY < checkboxY + checkboxSize;
    }

    private void renderTraderColumn(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TraderData trader = menu.getTraderData();
        if (trader == null) {
            guiGraphics.drawString(this.font, "Unknown Trader", rightColumnX + 8, 8, 0xFF5555, false);
            return;
        }

        // Trader name
        guiGraphics.drawString(this.font, trader.getName(), rightColumnX + 8, 8, 0xFFAA00, false);

        // Restock timer
        long restockTime = ClientTraderStockCache.getRestockTime(menu.getTraderId());
        if (this.minecraft != null && this.minecraft.level != null && restockTime > 0) {
            long currentTime = this.minecraft.level.getGameTime();
            long ticksRemaining = Math.max(0, restockTime - currentTime);
            String timeText = "Restock: " + formatTime(ticksRemaining);
            guiGraphics.drawString(this.font, timeText, rightColumnX + 8, 20, 0x888888, false);
        }

        // Tab buttons
        renderTab(guiGraphics, tradeTabX, tabY, "Trade", currentTab == TraderTab.TRADE, mouseX, mouseY);
        renderTab(guiGraphics, questTabX, tabY, "Quests", currentTab == TraderTab.QUESTS, mouseX, mouseY);

        if (currentTab == TraderTab.TRADE) {
            renderTradeTab(guiGraphics, trader, mouseX, mouseY);
        } else {
            renderQuestsTab(guiGraphics);
        }
    }

    private void renderTab(GuiGraphics guiGraphics, int x, int y, String text, boolean selected, int mouseX,
            int mouseY) {
        int bgColor = selected ? 0xFF3d3d3d : 0xFF1a1a1a;
        boolean hovered = mouseX >= x && mouseX < x + TAB_WIDTH && mouseY >= y && mouseY < y + TAB_HEIGHT;
        if (hovered && !selected) {
            bgColor = 0xFF2d2d2d;
        }

        guiGraphics.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT, bgColor);
        if (selected) {
            guiGraphics.fill(x, y + TAB_HEIGHT - 2, x + TAB_WIDTH, y + TAB_HEIGHT, 0xFFFFAA00);
        }

        int textWidth = this.font.width(text);
        guiGraphics.drawString(this.font, text, x + (TAB_WIDTH - textWidth) / 2, y + 6, 0xFFFFFF, false);
    }

    private void renderTradeTab(GuiGraphics guiGraphics, TraderData trader, int mouseX, int mouseY) {
        int contentY = tabY + TAB_HEIGHT + 10;

        // Buy section header
        guiGraphics.drawString(this.font, "Buy", rightColumnX + 8, contentY, 0xAAAAAA, false);
        contentY += 12;

        // Buy grid
        List<TraderOffer> offers = trader.getSellOffers();
        int gridStartX = rightColumnX + 8;
        int gridStartY = contentY;

        for (int i = 0; i < offers.size(); i++) {
            int col = i % BUY_GRID_COLS;
            int row = i / BUY_GRID_COLS;
            int cellX = gridStartX + col * (BUY_CELL_SIZE + 2);
            int cellY = gridStartY + row * (BUY_CELL_SIZE + 2);

            renderBuyCell(guiGraphics, cellX, cellY, offers.get(i), i, mouseX, mouseY);
        }

        // Selected item purchase bar
        int purchaseBarY = gridStartY + ((offers.size() + BUY_GRID_COLS - 1) / BUY_GRID_COLS) * (BUY_CELL_SIZE + 2)
                + 10;
        renderPurchaseBar(guiGraphics, trader, purchaseBarY, mouseX, mouseY);

        // Sell section
        int sellSectionY = this.height - 140;
        guiGraphics.drawString(this.font, "Sell", rightColumnX + 8, sellSectionY, 0xAAAAAA, false);

        // Highlight checkbox
        renderHighlightCheckbox(guiGraphics, rightColumnX + 40, sellSectionY - 2, mouseX, mouseY);

        // Sell slots background
        int sellGridWidth = 3 * SLOT_SIZE;
        int sellStartX = rightColumnX + (columnWidth - sellGridWidth) / 2;
        int sellStartY = this.height - 120;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                renderSlotBackground(guiGraphics, sellStartX + col * SLOT_SIZE, sellStartY + row * SLOT_SIZE);
            }
        }

        // Sell value and button
        long sellValue = menu.calculateSellValue();
        int sellInfoY = sellStartY + 3 * SLOT_SIZE + 8;

        guiGraphics.drawString(this.font, "Value: ₽" + sellValue, rightColumnX + 8, sellInfoY, 0xAAAAAA, false);

        int sellButtonX = rightColumnX + columnWidth - 60;
        int sellButtonY = sellInfoY - 2;
        int sellButtonW = 50;
        int sellButtonH = 16;

        boolean canSell = sellValue > 0;
        boolean sellHovered = mouseX >= sellButtonX && mouseX < sellButtonX + sellButtonW &&
                mouseY >= sellButtonY && mouseY < sellButtonY + sellButtonH;

        int sellBtnColor = canSell ? (sellHovered ? 0xFF3d8a4e : 0xFF2d7a3e) : 0xFF3d3d3d;
        guiGraphics.fill(sellButtonX, sellButtonY, sellButtonX + sellButtonW, sellButtonY + sellButtonH, sellBtnColor);
        guiGraphics.drawString(this.font, "Sell", sellButtonX + 16, sellButtonY + 4, canSell ? 0xFFFFFF : 0x666666,
                false);
    }

    private void renderBuyCell(GuiGraphics guiGraphics, int x, int y, TraderOffer offer, int index, int mouseX,
            int mouseY) {
        int stock = ClientTraderStockCache.getStock(menu.getTraderId(), index);
        boolean outOfStock = stock == 0;
        boolean selected = selectedOfferIndex == index;
        boolean hovered = mouseX >= x && mouseX < x + BUY_CELL_SIZE && mouseY >= y && mouseY < y + BUY_CELL_SIZE;

        int bgColor;
        if (outOfStock) {
            bgColor = 0xFF1a1a1a;
        } else if (selected) {
            bgColor = 0xFF3d5a3d;
        } else if (hovered) {
            bgColor = 0xFF3d3d3d;
        } else {
            bgColor = 0xFF2d2d2d;
        }
        guiGraphics.fill(x, y, x + BUY_CELL_SIZE, y + BUY_CELL_SIZE, bgColor);

        if (selected && !outOfStock) {
            guiGraphics.fill(x, y, x + BUY_CELL_SIZE, y + 2, 0xFF4a8a4a);
        }

        // Item icon
        ItemStack itemStack = new ItemStack(offer.getItem());
        int iconX = x + (BUY_CELL_SIZE - 16) / 2;
        int iconY = y + 4;
        guiGraphics.renderItem(itemStack, iconX, iconY);

        // Stock count in top right
        String stockText = stock < 0 ? "?" : String.valueOf(stock);
        int stockColor = outOfStock ? 0xFF5555 : (stock <= 3 ? 0xFFAA00 : 0x55FF55);
        int stockWidth = this.font.width(stockText);
        guiGraphics.drawString(this.font, stockText, x + BUY_CELL_SIZE - stockWidth - 2, y + 2, stockColor, false);

        // Price
        String priceText = "₽" + offer.getPrice();
        int priceWidth = this.font.width(priceText);
        int priceColor = outOfStock ? 0x666666 : 0xFFAA00;
        guiGraphics.drawString(this.font, priceText, x + (BUY_CELL_SIZE - priceWidth) / 2, y + BUY_CELL_SIZE - 10,
                priceColor, false);
    }

    private void renderPurchaseBar(GuiGraphics guiGraphics, TraderData trader, int y, int mouseX, int mouseY) {
        int barX = rightColumnX + 8;
        int barWidth = columnWidth - 20;

        guiGraphics.fill(barX, y, barX + barWidth, y + 30, 0xFF1a1a1a);

        if (selectedOfferIndex >= 0 && selectedOfferIndex < trader.getSellOffers().size()) {
            TraderOffer offer = trader.getSellOffers().get(selectedOfferIndex);
            int stock = ClientTraderStockCache.getStock(menu.getTraderId(), selectedOfferIndex);
            boolean outOfStock = stock == 0;

            ItemStack itemStack = new ItemStack(offer.getItem());

            // Item icon
            guiGraphics.renderItem(itemStack, barX + 4, y + 7);

            // Item name and stock
            String name = itemStack.getHoverName().getString();
            String stockInfo = stock < 0 ? "" : " (Stock: " + stock + ")";
            guiGraphics.drawString(this.font, name, barX + 24, y + 4, outOfStock ? 0x888888 : 0xFFFFFF, false);
            if (!stockInfo.isEmpty()) {
                int nameWidth = this.font.width(name);
                guiGraphics.drawString(this.font, stockInfo, barX + 24 + nameWidth, y + 4,
                        outOfStock ? 0xFF5555 : 0x888888, false);
            }

            if (outOfStock) {
                guiGraphics.drawString(this.font, "Out of stock", barX + 24, y + 16, 0xFF5555, false);
            } else {
                // Quantity controls
                int qtyX = barX + 24;
                int qtyY = y + 16;

                // Minus button
                boolean minusHover = mouseX >= qtyX && mouseX < qtyX + 12 && mouseY >= qtyY && mouseY < qtyY + 12;
                guiGraphics.fill(qtyX, qtyY, qtyX + 12, qtyY + 12, minusHover ? 0xFF4d4d4d : 0xFF3d3d3d);
                guiGraphics.drawString(this.font, "-", qtyX + 4, qtyY + 2, 0xFFFFFF, false);

                // Quantity display
                String qtyText = String.valueOf(buyQuantity);
                guiGraphics.drawString(this.font, qtyText, qtyX + 18, qtyY + 2, 0xFFFFFF, false);

                // Plus button
                int plusX = qtyX + 30;
                boolean plusHover = mouseX >= plusX && mouseX < plusX + 12 && mouseY >= qtyY && mouseY < qtyY + 12;
                guiGraphics.fill(plusX, qtyY, plusX + 12, qtyY + 12, plusHover ? 0xFF4d4d4d : 0xFF3d3d3d);
                guiGraphics.drawString(this.font, "+", plusX + 3, qtyY + 2, 0xFFFFFF, false);

                // Total cost
                long totalCost = offer.getPrice() * buyQuantity;
                long playerRoubles = this.minecraft != null && this.minecraft.player != null
                        ? PlayerCurrencyProvider.getRoubles(this.minecraft.player)
                        : 0;
                boolean canAfford = playerRoubles >= totalCost;
                boolean hasStock = stock < 0 || buyQuantity <= stock;
                boolean canBuy = canAfford && hasStock;

                String costText = "Total: ₽" + totalCost;
                guiGraphics.drawString(this.font, costText, qtyX + 50, qtyY + 2, canBuy ? 0x55FF55 : 0xFF5555, false);

                // Buy button
                int buyBtnX = barX + barWidth - 40;
                int buyBtnY = y + 8;
                int buyBtnW = 35;
                int buyBtnH = 14;

                boolean buyHover = mouseX >= buyBtnX && mouseX < buyBtnX + buyBtnW &&
                        mouseY >= buyBtnY && mouseY < buyBtnY + buyBtnH;
                int buyBtnColor = canBuy ? (buyHover ? 0xFF3d8a4e : 0xFF2d7a3e) : 0xFF5d3d3d;

                guiGraphics.fill(buyBtnX, buyBtnY, buyBtnX + buyBtnW, buyBtnY + buyBtnH, buyBtnColor);
                guiGraphics.drawString(this.font, "Buy", buyBtnX + 10, buyBtnY + 3, canBuy ? 0xFFFFFF : 0xAAAAAA,
                        false);
            }
        } else {
            guiGraphics.drawString(this.font, "Select an item to buy", barX + 10, y + 10, 0x666666, false);
        }
    }

    private void renderQuestsTab(GuiGraphics guiGraphics) {
        int contentY = tabY + TAB_HEIGHT + 40;
        int centerX = rightColumnX + columnWidth / 2;

        String wip = "Work in Progress";
        int textWidth = this.font.width(wip);
        guiGraphics.drawString(this.font, wip, centerX - textWidth / 2, contentY, 0xAAAAAA, false);
    }

    private void renderSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF1a1a1a);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF3d3d3d);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF2d2d2d);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Tab clicks
            if (isInBounds(mouseX, mouseY, tradeTabX, tabY, TAB_WIDTH, TAB_HEIGHT)) {
                currentTab = TraderTab.TRADE;
                return true;
            }
            if (isInBounds(mouseX, mouseY, questTabX, tabY, TAB_WIDTH, TAB_HEIGHT)) {
                currentTab = TraderTab.QUESTS;
                return true;
            }

            if (currentTab == TraderTab.TRADE) {
                // Highlight checkbox
                if (isHighlightCheckboxClicked(mouseX, mouseY)) {
                    highlightSellable = !highlightSellable;
                    return true;
                }

                TraderData trader = menu.getTraderData();
                if (trader != null) {
                    // Buy cell clicks
                    List<TraderOffer> offers = trader.getSellOffers();
                    int gridStartX = rightColumnX + 8;
                    int gridStartY = tabY + TAB_HEIGHT + 22;

                    for (int i = 0; i < offers.size(); i++) {
                        int col = i % BUY_GRID_COLS;
                        int row = i / BUY_GRID_COLS;
                        int cellX = gridStartX + col * (BUY_CELL_SIZE + 2);
                        int cellY = gridStartY + row * (BUY_CELL_SIZE + 2);

                        if (isInBounds(mouseX, mouseY, cellX, cellY, BUY_CELL_SIZE, BUY_CELL_SIZE)) {
                            int stock = ClientTraderStockCache.getStock(menu.getTraderId(), i);
                            if (stock != 0) { // Allow selection if stock > 0 or unknown (-1)
                                selectedOfferIndex = i;
                                buyQuantity = 1;
                            }
                            return true;
                        }
                    }

                    // Purchase bar controls
                    if (selectedOfferIndex >= 0 && selectedOfferIndex < offers.size()) {
                        int purchaseBarY = gridStartY
                                + ((offers.size() + BUY_GRID_COLS - 1) / BUY_GRID_COLS) * (BUY_CELL_SIZE + 2) + 10;
                        int barX = rightColumnX + 8;
                        int barWidth = columnWidth - 20;

                        int qtyX = barX + 24;
                        int qtyY = purchaseBarY + 16;

                        // Minus button
                        if (isInBounds(mouseX, mouseY, qtyX, qtyY, 12, 12)) {
                            buyQuantity = Math.max(1, buyQuantity - 1);
                            return true;
                        }

                        // Plus button
                        int plusX = qtyX + 30;
                        if (isInBounds(mouseX, mouseY, plusX, qtyY, 12, 12)) {
                            int stock = ClientTraderStockCache.getStock(menu.getTraderId(), selectedOfferIndex);
                            int maxQty = stock > 0 ? stock : offers.get(selectedOfferIndex).getMaxStock();
                            buyQuantity = Math.min(maxQty, buyQuantity + 1);
                            return true;
                        }

                        // Buy button
                        int buyBtnX = barX + barWidth - 40;
                        int buyBtnY = purchaseBarY + 8;
                        if (isInBounds(mouseX, mouseY, buyBtnX, buyBtnY, 35, 14)) {
                            TraderOffer offer = offers.get(selectedOfferIndex);
                            int stock = ClientTraderStockCache.getStock(menu.getTraderId(), selectedOfferIndex);

                            // Check stock availability
                            if (stock == 0)
                                return true;
                            if (stock > 0 && buyQuantity > stock)
                                return true;

                            long totalCost = offer.getPrice() * buyQuantity;
                            long playerRoubles = this.minecraft != null && this.minecraft.player != null
                                    ? PlayerCurrencyProvider.getRoubles(this.minecraft.player)
                                    : 0;

                            if (playerRoubles >= totalCost) {
                                ModNetworking.sendToServer(
                                        new TraderBuyPacket(menu.getTraderId(), selectedOfferIndex, buyQuantity));
                                buyQuantity = 1; // Reset quantity after purchase
                            }
                            return true;
                        }
                    }

                    // Sell button
                    int sellStartY = this.height - 120;
                    int sellInfoY = sellStartY + 3 * SLOT_SIZE + 8;
                    int sellButtonX = rightColumnX + columnWidth - 60;
                    int sellButtonY = sellInfoY - 2;

                    if (isInBounds(mouseX, mouseY, sellButtonX, sellButtonY, 50, 16)) {
                        long sellValue = menu.calculateSellValue();
                        if (sellValue > 0) {
                            ModNetworking.sendToServer(new TraderSellPacket(menu.getTraderId()));
                        }
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    private enum TraderTab {
        TRADE, QUESTS
    }
}
