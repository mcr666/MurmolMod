package net.mcr.murmol.procedures;

import net.minecraft.world.entity.Entity;

public class GivefireProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.igniteForSeconds(3);
	}
}