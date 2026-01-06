package dev.betrix.lifeindalensk.network.packet;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.currency.CurrencyHelper;
import dev.betrix.lifeindalensk.inventory.TraderScreenHandler;
import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Sent from client to server when player wants to sell items to a trader.
 */
public record TraderSellC2SPacket(String traderId) implements CustomPayload {

    public static final CustomPayload.Id<TraderSellC2SPacket> ID = new CustomPayload.Id<>(
            Identifier.of(LifeInDalensk.MOD_ID, "trader_sell"));

    public static final PacketCodec<RegistryByteBuf, TraderSellC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, TraderSellC2SPacket::traderId,
            TraderSellC2SPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handle(TraderSellC2SPacket packet, ServerPlayNetworking.Context context) {
        context.player().getServer().execute(() -> {
            ServerPlayerEntity player = context.player();

            if (!(player.currentScreenHandler instanceof TraderScreenHandler traderMenu))
                return;
            if (!traderMenu.getTraderId().equals(packet.traderId()))
                return;

            TraderData trader = TraderRegistry.getInstance().getTrader(packet.traderId());
            if (trader == null)
                return;

            long totalValue = 0;
            var sellContainer = traderMenu.getSellContainer();

            // Calculate total and validate all items
            for (int i = 0; i < sellContainer.size(); i++) {
                ItemStack stack = sellContainer.getStack(i);
                if (!stack.isEmpty()) {
                    long price = trader.getBuyPriceFor(stack);
                    if (price > 0) {
                        totalValue += price * stack.getCount();
                    }
                }
            }

            if (totalValue > 0) {
                // Clear the sell container and give player the roubles
                for (int i = 0; i < sellContainer.size(); i++) {
                    sellContainer.setStack(i, ItemStack.EMPTY);
                }
                CurrencyHelper.addRoubles(player, totalValue);
            }
        });
    }
}
