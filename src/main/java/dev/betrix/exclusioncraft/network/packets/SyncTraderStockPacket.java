package dev.betrix.exclusioncraft.network.packets;

import dev.betrix.exclusioncraft.client.ClientTraderStockCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Syncs trader stock data from server to client.
 */
public class SyncTraderStockPacket {

    private final String traderId;
    private final int[] stocks;
    private final long restockTime;

    public SyncTraderStockPacket(String traderId, int[] stocks, long restockTime) {
        this.traderId = traderId;
        this.stocks = stocks;
        this.restockTime = restockTime;
    }

    public static void encode(SyncTraderStockPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.traderId);
        buf.writeVarInt(packet.stocks.length);
        for (int stock : packet.stocks) {
            buf.writeVarInt(stock);
        }
        buf.writeLong(packet.restockTime);
    }

    public static SyncTraderStockPacket decode(FriendlyByteBuf buf) {
        String traderId = buf.readUtf();
        int length = buf.readVarInt();
        int[] stocks = new int[length];
        for (int i = 0; i < length; i++) {
            stocks[i] = buf.readVarInt();
        }
        long restockTime = buf.readLong();
        return new SyncTraderStockPacket(traderId, stocks, restockTime);
    }

    public static void handle(SyncTraderStockPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientTraderStockCache.setStock(packet.traderId, packet.stocks, packet.restockTime);
        });
        ctx.get().setPacketHandled(true);
    }
}
