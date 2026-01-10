package dev.betrix.lifeindalensk.extraction;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * Component interface for tracking player extraction countdown state.
 * Manages the 10-second countdown when a player is at an extraction point.
 */
public interface PlayerExtractionComponent extends AutoSyncedComponent {

    ComponentKey<PlayerExtractionComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(LifeInDalensk.MOD_ID, "extraction"),
            PlayerExtractionComponent.class);

    /**
     * Checks if the player is currently in extraction countdown.
     */
    boolean isExtracting();

    /**
     * Gets the remaining extraction time in ticks.
     */
    int getExtractionTicks();

    /**
     * Gets the extraction point position where countdown started.
     */
    BlockPos getExtractionPoint();

    /**
     * Starts the extraction countdown at the given position.
     * @param pos The extraction point position
     */
    void startExtraction(BlockPos pos);

    /**
     * Cancels the current extraction countdown.
     */
    void cancelExtraction();

    /**
     * Ticks the extraction countdown. Returns true when extraction is complete.
     */
    boolean tickExtraction();
}
