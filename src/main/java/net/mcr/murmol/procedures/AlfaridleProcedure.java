package net.mcr.murmol.procedures;

import net.minecraft.world.entity.Entity;

public class AlfaridleProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getDeltaMovement()).length() <= 0.01) {
			return true;
		}
		return false;
	}
}