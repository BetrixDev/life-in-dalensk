package dev.betrix.exclusioncraft.client;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.client.gui.SearchableContainerScreen;
import dev.betrix.exclusioncraft.client.gui.SurvivalInventoryScreen;
import dev.betrix.exclusioncraft.registry.ModMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {

    @Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.SEARCHABLE_CONTAINER.get(), SearchableContainerScreen::new);
            });
        }
    }

    @Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onScreenOpening(ScreenEvent.Opening event) {
            if (event.getScreen() instanceof InventoryScreen) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;

                if (player != null && !player.isCreative()) {
                    event.setNewScreen(new SurvivalInventoryScreen(
                            player.inventoryMenu,
                            player.getInventory(),
                            event.getScreen().getTitle()
                    ));
                }
            }
        }
    }
}
