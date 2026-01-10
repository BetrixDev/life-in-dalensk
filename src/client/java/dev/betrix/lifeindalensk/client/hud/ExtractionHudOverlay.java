package dev.betrix.lifeindalensk.client.hud;

import dev.betrix.lifeindalensk.extraction.PlayerExtractionComponent;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders extraction countdown overlay above the hotbar.
 * Displays "Extraction in X.X seconds" when player is extracting.
 */
public class ExtractionHudOverlay implements HudRenderCallback {

    private static final int TEXT_COLOR = 0xFFFFFF; // White
    private static final int SHADOW_COLOR = 0x3F3F3F; // Dark gray

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) {
            return;
        }

        PlayerExtractionComponent extraction = PlayerExtractionComponent.KEY.get(mc.player);
        if (!extraction.isExtracting()) {
            return;
        }

        int extractionTicks = extraction.getExtractionTicks();
        double seconds = extractionTicks / 20.0; // Convert ticks to seconds

        String text = String.format("Extraction in %.1f seconds", seconds);

        TextRenderer font = mc.textRenderer;
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int textWidth = font.getWidth(text);
        
        // Position above hotbar (hotbar is at screenHeight - 39)
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 59; // 20 pixels above hotbar

        // Draw text with shadow
        drawContext.drawText(font, text, x + 1, y + 1, SHADOW_COLOR, false);
        drawContext.drawText(font, text, x, y, TEXT_COLOR, false);
    }
}
