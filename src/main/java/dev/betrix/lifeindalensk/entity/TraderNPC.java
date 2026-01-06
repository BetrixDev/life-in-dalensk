package dev.betrix.lifeindalensk.entity;

import dev.betrix.lifeindalensk.inventory.TraderScreenHandler;
import dev.betrix.lifeindalensk.trader.TraderBuyEntry;
import dev.betrix.lifeindalensk.trader.TraderData;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class TraderNPC extends PathAwareEntity {

    private static final TrackedData<String> TRADER_ID = DataTracker.registerData(TraderNPC.class,
            TrackedDataHandlerRegistry.STRING);

    public TraderNPC(EntityType<? extends TraderNPC> entityType, World world) {
        super(entityType, world);
        this.setInvulnerable(true);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createTraderAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TRADER_ID, "");
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    }

    public void setTraderId(String traderId) {
        this.dataTracker.set(TRADER_ID, traderId);
        TraderData data = TraderRegistry.getInstance().getTrader(traderId);
        if (data != null) {
            this.setCustomName(Text.literal(data.getName()));
            this.setCustomNameVisible(true);
        }
    }

    public String getTraderId() {
        return this.dataTracker.get(TRADER_ID);
    }

    public TraderData getTraderData() {
        return TraderRegistry.getInstance().getTrader(getTraderId());
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient && hand == Hand.MAIN_HAND) {
            String traderId = getTraderId();
            if (!traderId.isEmpty() && player instanceof ServerPlayerEntity serverPlayer) {
                player.openHandledScreen(new TraderScreenHandlerFactory(this, traderId));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.success(this.getWorld().isClient);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void pushAway(net.minecraft.entity.Entity entity) {
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("TraderId", getTraderId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("TraderId")) {
            setTraderId(nbt.getString("TraderId"));
        }
    }

    private record TraderScreenHandlerFactory(TraderNPC entity, String traderId)
            implements ExtendedScreenHandlerFactory<TraderScreenHandler.TraderMenuData> {

        @Override
        public TraderScreenHandler.TraderMenuData getScreenOpeningData(ServerPlayerEntity player) {
            return new TraderScreenHandler.TraderMenuData(traderId, entity.getId());
        }

        @Override
        public Text getDisplayName() {
            TraderData data = entity.getTraderData();
            return Text.literal(data != null ? data.getName() : "Trader");
        }

        @Override
        @Nullable
        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new TraderScreenHandler(syncId, playerInventory, traderId, entity.getId());
        }
    }
}
