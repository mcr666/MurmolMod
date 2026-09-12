package net.mcr.murmol.procedures;

import net.minecraft.world.item.ItemStack;

public class UseiceboltProcedure {
	public static void execute(ItemStack itemstack) {
		itemstack.shrink(1);
	}
}