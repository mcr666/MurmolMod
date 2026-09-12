package net.mcr.murmol.procedures;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;

public class AlfarattdistanceProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) != null) {
			if (((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).position()).distanceTo((entity.position())) >= 3) {
				return true;
			}
			return false;
		}
		return true;
	}
}