package mcr.murmol.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import mcr.murmol.init.MurmolModMobEffects;

public class MangoAstralItem extends Item {
	public MangoAstralItem() {
		super(new Item.Properties().food((new FoodProperties.Builder()).nutrition(5).saturationModifier(0.5f).build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		if (!world.isClientSide()) {
			// 等同附魔金苹果：再生II 20s + 抗性I 300s + 防火I 300s + 吸收IV 120s
			entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
			entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));
			entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
			entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
			// 额外：10s 星幻感染 I
			entity.addEffect(new MobEffectInstance(MurmolModMobEffects.ASTRAL_INFECTION, 200, 0));
		}
		return retval;
	}
}
