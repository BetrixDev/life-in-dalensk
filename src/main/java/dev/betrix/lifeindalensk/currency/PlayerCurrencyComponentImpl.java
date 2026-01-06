package dev.betrix.lifeindalensk.currency;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

/**
 * Implementation of PlayerCurrencyComponent.
 */
public class PlayerCurrencyComponentImpl implements PlayerCurrencyComponent {

    private final PlayerEntity player;
    private long roubles;

    public PlayerCurrencyComponentImpl(PlayerEntity player) {
        this.player = player;
        this.roubles = 0;
    }

    @Override
    public long getRoubles() {
        return roubles;
    }

    @Override
    public void setRoubles(long roubles) {
        this.roubles = Math.max(0, roubles);
    }

    @Override
    public void addRoubles(long amount) {
        if (amount > 0) {
            setRoubles(this.roubles + amount);
        }
    }

    @Override
    public boolean subtractRoubles(long amount) {
        if (amount <= 0) {
            return true;
        }
        if (this.roubles >= amount) {
            this.roubles -= amount;
            return true;
        }
        return false;
    }

    @Override
    public boolean canAfford(long amount) {
        return this.roubles >= amount;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.roubles = tag.getLong("roubles");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putLong("roubles", this.roubles);
    }
}
