package net.mcr.murmol.procedures;

import net.minecraft.world.entity.Entity;

public class FeralCatswimProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity.isSwimming()) {
			if (entity.getDeltaMovement().x() != 0) {
				return true;
			} else if (entity.getDeltaMovement().z() != 0) {
				return true;
			}
		}
		return false;
	}
}