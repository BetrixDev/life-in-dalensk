package dev.betrix.lifeindalensk.registry;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.item.AnalginPainkillersItem;
import dev.betrix.lifeindalensk.item.BandageItem;
import dev.betrix.lifeindalensk.item.IbuprofenItem;
import dev.betrix.lifeindalensk.item.SplintItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SPLINT = register("splint", new SplintItem(new Item.Settings().maxCount(4)));

    public static final Item ANALGIN_PAINKILLERS = register("analgin_painkillers",
            new AnalginPainkillersItem(new Item.Settings().maxCount(4)));

    public static final Item IBUPROFEN = register("ibuprofen",
            new IbuprofenItem(new Item.Settings().maxCount(8)));

    public static final Item BANDAGE = register("bandage",
            new BandageItem(new Item.Settings().maxCount(4)));

    public static final Item LIGHT_BULB = register("light_bulb", new Item(new Item.Settings()));
    public static final Item NUTS = register("nuts", new Item(new Item.Settings()));
    public static final Item BOLTS = register("bolts", new Item(new Item.Settings()));
    public static final Item SCREWS = register("screws", new Item(new Item.Settings()));
    public static final Item PIPE = register("pipe", new Item(new Item.Settings()));
    public static final Item DUCT_TAPE = register("duct_tape", new Item(new Item.Settings()));
    public static final Item PAINTERS_TAPE = register("painters_tape", new Item(new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(LifeInDalensk.MOD_ID, name), item);
    }

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod items for " + LifeInDalensk.MOD_ID);

        // Add items to creative tabs
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(SPLINT);
            content.add(ANALGIN_PAINKILLERS);
            content.add(IBUPROFEN);
            content.add(BANDAGE);
            content.add(LIGHT_BULB);
            content.add(NUTS);
            content.add(BOLTS);
            content.add(SCREWS);
            content.add(PIPE);
            content.add(DUCT_TAPE);
            content.add(PAINTERS_TAPE);
        });
    }
}
