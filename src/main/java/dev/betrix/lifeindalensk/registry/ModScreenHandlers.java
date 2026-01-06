package dev.betrix.lifeindalensk.registry;

import dev.betrix.lifeindalensk.LifeInDalensk;
import dev.betrix.lifeindalensk.inventory.SearchableContainerScreenHandler;
import dev.betrix.lifeindalensk.inventory.TraderScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {

    public static ScreenHandlerType<SearchableContainerScreenHandler> SEARCHABLE_CONTAINER;
    public static ScreenHandlerType<TraderScreenHandler> TRADER_MENU;

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod screen handlers for " + LifeInDalensk.MOD_ID);

        SEARCHABLE_CONTAINER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(LifeInDalensk.MOD_ID, "searchable_container"),
                new ExtendedScreenHandlerType<>(SearchableContainerScreenHandler::new, BlockPos.PACKET_CODEC));

        TRADER_MENU = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(LifeInDalensk.MOD_ID, "trader_menu"),
                new ExtendedScreenHandlerType<>(TraderScreenHandler::new, TraderScreenHandler.CODEC));
    }
}
