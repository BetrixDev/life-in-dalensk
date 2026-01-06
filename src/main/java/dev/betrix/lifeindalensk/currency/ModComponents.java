package dev.betrix.lifeindalensk.currency;

import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

/**
 * Initializer for Cardinal Components API.
 * Registers player components for currency and trader stock.
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
    }
}
