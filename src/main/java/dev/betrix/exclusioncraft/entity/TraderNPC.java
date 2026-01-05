package dev.betrix.exclusioncraft.entity;

import dev.betrix.exclusioncraft.inventory.TraderMenu;
import dev.betrix.exclusioncraft.trader.TraderBuyEntry;
import dev.betrix.exclusioncraft.trader.TraderData;
import dev.betrix.exclusioncraft.trader.TraderRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TraderNPC extends PathfinderMob {

    private static final EntityDataAccessor<String> TRADER_ID =
            SynchedEntityData.defineId(TraderNPC.class, EntityDataSerializers.STRING);

    public TraderNPC(EntityType<? extends TraderNPC> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TRADER_ID, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public void setTraderId(String traderId) {
        this.entityData.set(TRADER_ID, traderId);
        TraderData data = TraderRegistry.getInstance().getTrader(traderId);
        if (data != null) {
            this.setCustomName(net.minecraft.network.chat.Component.literal(data.getName()));
            this.setCustomNameVisible(true);
        }
    }

    public String getTraderId() {
        return this.entityData.get(TRADER_ID);
    }

    public TraderData getTraderData() {
        return TraderRegistry.getInstance().getTrader(getTraderId());
    }

    @Override
    @Nonnull
    protected InteractionResult mobInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            String traderId = getTraderId();
            if (!traderId.isEmpty() && player instanceof ServerPlayer serverPlayer) {
                TraderNPC self = this;
                NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                    @Override
                    @Nonnull
                    public Component getDisplayName() {
                        TraderData data = getTraderData();
                        return Component.literal(data != null ? data.getName() : "Trader");
                    }

                    @Override
                    @Nullable
                    public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory playerInventory, 
                                                            @Nonnull Player menuPlayer) {
                        return new TraderMenu(containerId, playerInventory, traderId, self.getId());
                    }
                }, (FriendlyByteBuf buf) -> {
                    buf.writeUtf(traderId);
                    buf.writeVarInt(self.getId());
                    
                    // Write buyable items and tags for client-side validation
                    TraderData data = getTraderData();
                    Set<ResourceLocation> buyItems = new HashSet<>();
                    Set<ResourceLocation> buyTags = new HashSet<>();
                    
                    if (data != null) {
                        for (TraderBuyEntry entry : data.getBuyEntries()) {
                            if (entry.isTagBased() && entry.getTagId() != null) {
                                buyTags.add(entry.getTagId());
                            } else if (entry.getItemId() != null) {
                                buyItems.add(entry.getItemId());
                            }
                        }
                    }
                    
                    buf.writeVarInt(buyItems.size());
                    for (ResourceLocation itemId : buyItems) {
                        buf.writeResourceLocation(itemId);
                    }
                    
                    buf.writeVarInt(buyTags.size());
                    for (ResourceLocation tagId : buyTags) {
                        buf.writeResourceLocation(tagId);
                    }
                });
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("TraderId", getTraderId());
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TraderId")) {
            setTraderId(tag.getString("TraderId"));
        }
    }
}
