package dev.betrix.exclusioncraft.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.Collection;

public class SurvivalInventoryScreen extends AbstractContainerScreen<InventoryMenu> {

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

    public SurvivalInventoryScreen(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        columnWidth = this.width / 3;
        leftColumnX = 0;
        middleColumnX = columnWidth;

        repositionSlots();
    }

    private void repositionSlots() {
        int playerCenterX = leftColumnX + columnWidth / 2;
        int playerCenterY = this.height / 2;

        int armorSlotX = playerCenterX - PLAYER_RENDER_WIDTH / 2 - SLOT_SIZE - 4;
        int armorTopY = playerCenterY - PLAYER_RENDER_HEIGHT / 2;

        for (Slot slot : this.menu.slots) {
            int index = slot.index;

            if (index == 0 || (index >= 1 && index <= 4) || index == 45) {
                slot.x = -10000;
                slot.y = -10000;
            } else if (index >= 5 && index <= 8) {
                int armorIndex = index - 5;
                slot.x = armorSlotX - this.leftPos + 1;
                slot.y = armorTopY + (armorIndex * SLOT_SIZE) - this.topPos + 1;
            } else if (index >= 9 && index <= 35) {
                int invIndex = index - 9;
                int row = invIndex / 9;
                int col = invIndex % 9;

                int gridWidth = 9 * SLOT_SIZE;
                int gridStartX = middleColumnX + (columnWidth - gridWidth) / 2;
                int gridStartY = this.height / 2 - (3 * SLOT_SIZE) / 2 - SLOT_SIZE;

                slot.x = gridStartX + col * SLOT_SIZE - this.leftPos + 1;
                slot.y = gridStartY + row * SLOT_SIZE - this.topPos + 1;
            } else if (index >= 36 && index <= 44) {
                int hotbarIndex = index - 36;

                int hotbarWidth = 9 * SLOT_SIZE;
                int hotbarStartX = middleColumnX + (columnWidth - hotbarWidth) / 2;
                int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

                slot.x = hotbarStartX + hotbarIndex * SLOT_SIZE - this.leftPos + 1;
                slot.y = hotbarStartY - this.topPos + 1;
            }
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
    }

    private void renderPanelBackgrounds(GuiGraphics guiGraphics) {
        int panelWidth = columnWidth - PANEL_GAP;

        guiGraphics.fill(leftColumnX, 0, leftColumnX + panelWidth, this.height, PANEL_BG_COLOR);
        guiGraphics.fill(middleColumnX, 0, middleColumnX + panelWidth, this.height, PANEL_BG_COLOR);
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

        int hotbarWidth = 9 * SLOT_SIZE;
        int hotbarStartX = middleColumnX + (columnWidth - hotbarWidth) / 2;
        int hotbarStartY = this.height / 2 + (3 * SLOT_SIZE) / 2 + 8;

        for (int i = 0; i < 9; i++) {
            renderSlotBackground(guiGraphics, hotbarStartX + i * SLOT_SIZE, hotbarStartY);
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
