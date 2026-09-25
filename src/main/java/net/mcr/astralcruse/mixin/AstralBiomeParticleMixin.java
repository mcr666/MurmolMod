package net.mcr.astralcruse.mixin;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端配置开关：关闭时不再生成星界侵蚀群系的环境粒子（murmol:astral_effect）。
 * 直接在 AmbientParticleSettings.canSpawn 内部拦截，只对本 mod 命名空间的粒子生效，
 * 不触碰 ClientLevel（避免与 Sodium 等大量混入该类的 Mixin 冲突）。
 */
@Mixin(AmbientParticleSettings.class)
public abstract class AstralBiomeParticleMixin {

	@Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
	private void astralCruse$gateBiomeParticles(RandomSource random, CallbackInfoReturnable<Boolean> cir) {
		if (!net.mcr.murmol.MurmolModConfig.RENDER_BIOME_PARTICLES.get() && astralCruse$isMurmolParticle()) {
			cir.setReturnValue(false);
		}
	}

	private boolean astralCruse$isMurmolParticle() {
		ParticleOptions options = ((AmbientParticleSettings) (Object) this).getOptions();
		return net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE
				.getKey(options.getType()).getNamespace().equals("murmol");
	}
}
