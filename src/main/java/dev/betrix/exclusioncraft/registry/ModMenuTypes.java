package dev.betrix.exclusioncraft.registry;

import dev.betrix.exclusioncraft.ExclusionCraft;
import dev.betrix.exclusioncraft.inventory.SearchableContainerMenu;
import dev.betrix.exclusioncraft.inventory.TraderMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ExclusionCraft.MODID);

    public static final RegistryObject<MenuType<SearchableContainerMenu>> SEARCHABLE_CONTAINER =
            MENU_TYPES.register("searchable_container",
                    () -> IForgeMenuType.create(SearchableContainerMenu::new));

    public static final RegistryObject<MenuType<TraderMenu>> TRADER_MENU =
            MENU_TYPES.register("trader_menu",
                    () -> IForgeMenuType.create(TraderMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
