package dev.betrix.lifeindalensk.client.screen;

import dev.betrix.lifeindalensk.client.ClientCurrencyData;
import dev.betrix.lifeindalensk.client.ClientTraderStockCache;
import dev.betrix.lifeindalensk.inventory.TraderScreenHandler;
import dev.betrix.lifeindalensk.network.packet.TraderBuyC2SPacket;
import dev.betrix.lifeindalensk.network.packet.TraderSellC2SPacket;
import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderOffer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TraderScreen extends HandledScreen<TraderScreenHandler> {

    private static final int SLOT_SIZE = 18;
    private static final int PLAYER_RENDER_WIDTH = 51;
    private static final int PLAYER_RENDER_HEIGHT = 72;
    private static final int PANEL_GAP = 4;
    private static final int PANEL_BG_COLOR = 0x40000000;

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

    public TraderScreen(TraderScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        // Set background to full screen so clicks aren't considered "outside"
        this.backgroundWidth = this.width;
        this.backgroundHeight = this.height;

        super.init();

        // x and y are set by super.init() - we want them at 0,0 for full-screen layout
        this.x = 0;
        this.y = 0;

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

        // Armor slots positions (slots 45-48 in our handler)
        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        // Inventory grid positions
        int gridWidth = 9 * SLOT_SIZE;
        int gridStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int gridStartY = this.height / 2 - (3 * SLOT_SIZE) / 2 - SLOT_SIZE;

        // Hotbar positions
        int hotbarStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        // Sell slots positions (3x3 grid in right column)
        int sellGridWidth = 3 * SLOT_SIZE;
        int sellStartX = rightColumnX + (columnWidth - sellGridWidth) / 2;
        int sellStartY = this.height - 120;

        for (Slot slot : this.handler.slots) {
            int slotListIndex = this.handler.slots.indexOf(slot);

            if (slotListIndex < TraderScreenHandler.SELL_SLOT_COUNT) {
                // Sell slots (0-8 in handler) -> 3x3 grid
                int row = slotListIndex / 3;
                int col = slotListIndex % 3;
                slot.x = sellStartX + col * SLOT_SIZE + 1;
                slot.y = sellStartY + row * SLOT_SIZE + 1;
            } else if (slotListIndex < TraderScreenHandler.SELL_SLOT_COUNT + 27) {
                // Player main inventory (9-35 in handler)
                int invIndex = slotListIndex - TraderScreenHandler.SELL_SLOT_COUNT;
                int row = invIndex / 9;
                int col = invIndex % 9;
                slot.x = gridStartX + col * SLOT_SIZE + 1;
                slot.y = gridStartY + row * SLOT_SIZE + 1;
            } else if (slotListIndex < TraderScreenHandler.SELL_SLOT_COUNT + 36) {
                // Hotbar (36-44 in handler)
                int hotbarIndex = slotListIndex - TraderScreenHandler.SELL_SLOT_COUNT - 27;
                slot.x = hotbarStartX + hotbarIndex * SLOT_SIZE + 1;
                slot.y = hotbarStartY + 1;
            } else {
                // Armor slots (45-48 in handler)
                int armorIndex = slotListIndex - TraderScreenHandler.SELL_SLOT_COUNT - 36;
                slot.x = armorSlotX + 1;
                slot.y = armorTopY + (armorIndex * SLOT_SIZE) + 1;
            }
        }
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
        renderTraderColumn(context, mouseX, mouseY);
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
                int slotIndex = 9 + row * 9 + col;
                int slotX = gridStartX + col * SLOT_SIZE;
                int slotY = gridStartY + row * SLOT_SIZE;
                renderSlotBackground(context, slotX, slotY);

                if (highlightSellable && currentTab == TraderTab.TRADE) {
                    renderSellableHighlight(context, slotIndex, slotX, slotY);
                }
            }
        }

        int hotbarStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        for (int i = 0; i < 9; i++) {
            int slotIndex = 36 + i;
            int slotX = hotbarStartX + i * SLOT_SIZE;
            int slotY = hotbarStartY;
            renderSlotBackground(context, slotX, slotY);

            if (highlightSellable && currentTab == TraderTab.TRADE) {
                renderSellableHighlight(context, slotIndex, slotX, slotY);
            }
        }
    }

    private void renderSellableHighlight(DrawContext context, int slotIndex, int x, int y) {
        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot slot = handler.slots.get(slotIndex);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && handler.willBuyItem(stack)) {
                int highlightColor = 0xFF55FF55;
                context.fill(x, y, x + SLOT_SIZE, y + 1, highlightColor);
                context.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, highlightColor);
                context.fill(x, y, x + 1, y + SLOT_SIZE, highlightColor);
                context.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, highlightColor);
            }
        }
    }

    private void renderTraderColumn(DrawContext context, int mouseX, int mouseY) {
        TraderData trader = handler.getTraderData();
        if (trader == null) {
            context.drawText(this.textRenderer, "Unknown Trader", rightColumnX + 8, 8, 0xFF5555, false);
            return;
        }

        context.drawText(this.textRenderer, trader.getName(), rightColumnX + 8, 8, 0xFFAA00, false);

        // Restock timer
        long restockTime = ClientTraderStockCache.getRestockTime(handler.getTraderId());
        if (this.client != null && this.client.world != null && restockTime > 0) {
            long currentTime = this.client.world.getTime();
            long ticksRemaining = Math.max(0, restockTime - currentTime);
            String timeText = "Restock: " + formatTime(ticksRemaining);
            context.drawText(this.textRenderer, timeText, rightColumnX + 8, 20, 0x888888, false);
        }

        // Tab buttons
        renderTab(context, tradeTabX, tabY, "Trade", currentTab == TraderTab.TRADE, mouseX, mouseY);
        renderTab(context, questTabX, tabY, "Quests", currentTab == TraderTab.QUESTS, mouseX, mouseY);

        if (currentTab == TraderTab.TRADE) {
            renderTradeTab(context, trader, mouseX, mouseY);
        } else {
            renderQuestsTab(context);
        }
    }

    private void renderTab(DrawContext context, int x, int y, String text, boolean selected, int mouseX, int mouseY) {
        int bgColor = selected ? 0xFF3d3d3d : 0xFF1a1a1a;
        boolean hovered = mouseX >= x && mouseX < x + TAB_WIDTH && mouseY >= y && mouseY < y + TAB_HEIGHT;
        if (hovered && !selected) {
            bgColor = 0xFF2d2d2d;
        }

        context.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT, bgColor);
        if (selected) {
            context.fill(x, y + TAB_HEIGHT - 2, x + TAB_WIDTH, y + TAB_HEIGHT, 0xFFFFAA00);
        }

        int textWidth = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, x + (TAB_WIDTH - textWidth) / 2, y + 6, 0xFFFFFF, false);
    }

    private void renderTradeTab(DrawContext context, TraderData trader, int mouseX, int mouseY) {
        int contentY = tabY + TAB_HEIGHT + 10;

        context.drawText(this.textRenderer, "Buy", rightColumnX + 8, contentY, 0xAAAAAA, false);
        contentY += 12;

        List<TraderOffer> offers = trader.getSellOffers();
        int gridStartX = rightColumnX + 8;
        int gridStartY = contentY;

        for (int i = 0; i < offers.size(); i++) {
            int col = i % BUY_GRID_COLS;
            int row = i / BUY_GRID_COLS;
            int cellX = gridStartX + col * (BUY_CELL_SIZE + 2);
            int cellY = gridStartY + row * (BUY_CELL_SIZE + 2);

            renderBuyCell(context, cellX, cellY, offers.get(i), i, mouseX, mouseY);
        }

        int purchaseBarY = gridStartY + ((offers.size() + BUY_GRID_COLS - 1) / BUY_GRID_COLS) * (BUY_CELL_SIZE + 2)
                + 10;
        renderPurchaseBar(context, trader, purchaseBarY, mouseX, mouseY);

        // Sell section
        int sellSectionY = this.height - 140;
        context.drawText(this.textRenderer, "Sell", rightColumnX + 8, sellSectionY, 0xAAAAAA, false);

        int sellGridWidth = 3 * SLOT_SIZE;
        int sellStartX = rightColumnX + (columnWidth - sellGridWidth) / 2;
        int sellStartY = this.height - 120;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                renderSlotBackground(context, sellStartX + col * SLOT_SIZE, sellStartY + row * SLOT_SIZE);
            }
        }

        long sellValue = handler.calculateSellValue();
        int sellInfoY = sellStartY + 3 * SLOT_SIZE + 8;

        context.drawText(this.textRenderer, "Value: ₽" + sellValue, rightColumnX + 8, sellInfoY, 0xAAAAAA, false);

        int sellButtonX = rightColumnX + columnWidth - 60;
        int sellButtonY = sellInfoY - 2;
        int sellButtonW = 50;
        int sellButtonH = 16;

        boolean canSell = sellValue > 0;
        boolean sellHovered = mouseX >= sellButtonX && mouseX < sellButtonX + sellButtonW &&
                mouseY >= sellButtonY && mouseY < sellButtonY + sellButtonH;

        int sellBtnColor = canSell ? (sellHovered ? 0xFF3d8a4e : 0xFF2d7a3e) : 0xFF3d3d3d;
        context.fill(sellButtonX, sellButtonY, sellButtonX + sellButtonW, sellButtonY + sellButtonH, sellBtnColor);
        context.drawText(this.textRenderer, "Sell", sellButtonX + 16, sellButtonY + 4, canSell ? 0xFFFFFF : 0x666666,
                false);
    }

    private void renderBuyCell(DrawContext context, int x, int y, TraderOffer offer, int index, int mouseX,
            int mouseY) {
        int stock = ClientTraderStockCache.getStock(handler.getTraderId(), index);
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
        context.fill(x, y, x + BUY_CELL_SIZE, y + BUY_CELL_SIZE, bgColor);

        if (selected && !outOfStock) {
            context.fill(x, y, x + BUY_CELL_SIZE, y + 2, 0xFF4a8a4a);
        }

        ItemStack itemStack = new ItemStack(offer.getItem());
        int iconX = x + (BUY_CELL_SIZE - 16) / 2;
        int iconY = y + 4;
        context.drawItem(itemStack, iconX, iconY);

        String stockText = stock < 0 ? "?" : String.valueOf(stock);
        int stockColor = outOfStock ? 0xFF5555 : (stock <= 3 ? 0xFFAA00 : 0x55FF55);
        int stockWidth = this.textRenderer.getWidth(stockText);
        context.drawText(this.textRenderer, stockText, x + BUY_CELL_SIZE - stockWidth - 2, y + 2, stockColor, false);

        String priceText = "₽" + offer.getPrice();
        int priceWidth = this.textRenderer.getWidth(priceText);
        int priceColor = outOfStock ? 0x666666 : 0xFFAA00;
        context.drawText(this.textRenderer, priceText, x + (BUY_CELL_SIZE - priceWidth) / 2, y + BUY_CELL_SIZE - 10,
                priceColor, false);
    }

    private void renderPurchaseBar(DrawContext context, TraderData trader, int y, int mouseX, int mouseY) {
        int barX = rightColumnX + 8;
        int barWidth = columnWidth - 20;

        context.fill(barX, y, barX + barWidth, y + 30, 0xFF1a1a1a);

        if (selectedOfferIndex >= 0 && selectedOfferIndex < trader.getSellOffers().size()) {
            TraderOffer offer = trader.getSellOffers().get(selectedOfferIndex);
            int stock = ClientTraderStockCache.getStock(handler.getTraderId(), selectedOfferIndex);
            boolean outOfStock = stock == 0;

            ItemStack itemStack = new ItemStack(offer.getItem());

            context.drawItem(itemStack, barX + 4, y + 7);

            String name = itemStack.getName().getString();
            String stockInfo = stock < 0 ? "" : " (Stock: " + stock + ")";
            context.drawText(this.textRenderer, name, barX + 24, y + 4, outOfStock ? 0x888888 : 0xFFFFFF, false);
            if (!stockInfo.isEmpty()) {
                int nameWidth = this.textRenderer.getWidth(name);
                context.drawText(this.textRenderer, stockInfo, barX + 24 + nameWidth, y + 4,
                        outOfStock ? 0xFF5555 : 0x888888, false);
            }

            if (outOfStock) {
                context.drawText(this.textRenderer, "Out of stock", barX + 24, y + 16, 0xFF5555, false);
            } else {
                int qtyX = barX + 24;
                int qtyY = y + 16;

                boolean minusHover = mouseX >= qtyX && mouseX < qtyX + 12 && mouseY >= qtyY && mouseY < qtyY + 12;
                context.fill(qtyX, qtyY, qtyX + 12, qtyY + 12, minusHover ? 0xFF4d4d4d : 0xFF3d3d3d);
                context.drawText(this.textRenderer, "-", qtyX + 4, qtyY + 2, 0xFFFFFF, false);

                String qtyText = String.valueOf(buyQuantity);
                context.drawText(this.textRenderer, qtyText, qtyX + 18, qtyY + 2, 0xFFFFFF, false);

                int plusX = qtyX + 30;
                boolean plusHover = mouseX >= plusX && mouseX < plusX + 12 && mouseY >= qtyY && mouseY < qtyY + 12;
                context.fill(plusX, qtyY, plusX + 12, qtyY + 12, plusHover ? 0xFF4d4d4d : 0xFF3d3d3d);
                context.drawText(this.textRenderer, "+", plusX + 3, qtyY + 2, 0xFFFFFF, false);

                long totalCost = offer.getPrice() * buyQuantity;
                long playerRoubles = ClientCurrencyData.getRoubles();
                boolean canAfford = playerRoubles >= totalCost;
                boolean hasStock = stock < 0 || buyQuantity <= stock;
                boolean canBuy = canAfford && hasStock;

                String costText = "Total: ₽" + totalCost;
                context.drawText(this.textRenderer, costText, qtyX + 50, qtyY + 2, canBuy ? 0x55FF55 : 0xFF5555, false);

                int buyBtnX = barX + barWidth - 40;
                int buyBtnY = y + 8;
                int buyBtnW = 35;
                int buyBtnH = 14;

                boolean buyHover = mouseX >= buyBtnX && mouseX < buyBtnX + buyBtnW &&
                        mouseY >= buyBtnY && mouseY < buyBtnY + buyBtnH;
                int buyBtnColor = canBuy ? (buyHover ? 0xFF3d8a4e : 0xFF2d7a3e) : 0xFF5d3d3d;

                context.fill(buyBtnX, buyBtnY, buyBtnX + buyBtnW, buyBtnY + buyBtnH, buyBtnColor);
                context.drawText(this.textRenderer, "Buy", buyBtnX + 10, buyBtnY + 3, canBuy ? 0xFFFFFF : 0xAAAAAA,
                        false);
            }
        } else {
            context.drawText(this.textRenderer, "Select an item to buy", barX + 10, y + 10, 0x666666, false);
        }
    }

    private void renderQuestsTab(DrawContext context) {
        int contentY = tabY + TAB_HEIGHT + 40;
        int centerX = rightColumnX + columnWidth / 2;

        String wip = "Work in Progress";
        int textWidth = this.textRenderer.getWidth(wip);
        context.drawText(this.textRenderer, wip, centerX - textWidth / 2, contentY, 0xAAAAAA, false);
    }

    private void renderSlotBackground(DrawContext context, int x, int y) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF1a1a1a);
        context.fill(x + 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF3d3d3d);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF2d2d2d);
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
                TraderData trader = handler.getTraderData();
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
                            int stock = ClientTraderStockCache.getStock(handler.getTraderId(), i);
                            if (stock != 0) {
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
                            int stock = ClientTraderStockCache.getStock(handler.getTraderId(), selectedOfferIndex);
                            int maxQty = stock > 0 ? stock : offers.get(selectedOfferIndex).getMaxStock();
                            buyQuantity = Math.min(maxQty, buyQuantity + 1);
                            return true;
                        }

                        // Buy button
                        int buyBtnX = barX + barWidth - 40;
                        int buyBtnY = purchaseBarY + 8;
                        if (isInBounds(mouseX, mouseY, buyBtnX, buyBtnY, 35, 14)) {
                            TraderOffer offer = offers.get(selectedOfferIndex);
                            int stock = ClientTraderStockCache.getStock(handler.getTraderId(), selectedOfferIndex);

                            if (stock == 0)
                                return true;
                            if (stock > 0 && buyQuantity > stock)
                                return true;

                            long totalCost = offer.getPrice() * buyQuantity;
                            long playerRoubles = ClientCurrencyData.getRoubles();

                            if (playerRoubles >= totalCost) {
                                ClientPlayNetworking.send(
                                        new TraderBuyC2SPacket(handler.getTraderId(), selectedOfferIndex, buyQuantity));
                                buyQuantity = 1;
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
                        long sellValue = handler.calculateSellValue();
                        if (sellValue > 0) {
                            ClientPlayNetworking.send(new TraderSellC2SPacket(handler.getTraderId()));
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
    protected void drawForeground(@NotNull DrawContext context, int mouseX, int mouseY) {
        // Don't draw default labels
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

    private enum TraderTab {
        TRADE, QUESTS
    }
}
