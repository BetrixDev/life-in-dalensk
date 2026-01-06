package dev.betrix.lifeindalensk.network.packet;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Sent from server to client when currency changes.
 * Used to display floating +/- animations on the HUD.
 * Client handler is registered in LifeInDalenskClient.
 */
public record CurrencyChangeS2CPacket(long amount, boolean isAddition) implements CustomPayload {

    public static final CustomPayload.Id<CurrencyChangeS2CPacket> ID = new CustomPayload.Id<>(
            Identifier.of(LifeInDalensk.MOD_ID, "currency_change"));

    public static final PacketCodec<RegistryByteBuf, CurrencyChangeS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, CurrencyChangeS2CPacket::amount,
            PacketCodecs.BOOL, CurrencyChangeS2CPacket::isAddition,
            CurrencyChangeS2CPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
