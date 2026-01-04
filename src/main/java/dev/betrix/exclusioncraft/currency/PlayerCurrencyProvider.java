package dev.betrix.exclusioncraft.currency;

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

public class PlayerCurrencyProvider implements ICapabilitySerializable<CompoundTag> {

    public static final ResourceLocation IDENTIFIER = new ResourceLocation(ExclusionCraft.MODID, "currency");

    public static final Capability<PlayerCurrency> PLAYER_CURRENCY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private final PlayerCurrency currency = new PlayerCurrency();
    private final LazyOptional<PlayerCurrency> optional = LazyOptional.of(() -> currency);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PLAYER_CURRENCY.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return currency.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        currency.deserializeNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }

    /**
     * Helper method to get a player's currency capability.
     * Works on both client and server.
     */
    public static LazyOptional<PlayerCurrency> get(Player player) {
        return player.getCapability(PLAYER_CURRENCY);
    }

    /**
     * Helper method to get the rouble count directly.
     * Returns 0 if capability is not present.
     */
    public static long getRoubles(Player player) {
        return get(player).map(PlayerCurrency::getRoubles).orElse(0L);
    }
}
