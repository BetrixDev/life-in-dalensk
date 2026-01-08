package dev.betrix.lifeindalensk;

import dev.betrix.lifeindalensk.command.ModCommands;
import dev.betrix.lifeindalensk.event.PlayerEvents;
import dev.betrix.lifeindalensk.event.ServerEvents;
import dev.betrix.lifeindalensk.network.ModNetworking;
import dev.betrix.lifeindalensk.registry.ModBlockEntities;
import dev.betrix.lifeindalensk.registry.ModBlocks;
import dev.betrix.lifeindalensk.registry.ModEffects;
import dev.betrix.lifeindalensk.registry.ModEntityTypes;
import dev.betrix.lifeindalensk.registry.ModItems;
import dev.betrix.lifeindalensk.registry.ModScreenHandlers;
import dev.betrix.lifeindalensk.trader.TraderRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifeInDalensk implements ModInitializer {

    public static final String MOD_ID = "lifeindalensk";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Life in Dalensk");

        // Register all mod content
        ModEffects.register();
        ModItems.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();
        ModEntityTypes.register();
        
        // Register networking (both C2S and S2C packet types)
        ModNetworking.registerC2SPackets();
        ModNetworking.registerS2CPackets();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher);
        });
        
        // Register trader data reload listener
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(TraderRegistry.getInstance());
        
        // Register player events
        PlayerEvents.register();
        
        // Register server events
        ServerEvents.register();
        
        LOGGER.info("Life in Dalensk initialized");
    }
}
