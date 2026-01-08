package dev.betrix.lifeindalensk.item;

import dev.betrix.lifeindalensk.registry.ModEffects;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * A bandage item that removes the bleeding effect when used.
 * Can be purchased from the Pharmacist for 100 roubles.
 */
public class BandageItem extends Item {

    public static final FoodComponent BANDAGE_FOOD = new FoodComponent.Builder()
            .alwaysEdible()
            .build();

    public BandageItem(Settings settings) {
        super(settings.food(BANDAGE_FOOD));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            // Remove bleeding effect if the player has it
            if (player.hasStatusEffect(ModEffects.BLEEDING)) {
                player.removeStatusEffect(ModEffects.BLEEDING);
            }
        }

        return super.finishUsing(stack, world, user);
    }
}
