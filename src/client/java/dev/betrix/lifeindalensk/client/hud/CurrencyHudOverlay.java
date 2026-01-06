package dev.betrix.lifeindalensk.client.hud;

import dev.betrix.lifeindalensk.client.ClientCurrencyData;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Renders currency HUD overlay and floating +/- animations.
 */
public class CurrencyHudOverlay implements HudRenderCallback {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private static final List<FloatingCurrencyNumber> floatingNumbers = new ArrayList<>();
    private static final Random random = new Random();

    // Colors
    private static final int GOLD_COLOR = 0xFFD700;
    private static final int GREEN_COLOR = 0x44FF44;
    private static final int RED_COLOR = 0xFF4444;
    private static final int SHADOW_COLOR = 0x3F3F00;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) {
            return;
        }

        TextRenderer font = mc.textRenderer;
        int screenWidth = mc.getWindow().getScaledWidth();

        long roubles = ClientCurrencyData.getRoubles();
        String formattedAmount = NUMBER_FORMAT.format(roubles);
        String displayText = "R " + formattedAmount;

        int textWidth = font.getWidth(displayText);
        int padding = 6;
        int height = font.fontHeight;

        int x = screenWidth - textWidth - padding * 2;
        int y = 10;

        // Draw text with shadow
        drawContext.drawText(font, displayText, x + 1, y + 1, SHADOW_COLOR, false);
        drawContext.drawText(font, displayText, x, y, GOLD_COLOR, false);

        // Draw floating numbers
        renderFloatingNumbers(drawContext, font, x + textWidth / 2, y + height + padding);
    }

    private void renderFloatingNumbers(DrawContext drawContext, TextRenderer font, int baseX, int baseY) {
        for (FloatingCurrencyNumber floating : floatingNumbers) {
            float alpha = floating.getAlpha();
            if (alpha <= 0)
                continue;

            float yOffset = floating.getYOffset();
            float xOffset = floating.getXOffset();
            float scale = floating.getScale();

            String prefix = floating.isAddition() ? "+" : "-";
            String text = prefix + "R" + NUMBER_FORMAT.format(floating.getAmount());

            int color = floating.isAddition() ? GREEN_COLOR : RED_COLOR;
            int alphaInt = (int) (alpha * 255);
            int colorWithAlpha = (alphaInt << 24) | (color & 0x00FFFFFF);
            int shadowWithAlpha = (alphaInt << 24) | (SHADOW_COLOR & 0x00FFFFFF);

            float drawX = baseX + xOffset - font.getWidth(text) * scale / 2;
            float drawY = baseY + yOffset;

            var matrices = drawContext.getMatrices();
            matrices.push();
            matrices.translate(drawX, drawY, 0);
            matrices.scale(scale, scale, 1.0f);

            drawContext.drawText(font, text, 1, 1, shadowWithAlpha, false);
            drawContext.drawText(font, text, 0, 0, colorWithAlpha, false);

            matrices.pop();
        }
    }

    /**
     * Called every client tick to update floating numbers.
     */
    public static void tick() {
        Iterator<FloatingCurrencyNumber> iterator = floatingNumbers.iterator();
        while (iterator.hasNext()) {
            FloatingCurrencyNumber floating = iterator.next();
            floating.tick();
            if (floating.isExpired()) {
                iterator.remove();
            }
        }
    }

    /**
     * Add a floating +/- animation for currency changes.
     */
    public static void addFloatingNumber(long amount, boolean isAddition) {
        // Add some random horizontal offset for visual variety
        float xOffset = (random.nextFloat() - 0.5f) * 30f;
        float yOffset = random.nextFloat() * 5f;
        floatingNumbers.add(new FloatingCurrencyNumber(amount, isAddition, xOffset, yOffset));
    }
}
