package dev.betrix.exclusioncraft.currency;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerCurrency implements INBTSerializable<CompoundTag> {

    private long roubles;

    public PlayerCurrency() {
        this.roubles = 0;
    }

    public long getRoubles() {
        return roubles;
    }

    public void setRoubles(long roubles) {
        this.roubles = Math.max(0, roubles);
    }

    public void addRoubles(long amount) {
        setRoubles(this.roubles + amount);
    }

    public boolean subtractRoubles(long amount) {
        if (this.roubles >= amount) {
            this.roubles -= amount;
            return true;
        }
        return false;
    }

    public boolean canAfford(long amount) {
        return this.roubles >= amount;
    }

    public void copyFrom(PlayerCurrency other) {
        this.roubles = other.roubles;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("roubles", roubles);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        roubles = tag.getLong("roubles");
    }
}
