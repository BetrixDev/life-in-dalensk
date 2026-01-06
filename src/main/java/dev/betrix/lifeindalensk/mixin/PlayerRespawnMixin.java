package dev.betrix.lifeindalensk.mixin;

import dev.betrix.lifeindalensk.dimension.UndergroundDimensionManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerRespawnMixin {
    @Inject(
            method = "respawnPlayer",
            at = @At("RETURN")
    )
    private void onPlayerRespawn(
            ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir
    ) {
        ServerPlayerEntity respawnedPlayer = cir.getReturnValue();

        if (!alive) {
            UndergroundDimensionManager.teleportToSpawn(respawnedPlayer);
        }
    }
}
