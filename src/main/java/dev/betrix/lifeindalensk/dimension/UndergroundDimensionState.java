package dev.betrix.lifeindalensk.dimension;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

public class UndergroundDimensionState extends PersistentState {
    public static final Identifier DATA_KEY = Identifier.of(LifeInDalensk.MOD_ID, "underground_dimension_state");

    private boolean initialized = false;

    private UndergroundDimensionState() {
    }

    public static UndergroundDimensionState getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new Factory<>(UndergroundDimensionState::new, UndergroundDimensionState::fromNbt, null),
                DATA_KEY);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void markInitialized() {
        this.initialized = true;
        this.markDirty();
    }

    private static UndergroundDimensionState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        UndergroundDimensionState state = new UndergroundDimensionState();
        state.initialized = nbt.getBoolean("initialized");
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean("initialized", initialized);
        return nbt;
    }
}
