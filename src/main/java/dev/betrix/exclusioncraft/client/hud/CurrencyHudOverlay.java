package dev.betrix.exclusioncraft.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.currency.PlayerCurrencyProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CurrencyHudOverlay {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private static final List<FloatingCurrencyNumber> floatingNumbers = new ArrayList<>();
    private static final Random random = new Random();

    // Colors
    private static final int GOLD_COLOR = 0xFFD700;
    private static final int GREEN_COLOR = 0x44FF44;
    private static final int RED_COLOR = 0xFF4444;
    private static final int SHADOW_COLOR = 0x3F3F00;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        long roubles = PlayerCurrencyProvider.getRoubles(player);
        String formattedAmount = NUMBER_FORMAT.format(roubles);
        String displayText = "₽ " + formattedAmount;

        int textWidth = font.width(displayText);
        int padding = 6;
        int height = font.lineHeight;

        int x = screenWidth - textWidth - padding * 2;
        int y = 10;

        // Draw background with border effect
//        int bgColor = 0xCC000000;
//        int borderColor = 0xFF333333;
//        graphics.fill(x - padding - 1, y - padding - 1, x + textWidth + padding + 1, y + height + padding + 1,
//                borderColor);
//        graphics.fill(x - padding, y - padding, x + textWidth + padding, y + height + padding, bgColor);

        // Draw text with shadow
        graphics.drawString(font, displayText, x + 1, y + 1, SHADOW_COLOR, false);
        graphics.drawString(font, displayText, x, y, GOLD_COLOR, false);

        // Draw floating numbers
        renderFloatingNumbers(graphics, font, x + textWidth / 2, y + height + padding);
    }

    private static void renderFloatingNumbers(GuiGraphics graphics, Font font, int baseX, int baseY) {
        for (FloatingCurrencyNumber floating : floatingNumbers) {
            float alpha = floating.getAlpha();
            if (alpha <= 0)
                continue;

            float yOffset = floating.getYOffset();
            float xOffset = floating.getXOffset();
            float scale = floating.getScale();

            String prefix = floating.isAddition() ? "+" : "-";
            String text = prefix + "₽" + NUMBER_FORMAT.format(floating.getAmount());

            int color = floating.isAddition() ? GREEN_COLOR : RED_COLOR;
            int alphaInt = (int) (alpha * 255);
            int colorWithAlpha = (alphaInt << 24) | (color & 0x00FFFFFF);
            int shadowWithAlpha = (alphaInt << 24) | (SHADOW_COLOR & 0x00FFFFFF);

            float drawX = baseX + xOffset - font.width(text) * scale / 2;
            float drawY = baseY + yOffset;

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(drawX, drawY, 0);
            poseStack.scale(scale, scale, 1.0f);

            graphics.drawString(font, text, 1, 1, shadowWithAlpha, false);
            graphics.drawString(font, text, 0, 0, colorWithAlpha, false);

            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Iterator<FloatingCurrencyNumber> iterator = floatingNumbers.iterator();
        while (iterator.hasNext()) {
            FloatingCurrencyNumber floating = iterator.next();
            floating.tick();
            if (floating.isExpired()) {
                iterator.remove();
            }
        }
    }

    public static void addFloatingNumber(long amount, boolean isAddition) {
        // Add some random horizontal offset for visual variety
        float xOffset = (random.nextFloat() - 0.5f) * 30f;
        float yOffset = random.nextFloat() * 5f;
        floatingNumbers.add(new FloatingCurrencyNumber(amount, isAddition, xOffset, yOffset));
    }
}
