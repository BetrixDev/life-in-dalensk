package dev.betrix.lifeindalensk.currency;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * Component interface for per-player trader stock data.
 * Tracks stock levels and restock timers for each trader per-player.
 */
public interface PlayerTraderStockComponent extends AutoSyncedComponent, ServerTickingComponent {

    ComponentKey<PlayerTraderStockComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(LifeInDalensk.MOD_ID, "trader_stock"),
            PlayerTraderStockComponent.class);

    /**
     * Get the current stock for a trader offer.
     */
    int getStock(String traderId, int offerIndex);

    /**
     * Decrement stock for a trader offer.
     */
    void decrementStock(String traderId, int offerIndex, int amount);

    /**
     * Get the next restock time for a trader (game time).
     */
    long getRestockTime(String traderId);

    /**
     * Get all stocks for a trader as an array.
     */
    int[] getAllStocks(String traderId);
}
