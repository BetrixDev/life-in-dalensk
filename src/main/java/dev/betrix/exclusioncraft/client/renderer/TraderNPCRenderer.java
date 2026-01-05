package dev.betrix.exclusioncraft.client.renderer;

import dev.betrix.exclusioncraft.entity.TraderNPC;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class TraderNPCRenderer extends HumanoidMobRenderer<TraderNPC, PlayerModel<TraderNPC>> {

    private static final ResourceLocation DEFAULT_TEXTURE = 
            new ResourceLocation("minecraft", "textures/entity/steve.png");

    public TraderNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull TraderNPC entity) {
        var traderData = entity.getTraderData();
        if (traderData != null) {
            return traderData.getSkinTexture();
        }
        return DEFAULT_TEXTURE;
    }
}
