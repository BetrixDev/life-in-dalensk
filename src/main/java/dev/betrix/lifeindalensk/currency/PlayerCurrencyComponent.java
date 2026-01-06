package dev.betrix.lifeindalensk.currency;

import dev.betrix.lifeindalensk.LifeInDalensk;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * Component interface for player currency data.
 * Uses Cardinal Components API for data persistence.
 */
public interface PlayerCurrencyComponent extends AutoSyncedComponent {

    ComponentKey<PlayerCurrencyComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(LifeInDalensk.MOD_ID, "currency"),
            PlayerCurrencyComponent.class);

    long getRoubles();

    void setRoubles(long roubles);

    void addRoubles(long amount);

    boolean subtractRoubles(long amount);

    boolean canAfford(long amount);
}
