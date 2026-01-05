package dev.betrix.exclusioncraft.trader;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.network.ModNetworking;
import dev.betrix.exclusioncraft.network.packets.SyncTraderStockPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TraderEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(PlayerTraderStockProvider.IDENTIFIER, new PlayerTraderStockProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        original.reviveCaps();
        original.getCapability(PlayerTraderStockProvider.PLAYER_TRADER_STOCK).ifPresent(oldStock -> {
            newPlayer.getCapability(PlayerTraderStockProvider.PLAYER_TRADER_STOCK).ifPresent(newStock -> {
                newStock.deserializeNBT(oldStock.serializeNBT());
            });
        });
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        
        // Only tick every 20 ticks (1 second)
        if (event.player.tickCount % 20 != 0) return;

        event.player.getCapability(PlayerTraderStockProvider.PLAYER_TRADER_STOCK).ifPresent(stock -> {
            stock.tick(event.player.level().getGameTime());
        });
    }

    public static void syncStockToClient(ServerPlayer player, String traderId) {
        player.getCapability(PlayerTraderStockProvider.PLAYER_TRADER_STOCK).ifPresent(stock -> {
            TraderData trader = TraderRegistry.getInstance().getTrader(traderId);
            if (trader == null) return;

            int[] stocks = new int[trader.getSellOffers().size()];
            for (int i = 0; i < stocks.length; i++) {
                stocks[i] = stock.getStock(traderId, i);
            }
            long restockTime = stock.getRestockTime(traderId);

            ModNetworking.sendToPlayer(new SyncTraderStockPacket(traderId, stocks, restockTime), player);
        });
    }

    @Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(PlayerTraderStock.class);
        }
    }
}
