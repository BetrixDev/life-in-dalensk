package dev.betrix.lifeindalensk.currency;

import dev.betrix.lifeindalensk.extraction.PlayerExtractionComponent;
import dev.betrix.lifeindalensk.extraction.PlayerExtractionComponentImpl;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

/**
 * Initializer for Cardinal Components API.
 * Registers player components for currency, trader stock, and extraction.
 */
public class ModComponents implements EntityComponentInitializer {

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Register currency component for all players
        // ALWAYS_COPY ensures currency persists across deaths
        registry.registerForPlayers(
                PlayerCurrencyComponent.KEY,
                PlayerCurrencyComponentImpl::new,
                RespawnCopyStrategy.ALWAYS_COPY);

        // Register trader stock component for all players
        // ALWAYS_COPY ensures stock data persists across deaths
        registry.registerForPlayers(
                PlayerTraderStockComponent.KEY,
                PlayerTraderStockComponentImpl::new,
                RespawnCopyStrategy.ALWAYS_COPY);

        // Register extraction component for all players
        // NEVER_COPY because extraction state should reset on death
        registry.registerForPlayers(
                PlayerExtractionComponent.KEY,
                PlayerExtractionComponentImpl::new,
                RespawnCopyStrategy.NEVER_COPY);
    }
}
