package dev.betrix.lifeindalensk.network;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.network.packet.CurrencyChangeS2CPacket;
import dev.betrix.lifeindalensk.network.packet.SyncCurrencyS2CPacket;
import dev.betrix.lifeindalensk.network.packet.SyncTraderStockS2CPacket;
import dev.betrix.lifeindalensk.network.packet.TraderBuyC2SPacket;
import dev.betrix.lifeindalensk.network.packet.TraderSellC2SPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModNetworking {

    // Packet identifiers
    public static final Identifier SYNC_CURRENCY_ID = Identifier.of(LifeInDalensk.MOD_ID, "sync_currency");
    public static final Identifier CURRENCY_CHANGE_ID = Identifier.of(LifeInDalensk.MOD_ID, "currency_change");
    public static final Identifier SYNC_TRADER_STOCK_ID = Identifier.of(LifeInDalensk.MOD_ID, "sync_trader_stock");
    public static final Identifier TRADER_BUY_ID = Identifier.of(LifeInDalensk.MOD_ID, "trader_buy");
    public static final Identifier TRADER_SELL_ID = Identifier.of(LifeInDalensk.MOD_ID, "trader_sell");

    public static void registerC2SPackets() {
        LifeInDalensk.LOGGER.info("Registering C2S packets for " + LifeInDalensk.MOD_ID);

        // Register C2S (client to server) packet payloads
        PayloadTypeRegistry.playC2S().register(TraderBuyC2SPacket.ID, TraderBuyC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TraderSellC2SPacket.ID, TraderSellC2SPacket.CODEC);

        // Register C2S packet handlers
        ServerPlayNetworking.registerGlobalReceiver(TraderBuyC2SPacket.ID, TraderBuyC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(TraderSellC2SPacket.ID, TraderSellC2SPacket::handle);
    }

    public static void registerS2CPackets() {
        LifeInDalensk.LOGGER.info("Registering S2C packets for " + LifeInDalensk.MOD_ID);

        // Register S2C (server to client) packet payloads
        PayloadTypeRegistry.playS2C().register(SyncCurrencyS2CPacket.ID, SyncCurrencyS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(CurrencyChangeS2CPacket.ID, CurrencyChangeS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncTraderStockS2CPacket.ID, SyncTraderStockS2CPacket.CODEC);
    }
}
