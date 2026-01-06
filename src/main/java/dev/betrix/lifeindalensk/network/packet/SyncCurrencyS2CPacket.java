package dev.betrix.lifeindalensk.network.packet;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Sent from server to client to sync the player's total currency.
 * Client handler is registered in LifeInDalenskClient.
 */
public record SyncCurrencyS2CPacket(long roubles) implements CustomPayload {

    public static final CustomPayload.Id<SyncCurrencyS2CPacket> ID = new CustomPayload.Id<>(
            Identifier.of(LifeInDalensk.MOD_ID, "sync_currency"));

    public static final PacketCodec<RegistryByteBuf, SyncCurrencyS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, SyncCurrencyS2CPacket::roubles,
            SyncCurrencyS2CPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
