package dev.betrix.exclusioncraft.currency;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.network.ModNetworking;
import dev.betrix.exclusioncraft.network.packets.SyncCurrencyPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CurrencyEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(PlayerCurrencyProvider.IDENTIFIER, new PlayerCurrencyProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Copy currency on death/respawn
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        original.reviveCaps();
        original.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(oldCurrency -> {
            newPlayer.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(newCurrency -> {
                newCurrency.copyFrom(oldCurrency);
            });
        });
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncCurrencyToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncCurrencyToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncCurrencyToClient(serverPlayer);
        }
    }

    public static void syncCurrencyToClient(ServerPlayer player) {
        player.getCapability(PlayerCurrencyProvider.PLAYER_CURRENCY).ifPresent(currency -> {
            ModNetworking.sendToPlayer(new SyncCurrencyPacket(currency.getRoubles()), player);
        });
    }

    @Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(PlayerCurrency.class);
        }
    }
}
