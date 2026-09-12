package net.mcr.murmol.item;

import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.MurmolMod;

public class PetalItem extends Item {
	public PetalItem() {
		super(new Item.Properties().food((new FoodProperties.Builder()).nutrition(1).saturationModifier(0.3f).alwaysEdible().build()).attributes(
				ItemAttributeModifiers.builder().add(Attributes.LUCK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "petal_0"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND).build()));
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		return 20;
	}
}