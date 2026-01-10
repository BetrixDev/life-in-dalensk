package dev.betrix.lifeindalensk.extraction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/**
 * Implementation of PlayerExtractionComponent.
 * Tracks extraction countdown state for a player.
 */
public class PlayerExtractionComponentImpl implements PlayerExtractionComponent {

    private static final int EXTRACTION_TIME_TICKS = 200; // 10 seconds (20 ticks/second)
    private static final String NBT_EXTRACTING = "extracting";
    private static final String NBT_TICKS = "extractionTicks";
    private static final String NBT_POS_X = "extractionX";
    private static final String NBT_POS_Y = "extractionY";
    private static final String NBT_POS_Z = "extractionZ";

    private final PlayerEntity player;
    private boolean extracting;
    private int extractionTicks;
    private BlockPos extractionPoint;

    public PlayerExtractionComponentImpl(PlayerEntity player) {
        this.player = player;
        this.extracting = false;
        this.extractionTicks = 0;
        this.extractionPoint = BlockPos.ORIGIN;
    }

    @Override
    public boolean isExtracting() {
        return extracting;
    }

    @Override
    public int getExtractionTicks() {
        return extractionTicks;
    }

    @Override
    public BlockPos getExtractionPoint() {
        return extractionPoint;
    }

    @Override
    public void startExtraction(BlockPos pos) {
        this.extracting = true;
        this.extractionTicks = EXTRACTION_TIME_TICKS;
        this.extractionPoint = pos;
        KEY.sync(player);
    }

    @Override
    public void cancelExtraction() {
        this.extracting = false;
        this.extractionTicks = 0;
        this.extractionPoint = BlockPos.ORIGIN;
        KEY.sync(player);
    }

    @Override
    public boolean tickExtraction() {
        if (!extracting) {
            return false;
        }

        extractionTicks--;
        
        // Sync every 5 ticks (4 times per second) for smooth countdown display
        if (extractionTicks % 5 == 0) {
            KEY.sync(player);
        }

        if (extractionTicks <= 0) {
            extracting = false;
            KEY.sync(player);
            return true; // Extraction complete
        }

        return false;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        extracting = tag.getBoolean(NBT_EXTRACTING);
        extractionTicks = tag.getInt(NBT_TICKS);
        if (tag.contains(NBT_POS_X)) {
            extractionPoint = new BlockPos(
                    tag.getInt(NBT_POS_X),
                    tag.getInt(NBT_POS_Y),
                    tag.getInt(NBT_POS_Z));
        } else {
            extractionPoint = BlockPos.ORIGIN;
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean(NBT_EXTRACTING, extracting);
        tag.putInt(NBT_TICKS, extractionTicks);
        tag.putInt(NBT_POS_X, extractionPoint.getX());
        tag.putInt(NBT_POS_Y, extractionPoint.getY());
        tag.putInt(NBT_POS_Z, extractionPoint.getZ());
    }
}
