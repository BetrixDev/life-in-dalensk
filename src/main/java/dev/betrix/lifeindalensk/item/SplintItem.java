package dev.betrix.lifeindalensk.item;

import dev.betrix.lifeindalensk.registry.ModEffects;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SplintItem extends Item {

    public static final FoodComponent SPLINT_FOOD = new FoodComponent.Builder()
            .alwaysEdible()
            .snack()
            .build();

    public SplintItem(Settings settings) {
        super(settings.food(SPLINT_FOOD));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            player.removeStatusEffect(ModEffects.BROKEN_LEG);
        }
        return super.finishUsing(stack, world, user);
    }
}
