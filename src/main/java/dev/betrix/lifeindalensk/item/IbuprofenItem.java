package dev.betrix.lifeindalensk.item;

import dev.betrix.lifeindalensk.registry.ModEffects;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class IbuprofenItem extends Item {

    public static final FoodComponent IBUPROFEN_FOOD = new FoodComponent.Builder()
            .alwaysEdible()
            .build();

    public IbuprofenItem(Settings settings) {
        super(settings.food(IBUPROFEN_FOOD));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            if (player.hasStatusEffect(ModEffects.PAINKILLER)) {
                player.removeStatusEffect(ModEffects.PAINKILLER);
            }

            // Give player a painkiller effect for 360 seconds and hunger for 3 seconds
            player.addStatusEffect(new StatusEffectInstance(ModEffects.PAINKILLER, 20 * 360, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 3, 0));
        }

        return super.finishUsing(stack, world, user);
    }
}
