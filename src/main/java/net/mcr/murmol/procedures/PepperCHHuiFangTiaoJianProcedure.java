package net.mcr.murmol.procedures;

import net.minecraft.world.entity.Entity;

public class PepperCHHuiFangTiaoJianProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return (entity.getDeltaMovement()).length() <= 0.01;
	}
}