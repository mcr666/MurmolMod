package mcr.astralcruse.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;

import mcr.murmol.init.MurmolModGameRules;
import mcr.murmol.worldgen.WorldGenRuleContext;

/**
 * 新建世界时把待创建世界 GameRules 中的幻星瘟疫噪声范围压入线程上下文，
 * 使该世界的 MultiNoiseBiomeSourceParameterList 按界面/规则设定的范围构建。
 * createFreshLevel 在渲染线程提交世界生成任务（世界数据组装在其调用链内的后台线程，
 * 但 LevelSettings 此处已可读），用静态 ThreadLocal 桥接；ParameterList 构建后由
 * MultiNoiseBiomeSourceParameterListMixin 清理。
 */
@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {

	@Inject(method = "createFreshLevel", at = @At("HEAD"))
	private void murmol$pushInfectionRange(String levelName, LevelSettings settings, WorldOptions options,
			java.util.function.Function<RegistryAccess, RegistryAccess.Frozen> dimensionSettings, Screen parent, CallbackInfo ci) {
		if (settings != null) {
			WorldGenRuleContext.setInfectionRange(
					MurmolModGameRules.astralInfectionRangeForPreset(
							settings.gameRules().getInt(MurmolModGameRules.ASTRAL_INFECTION_RANGE)));
		}
	}
}
