package dev.betrix.exclusioncraft.client;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.client.gui.SurvivalInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExclusionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

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
