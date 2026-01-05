package dev.betrix.exclusioncraft.trader;

import dev.betrix.exclusioncraft.ExclusionCraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerTraderStockProvider implements ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation IDENTIFIER = new ResourceLocation(ExclusionCraft.MODID, "trader_stock");

    public static final Capability<PlayerTraderStock> PLAYER_TRADER_STOCK = 
            CapabilityManager.get(new CapabilityToken<>() {});

    private final PlayerTraderStock stock = new PlayerTraderStock();
    private final LazyOptional<PlayerTraderStock> optional = LazyOptional.of(() -> stock);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PLAYER_TRADER_STOCK.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return stock.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        stock.deserializeNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }

    public static LazyOptional<PlayerTraderStock> get(Player player) {
        return player.getCapability(PLAYER_TRADER_STOCK);
    }

    public static int getStock(Player player, String traderId, int offerIndex) {
        return get(player).map(stock -> stock.getStock(traderId, offerIndex)).orElse(0);
    }

    public static long getRestockTime(Player player, String traderId) {
        return get(player).map(stock -> stock.getRestockTime(traderId)).orElse(0L);
    }
}
