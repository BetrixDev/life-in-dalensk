package dev.betrix.exclusioncraft.network;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.network.packets.CurrencyChangePacket;
import dev.betrix.exclusioncraft.network.packets.SyncCurrencyPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExclusionCraft.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.messageBuilder(SyncCurrencyPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncCurrencyPacket::encode)
                .decoder(SyncCurrencyPacket::decode)
                .consumerMainThread(SyncCurrencyPacket::handle)
                .add();

        CHANNEL.messageBuilder(CurrencyChangePacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CurrencyChangePacket::encode)
                .decoder(CurrencyChangePacket::decode)
                .consumerMainThread(CurrencyChangePacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }
}
