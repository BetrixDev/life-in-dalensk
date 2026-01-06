package dev.betrix.lifeindalensk.client.render;

import dev.betrix.lifeindalensk.entity.TraderNPC;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TraderNPCRenderer extends MobEntityRenderer<TraderNPC, PlayerEntityModel<TraderNPC>> {

    private static final Identifier DEFAULT_TEXTURE = Identifier.ofVanilla("textures/entity/steve.png");

    public TraderNPCRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    @NotNull
    public Identifier getTexture(@NotNull TraderNPC entity) {
        var traderData = entity.getTraderData();
        if (traderData != null) {
            return traderData.getSkinTexture();
        }
        return DEFAULT_TEXTURE;
    }
}
