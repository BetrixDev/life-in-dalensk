package dev.betrix.lifeindalensk.mixin.client;

import dev.betrix.lifeindalensk.client.screen.SurvivalInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    public ClientPlayerEntity player;

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen modifyScreen(Screen screen) {
        if (screen instanceof InventoryScreen && player != null && !player.isCreative()) {
            return new SurvivalInventoryScreen(
                    player.playerScreenHandler,
                    player.getInventory(),
                    screen.getTitle()
            );
        }
        return screen;
    }

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void customTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("Life in Dalensk");
    }
}
