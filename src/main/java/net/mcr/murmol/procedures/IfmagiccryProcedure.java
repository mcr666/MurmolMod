package net.mcr.murmol.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;

public class IfmagiccryProcedure {
	public static boolean execute(ItemStack itemstack) {
		if (itemstack.is(ItemTags.create(ResourceLocation.parse("murmol:magic_crystal")))) {
			return false;
		}
		return true;
	}
}