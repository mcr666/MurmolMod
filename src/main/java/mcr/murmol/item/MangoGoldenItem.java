package mcr.murmol.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class MangoGoldenItem extends Item {
	public MangoGoldenItem() {
		super(new Item.Properties().food((new FoodProperties.Builder()).nutrition(5).saturationModifier(0.5f).alwaysEdible().build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		if (!world.isClientSide()) {
			// 对标原版金苹果：再生II 5s + 吸收I 120s
			entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
			entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
		}
		return retval;
	}
}