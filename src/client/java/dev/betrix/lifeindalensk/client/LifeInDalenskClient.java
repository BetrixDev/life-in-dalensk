package dev.betrix.lifeindalensk.client;

import dev.betrix.lifeindalensk.client.hud.CurrencyHudOverlay;
import dev.betrix.lifeindalensk.client.render.TraderNPCRenderer;
import dev.betrix.lifeindalensk.client.screen.SearchableContainerScreen;
import dev.betrix.lifeindalensk.client.screen.TraderScreen;
import dev.betrix.lifeindalensk.network.packet.CurrencyChangeS2CPacket;
import dev.betrix.lifeindalensk.network.packet.SyncCurrencyS2CPacket;
import dev.betrix.lifeindalensk.network.packet.SyncTraderStockS2CPacket;
import dev.betrix.lifeindalensk.registry.ModEntityTypes;
import dev.betrix.lifeindalensk.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class LifeInDalenskClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register screens
        HandledScreens.register(ModScreenHandlers.SEARCHABLE_CONTAINER, SearchableContainerScreen::new);
        HandledScreens.register(ModScreenHandlers.TRADER_MENU, TraderScreen::new);

        // Register entity renderers
        EntityRendererRegistry.register(ModEntityTypes.TRADER_NPC, TraderNPCRenderer::new);

        // Register HUD overlay
        CurrencyHudOverlay hudOverlay = new CurrencyHudOverlay();
        HudRenderCallback.EVENT.register(hudOverlay);

        // Register client tick for HUD animations
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CurrencyHudOverlay.tick();
        });

        // Register S2C packet handlers
        registerPacketHandlers();
    }

    private void registerPacketHandlers() {
        // Sync currency packet - updates client's stored currency value
        ClientPlayNetworking.registerGlobalReceiver(SyncCurrencyS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                ClientCurrencyData.setRoubles(packet.roubles());
            });
        });

        // Currency change packet - shows floating +/- animation
        ClientPlayNetworking.registerGlobalReceiver(CurrencyChangeS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                CurrencyHudOverlay.addFloatingNumber(packet.amount(), packet.isAddition());
            });
        });

        // Sync trader stock packet - updates cached stock for trader
        ClientPlayNetworking.registerGlobalReceiver(SyncTraderStockS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                ClientTraderStockCache.setStock(packet.traderId(), packet.getStocksArray(), packet.restockTime());
            });
        });
    }
}
