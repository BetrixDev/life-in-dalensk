package dev.betrix.lifeindalensk.network.packet;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.currency.CurrencyHelper;
import dev.betrix.lifeindalensk.currency.PlayerTraderStockHelper;
import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderOffer;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Sent from client to server when player wants to buy items from a trader.
 */
public record TraderBuyC2SPacket(String traderId, int offerIndex, int quantity) implements CustomPayload {

    public static final CustomPayload.Id<TraderBuyC2SPacket> ID = new CustomPayload.Id<>(
            Identifier.of(LifeInDalensk.MOD_ID, "trader_buy"));

    public static final PacketCodec<RegistryByteBuf, TraderBuyC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, TraderBuyC2SPacket::traderId,
            PacketCodecs.VAR_INT, TraderBuyC2SPacket::offerIndex,
            PacketCodecs.VAR_INT, TraderBuyC2SPacket::quantity,
            TraderBuyC2SPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handle(TraderBuyC2SPacket packet, ServerPlayNetworking.Context context) {
        context.player().getServer().execute(() -> {
            ServerPlayerEntity player = context.player();

            TraderData trader = TraderRegistry.getInstance().getTrader(packet.traderId());
            if (trader == null)
                return;

            if (packet.offerIndex() < 0 || packet.offerIndex() >= trader.getSellOffers().size())
                return;
            if (packet.quantity() <= 0 || packet.quantity() > 64)
                return;

            TraderOffer offer = trader.getSellOffers().get(packet.offerIndex());
            Item item = offer.getItem();
            if (item == null)
                return;

            // Check stock
            int currentStock = PlayerTraderStockHelper.getStock(player, packet.traderId(), packet.offerIndex());
            if (currentStock < packet.quantity())
                return;

            long totalCost = offer.getPrice() * packet.quantity();

            if (CurrencyHelper.canAfford(player, totalCost)) {
                ItemStack stack = new ItemStack(item, packet.quantity());

                // Decrement stock
                PlayerTraderStockHelper.decrementStock(player, packet.traderId(), packet.offerIndex(),
                        packet.quantity());

                if (player.getInventory().insertStack(stack)) {
                    CurrencyHelper.subtractRoubles(player, totalCost);
                } else {
                    player.dropItem(stack, false);
                    CurrencyHelper.subtractRoubles(player, totalCost);
                }

                // Sync stock to client
                PlayerTraderStockHelper.syncStockToClient(player, packet.traderId());
            }
        });
    }
}
