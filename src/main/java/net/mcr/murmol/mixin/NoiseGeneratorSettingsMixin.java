package net.mcr.murmol.mixin;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.core.Holder;

import net.mcr.murmol.init.MurmolModBiomes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements MurmolModBiomes.MurmolModNoiseGeneratorSettings {
	@Unique
	private Holder<DimensionType> murmol_dimensionTypeReference;

	@WrapMethod(method = "surfaceRule")
	public SurfaceRules.RuleSource surfaceRule(Operation<SurfaceRules.RuleSource> original) {
		SurfaceRules.RuleSource retval = original.call();
		if (this.murmol_dimensionTypeReference != null) {
			retval = MurmolModBiomes.adaptSurfaceRule(retval, this.murmol_dimensionTypeReference);
		}
		return retval;
	}

	@Override
	public void setmurmolDimensionTypeReference(Holder<DimensionType> dimensionType) {
		this.murmol_dimensionTypeReference = dimensionType;
	}
}