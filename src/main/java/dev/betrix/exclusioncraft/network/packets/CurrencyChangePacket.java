package dev.betrix.exclusioncraft.network.packets;

import dev.betrix.exclusioncraft.client.hud.CurrencyHudOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CurrencyChangePacket {

    private final long amount;
    private final boolean isAddition;

    public CurrencyChangePacket(long amount, boolean isAddition) {
        this.amount = amount;
        this.isAddition = isAddition;
    }

    public static void encode(CurrencyChangePacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.amount);
        buf.writeBoolean(packet.isAddition);
    }

    public static CurrencyChangePacket decode(FriendlyByteBuf buf) {
        return new CurrencyChangePacket(buf.readLong(), buf.readBoolean());
    }

    public static void handle(CurrencyChangePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CurrencyHudOverlay.addFloatingNumber(packet.amount, packet.isAddition);
        });
        ctx.get().setPacketHandled(true);
    }
}
