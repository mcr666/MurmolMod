package net.mcr.murmol.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.particles.SimpleParticleType;

import net.mcr.murmol.init.MurmolModParticleTypes;

public class AlfaSpitProjDanSheWuFeiXingShiMeiKeFaShengProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		world.addParticle((SimpleParticleType) (MurmolModParticleTypes.SPITEFFECT.get()), x, y, z, 0.1, 0, 0.1);
	}
}