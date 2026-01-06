package dev.betrix.lifeindalensk.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * Custom full-screen survival inventory that replaces vanilla inventory.
 * Shows player model on left, inventory grid in middle, with status effects.
 */
public class SurvivalInventoryScreen extends HandledScreen<PlayerScreenHandler> {

    private static final int SLOT_SIZE = 18;
    private static final int PLAYER_RENDER_WIDTH = 51;
    private static final int PLAYER_RENDER_HEIGHT = 72;
    private static final int PANEL_GAP = 4;
    private static final int PANEL_BG_COLOR = 0x40000000;
    private static final int EFFECT_ICON_SIZE = 18;
    private static final int EFFECT_ENTRY_HEIGHT = 24;
    private static final int EFFECT_ENTRY_PADDING = 4;

    private int leftColumnX;
    private int middleColumnX;
    private int columnWidth;

    private float xMouse;
    private float yMouse;

    public SurvivalInventoryScreen(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        // Set background to full screen so clicks aren't considered "outside"
        // which would cause items to be dropped
        this.backgroundWidth = this.width;
        this.backgroundHeight = this.height;
        
        super.init();

        columnWidth = this.width / 3;
        leftColumnX = 0;
        middleColumnX = columnWidth;

        // x and y are set by super.init() based on backgroundWidth/Height
        // We want them at 0,0 for our full-screen layout
        this.x = 0;
        this.y = 0;

        repositionSlots();
    }

    private void repositionSlots() {
        int playerCenterX = leftColumnX + columnWidth / 2;
        int playerCenterY = this.height / 2;

        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        int gridWidth = 9 * SLOT_SIZE;
        int gridStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int gridStartY = this.height / 2 - (3 * SLOT_SIZE) / 2 - SLOT_SIZE;

        int hotbarStartX = middleColumnX + (columnWidth - gridWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        for (Slot slot : this.handler.slots) {
            int index = slot.id;

            if (index == 0 || (index >= 1 && index <= 4) || index == 45) {
                // Crafting output (0), crafting input (1-4), offhand (45) - hide them
                slot.x = -10000;
                slot.y = -10000;
            } else if (index >= 5 && index <= 8) {
                // Armor slots (5-8: head, chest, legs, feet)
                int armorIndex = index - 5;
                // +1 offset to align item icon inside the slot background
                slot.x = armorSlotX + 1;
                slot.y = armorTopY + (armorIndex * SLOT_SIZE) + 1;
            } else if (index >= 9 && index <= 35) {
                // Main inventory (9-35)
                int invIndex = index - 9;
                int row = invIndex / 9;
                int col = invIndex % 9;
                slot.x = gridStartX + col * SLOT_SIZE + 1;
                slot.y = gridStartY + row * SLOT_SIZE + 1;
            } else if (index >= 36 && index <= 44) {
                // Hotbar (36-44)
                int hotbarIndex = index - 36;
                slot.x = hotbarStartX + hotbarIndex * SLOT_SIZE + 1;
                slot.y = hotbarStartY + 1;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render dark background
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        renderPanelBackgrounds(context);
        renderPlayerColumn(context);
        renderInventoryColumn(context);
    }

    private void renderPanelBackgrounds(DrawContext context) {
        int panelWidth = columnWidth - PANEL_GAP;

        context.fill(leftColumnX, 0, leftColumnX + panelWidth, this.height, PANEL_BG_COLOR);
        context.fill(middleColumnX, 0, middleColumnX + panelWidth, this.height, PANEL_BG_COLOR);
    }

    private void renderPlayerColumn(DrawContext context) {
        context.drawText(this.textRenderer, "Player", leftColumnX + 8, 8, 0xFFFFFF, false);

        int playerCenterX = leftColumnX + columnWidth / 2;
        int playerCenterY = this.height / 2;

        int playerRenderX = playerCenterX;
        int playerRenderY = playerCenterY + PLAYER_RENDER_HEIGHT / 2 - 10;

        if (this.client != null && this.client.player != null) {
            // In 1.21, renderEntityInInventoryFollowsMouse takes additional parameters
            InventoryScreen.drawEntity(
                    context,
                    playerRenderX - 25, // x1
                    playerCenterY - PLAYER_RENDER_HEIGHT / 2, // y1
                    playerRenderX + 25, // x2
                    playerRenderY + 10, // y2
                    30, // scale
                    0.0625F, // yOffset
                    this.xMouse,
                    this.yMouse,
                    this.client.player);
        }

        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        for (int i = 0; i < 4; i++) {
            renderSlotBackground(context, armorSlotX, armorTopY + i * SLOT_SIZE);
        }

        renderEffects(context, playerCenterX, playerCenterY);
    }

    private void renderEffects(DrawContext context, int playerCenterX, int playerCenterY) {
        if (this.client == null || this.client.player == null)
            return;

        Collection<StatusEffectInstance> effects = this.client.player.getStatusEffects();
        if (effects.isEmpty())
            return;

        int effectsStartX = playerCenterX + PLAYER_RENDER_WIDTH / 2 + 8;
        int effectsStartY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;
        int effectWidth = columnWidth - (effectsStartX - leftColumnX) - 8;

        int yOffset = 0;
        for (StatusEffectInstance effect : effects) {
            int entryY = effectsStartY + yOffset;

            boolean isBeneficial = effect.getEffectType().value().getCategory() == StatusEffectCategory.BENEFICIAL;
            int accentColor = isBeneficial ? 0xFF2d8a4e : 0xFF8a2d2d;
            int bgColor = 0xCC1a1a1a;

            context.fill(effectsStartX, entryY, effectsStartX + effectWidth, entryY + EFFECT_ENTRY_HEIGHT, bgColor);
            context.fill(effectsStartX, entryY, effectsStartX + 2, entryY + EFFECT_ENTRY_HEIGHT, accentColor);

            int iconX = effectsStartX + EFFECT_ENTRY_PADDING + 2;
            int iconY = entryY + (EFFECT_ENTRY_HEIGHT - EFFECT_ICON_SIZE) / 2;
            
            // Draw effect icon
            Sprite sprite = this.client.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            context.drawSprite(iconX, iconY, 0, EFFECT_ICON_SIZE, EFFECT_ICON_SIZE, sprite);

            String name = effect.getEffectType().value().getName().getString();
            if (effect.getAmplifier() > 0) {
                name += " " + toRoman(effect.getAmplifier() + 1);
            }
            int textX = iconX + EFFECT_ICON_SIZE + 4;
            int textY = entryY + 4;
            context.drawText(this.textRenderer, name, textX, textY, 0xFFFFFF, false);

            Text duration = StatusEffectUtil.getDurationText(effect, 1.0f, this.client.world.getTickManager().getTickRate());
            context.drawText(this.textRenderer, duration, textX, textY + 10, 0xAAAAAA, false);

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

        int hotbarWidth = 9 * SLOT_SIZE;
        int hotbarStartX = middleColumnX + (columnWidth - hotbarWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        for (int i = 0; i < 9; i++) {
            renderSlotBackground(context, hotbarStartX + i * SLOT_SIZE, hotbarStartY);
        }
    }

    private void renderSlotBackground(DrawContext context, int x, int y) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF1a1a1a);
        context.fill(x + 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF3d3d3d);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF2d2d2d);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Don't draw default labels - we handle our own
    }
}
