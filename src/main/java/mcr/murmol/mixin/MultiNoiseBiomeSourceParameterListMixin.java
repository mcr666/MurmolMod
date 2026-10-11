package mcr.murmol.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

import mcr.murmol.worldgen.WorldGenRuleContext;

/**
 * 构建噪声参数列表前确保线程上下文存在：新建世界路径由 WorldOpenFlowsMixin 提前压入
 * 规则值；其他路径（加载存档时 ParameterList 已随 level 数据缓存，通常不走此构造）
 * 若无上下文，provider 内部使用默认值 2。此处仅负责构建后清理，避免线程复用污染。
 */
@Mixin(MultiNoiseBiomeSourceParameterList.class)
public class MultiNoiseBiomeSourceParameterListMixin {

	@Inject(method = "<init>(Lnet/minecraft/world/level/biome/MultiNoiseBiomeSourceParameterList$Preset;Lnet/minecraft/core/HolderGetter;)V", at = @At("RETURN"))
	private void murmol$popRuleContext(MultiNoiseBiomeSourceParameterList.Preset preset, HolderGetter<?> holderGetter, CallbackInfo ci) {
		WorldGenRuleContext.clear();
	}
}
