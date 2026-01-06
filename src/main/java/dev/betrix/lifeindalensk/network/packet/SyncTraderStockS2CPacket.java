package dev.betrix.lifeindalensk.network.packet;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Syncs trader stock data from server to client.
 * Client handler is registered in LifeInDalenskClient.
 */
public record SyncTraderStockS2CPacket(String traderId, List<Integer> stocks, long restockTime)
        implements CustomPayload {

    public static final CustomPayload.Id<SyncTraderStockS2CPacket> ID = new CustomPayload.Id<>(
            Identifier.of(LifeInDalensk.MOD_ID, "sync_trader_stock"));

    public static final PacketCodec<RegistryByteBuf, SyncTraderStockS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SyncTraderStockS2CPacket::traderId,
            PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), SyncTraderStockS2CPacket::stocks,
            PacketCodecs.VAR_LONG, SyncTraderStockS2CPacket::restockTime,
            SyncTraderStockS2CPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Get stock as an array for compatibility.
     */
    public int[] getStocksArray() {
        return stocks.stream().mapToInt(Integer::intValue).toArray();
    }
}
