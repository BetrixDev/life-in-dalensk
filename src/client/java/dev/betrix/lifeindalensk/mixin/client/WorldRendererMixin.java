package dev.betrix.lifeindalensk.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes the block outline (wireframe) that appears when looking at a block in survival mode.
 * Creative mode players will still see the outline.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void disableBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity,
                                     double cameraX, double cameraY, double cameraZ,
                                     BlockHitResult blockHitResult, CallbackInfo ci) {
        if (entity instanceof ClientPlayerEntity player && !player.isCreative()) {
            ci.cancel();
        }
    }
}
