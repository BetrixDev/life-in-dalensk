package dev.betrix.exclusioncraft.network.packets;

import dev.betrix.exclusioncraft.currency.PlayerCurrencyProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCurrencyPacket {

    private final long roubles;

    public SyncCurrencyPacket(long roubles) {
        this.roubles = roubles;
    }

    public static void encode(SyncCurrencyPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.roubles);
    }

    public static SyncCurrencyPacket decode(FriendlyByteBuf buf) {
        return new SyncCurrencyPacket(buf.readLong());
    }

    public static void handle(SyncCurrencyPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(currency -> {
                    currency.setRoubles(packet.roubles);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
