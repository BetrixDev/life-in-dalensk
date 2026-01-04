package dev.betrix.exclusioncraft.items;

import dev.betrix.exclusioncraft.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SplintItem extends Item {

    public static final FoodProperties SPLINT_FOOD = new FoodProperties.Builder()
            .alwaysEat()
            .fast()
            .build();

    public SplintItem(Properties properties) {
        super(properties.food(SPLINT_FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            player.removeEffect(ModEffects.BROKEN_LEG.get());
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
