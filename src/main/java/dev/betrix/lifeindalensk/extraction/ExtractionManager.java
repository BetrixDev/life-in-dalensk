package dev.betrix.lifeindalensk.extraction;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.dimension.UndergroundDimensionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Manages extraction points and player extraction countdown logic.
 * Handles checking if players are in extraction zones, starting/canceling countdowns,
 * and teleporting players when extraction completes.
 */
public class ExtractionManager {

    // Distance from extraction point center where countdown is active
    private static final double EXTRACTION_RADIUS = 3.0;
    
    // Maximum vertical distance from extraction point
    private static final double EXTRACTION_HEIGHT = 3.0;

    /**
     * Ticks extraction logic for a player.
     * Checks if player is in an extraction zone and manages countdown state.
     * 
     * @param player The player to tick
     */
    public static void tickPlayerExtraction(ServerPlayerEntity player) {
        // Only process in overworld
        if (!player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
            return;
        }

        PlayerExtractionComponent extraction = PlayerExtractionComponent.KEY.get(player);
        BlockPos playerPos = player.getBlockPos();

        // Check if player is currently extracting
        if (extraction.isExtracting()) {
            BlockPos extractionPoint = extraction.getExtractionPoint();
            
            // Check if player moved away from extraction point
            if (!isPlayerInExtractionZone(playerPos, extractionPoint)) {
                extraction.cancelExtraction();
                LifeInDalensk.LOGGER.debug("Player {} moved away from extraction point, canceling extraction",
                        player.getName().getString());
                return;
            }

            // Tick the extraction countdown
            boolean complete = extraction.tickExtraction();
            if (complete) {
                // Teleport player to underground dimension
                LifeInDalensk.LOGGER.info("Player {} extraction complete, teleporting to underground",
                        player.getName().getString());
                UndergroundDimensionManager.teleportToSpawn(player);
            }
        } else {
            // Check if player entered an extraction zone
            BlockPos nearestExtractionPoint = findNearestExtractionPoint(player, playerPos);
            if (nearestExtractionPoint != null) {
                extraction.startExtraction(nearestExtractionPoint);
                LifeInDalensk.LOGGER.info("Player {} started extraction at {}",
                        player.getName().getString(), nearestExtractionPoint);
            }
        }
    }

    /**
     * Checks if a player position is within extraction zone of an extraction point.
     */
    private static boolean isPlayerInExtractionZone(BlockPos playerPos, BlockPos extractionPoint) {
        double dx = playerPos.getX() - extractionPoint.getX();
        double dy = playerPos.getY() - extractionPoint.getY();
        double dz = playerPos.getZ() - extractionPoint.getZ();
        
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        
        return horizontalDistance <= EXTRACTION_RADIUS && Math.abs(dy) <= EXTRACTION_HEIGHT;
    }

    /**
     * Finds the nearest extraction point to the player's current position.
     * Returns null if no extraction point is nearby.
     * 
     * For now, this is a placeholder that will be replaced with structure-based detection.
     * You can add specific BlockPos coordinates here or implement structure detection.
     */
    private static BlockPos findNearestExtractionPoint(ServerPlayerEntity player, BlockPos playerPos) {
        // TODO: Implement structure-based extraction point detection
        // For now, you can manually add extraction point coordinates here for testing
        // Example: if (isPlayerInExtractionZone(playerPos, new BlockPos(100, 64, 100))) { return new BlockPos(100, 64, 100); }
        
        // This will be implemented when bunker structures are added
        return null;
    }

    /**
     * Registers a specific block position as an extraction point.
     * This can be called when bunker structures generate.
     * 
     * @param world The world
     * @param pos The extraction point position
     */
    public static void registerExtractionPoint(World world, BlockPos pos) {
        // TODO: Store extraction points in a world-based component or persistent state
        // For now, extraction points will need to be hardcoded or structure-based
        LifeInDalensk.LOGGER.info("Registered extraction point at {}", pos);
    }
}
