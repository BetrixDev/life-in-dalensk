package dev.betrix.exclusioncraft.network.packets;

import dev.betrix.exclusioncraft.currency.CurrencyHelper;
import dev.betrix.exclusioncraft.inventory.TraderMenu;
import dev.betrix.exclusioncraft.trader.TraderData;
import dev.betrix.exclusioncraft.trader.TraderRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent from client to server when player wants to sell items to a trader.
 */
public class TraderSellPacket {

    private final String traderId;

    public TraderSellPacket(String traderId) {
        this.traderId = traderId;
    }

    public static void encode(TraderSellPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.traderId);
    }

    public static TraderSellPacket decode(FriendlyByteBuf buf) {
        return new TraderSellPacket(buf.readUtf());
    }

    public static void handle(TraderSellPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!(player.containerMenu instanceof TraderMenu traderMenu)) return;
            if (!traderMenu.getTraderId().equals(packet.traderId)) return;

            TraderData trader = TraderRegistry.getInstance().getTrader(packet.traderId);
            if (trader == null) return;

            long totalValue = 0;
            var sellContainer = traderMenu.getSellContainer();

            // Calculate total and validate all items
            for (int i = 0; i < sellContainer.getContainerSize(); i++) {
                ItemStack stack = sellContainer.getItem(i);
                if (!stack.isEmpty()) {
                    long price = trader.getBuyPriceFor(stack);
                    if (price > 0) {
                        totalValue += price * stack.getCount();
                    }
                }
            }

            if (totalValue > 0) {
                // Clear the sell container and give player the roubles
                for (int i = 0; i < sellContainer.getContainerSize(); i++) {
                    sellContainer.setItem(i, ItemStack.EMPTY);
                }
                CurrencyHelper.addRoubles(player, totalValue);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
