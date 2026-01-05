package dev.betrix.exclusioncraft.network.packets;

import dev.betrix.exclusioncraft.currency.CurrencyHelper;
import dev.betrix.exclusioncraft.trader.PlayerTraderStockProvider;
import dev.betrix.exclusioncraft.trader.TraderData;
import dev.betrix.exclusioncraft.trader.TraderEvents;
import dev.betrix.exclusioncraft.trader.TraderOffer;
import dev.betrix.exclusioncraft.trader.TraderRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent from client to server when player wants to buy items from a trader.
 */
public class TraderBuyPacket {

    private final String traderId;
    private final int offerIndex;
    private final int quantity;

    public TraderBuyPacket(String traderId, int offerIndex, int quantity) {
        this.traderId = traderId;
        this.offerIndex = offerIndex;
        this.quantity = quantity;
    }

    public static void encode(TraderBuyPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.traderId);
        buf.writeVarInt(packet.offerIndex);
        buf.writeVarInt(packet.quantity);
    }

    public static TraderBuyPacket decode(FriendlyByteBuf buf) {
        return new TraderBuyPacket(buf.readUtf(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(TraderBuyPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            TraderData trader = TraderRegistry.getInstance().getTrader(packet.traderId);
            if (trader == null) return;

            if (packet.offerIndex < 0 || packet.offerIndex >= trader.getSellOffers().size()) return;
            if (packet.quantity <= 0 || packet.quantity > 64) return;

            TraderOffer offer = trader.getSellOffers().get(packet.offerIndex);
            Item item = offer.getItem();
            if (item == null) return;

            // Check stock
            int currentStock = PlayerTraderStockProvider.getStock(player, packet.traderId, packet.offerIndex);
            if (currentStock < packet.quantity) return;

            long totalCost = offer.getPrice() * packet.quantity;

            if (CurrencyHelper.canAfford(player, totalCost)) {
                ItemStack stack = new ItemStack(item, packet.quantity);
                
                // Decrement stock
                player.getCapability(PlayerTraderStockProvider.PLAYER_TRADER_STOCK).ifPresent(stock -> {
                    stock.decrementStock(packet.traderId, packet.offerIndex, packet.quantity);
                });
                
                if (player.getInventory().add(stack)) {
                    CurrencyHelper.subtractRoubles(player, totalCost);
                } else {
                    player.drop(stack, false);
                    CurrencyHelper.subtractRoubles(player, totalCost);
                }
                
                // Sync stock to client
                TraderEvents.syncStockToClient(player, packet.traderId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
