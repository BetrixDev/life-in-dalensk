package dev.betrix.exclusioncraft.registry;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.items.SplintItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExclusionCraft.MODID);

    public static final RegistryObject<Item> SPLINT =
            ITEMS.register("splint", () -> new SplintItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
