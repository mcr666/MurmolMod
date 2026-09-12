package net.mcr.murmol.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcr.murmol.entity.AlfarEntity;
import net.mcr.murmol.MurmolMod;

public class AlfarspttProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double i = 0;
		if (entity instanceof AlfarEntity _datEntL0 && _datEntL0.getEntityData().get(AlfarEntity.DATA_spattack)) {
			if (Mth.nextInt(RandomSource.create(), 1, 150) == 1) {
				if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) != null) {
					if (entity instanceof AlfarEntity _datEntSetL)
						_datEntSetL.getEntityData().set(AlfarEntity.DATA_spattack, false);
					i = 20;
					for (int _i1 = 0; _i1 < 5; _i1++) {
						MurmolMod.queueServerWork((int) i, () -> {
							if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) != null) {
								if (world instanceof ServerLevel _level) {
									LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
									entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getX(),
											(entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getY(), (entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null).getZ())));;
									_level.addFreshEntity(entityToSpawn);
								}
							}
						});
						i = i + 20;
					}
					MurmolMod.queueServerWork(200, () -> {
						if (entity instanceof AlfarEntity _datEntSetL)
							_datEntSetL.getEntityData().set(AlfarEntity.DATA_spattack, true);
					});
				}
			}
		}
	}
}