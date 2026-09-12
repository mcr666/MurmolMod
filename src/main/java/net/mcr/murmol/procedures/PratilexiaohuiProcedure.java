package net.mcr.murmol.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.SimpleParticleType;

import net.mcr.murmol.init.MurmolModParticleTypes;

public class PratilexiaohuiProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (world instanceof ServerLevel _level)
			_level.sendParticles((SimpleParticleType) (MurmolModParticleTypes.EIGHTPARTICLE.get()), x, y, z, 5, 1, 1, 1, 0.1);
	}
}