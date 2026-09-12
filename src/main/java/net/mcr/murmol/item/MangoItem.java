package net.mcr.murmol.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;

import net.mcr.murmol.procedures.MangoDiaoLuoWuXiaoShiShiProcedure;

public class MangoItem extends Item {
	public MangoItem() {
		super(new Item.Properties().food((new FoodProperties.Builder()).nutrition(3).saturationModifier(0.3f).build()));
	}

	@Override
	public void onDestroyed(ItemEntity entity, DamageSource damagesource) {
		super.onDestroyed(entity, damagesource);
		MangoDiaoLuoWuXiaoShiShiProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}
}