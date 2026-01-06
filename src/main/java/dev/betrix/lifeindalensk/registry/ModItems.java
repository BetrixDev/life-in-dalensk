package dev.betrix.lifeindalensk.registry;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.item.SplintItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SPLINT = register("splint", new SplintItem(new Item.Settings().maxCount(4)));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(LifeInDalensk.MOD_ID, name), item);
    }

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod items for " + LifeInDalensk.MOD_ID);

        // Add items to creative tabs
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(SPLINT);
        });
    }
}
