package net.mcr.murmol.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.mcr.murmol.init.MurmolModBiomes;

/**
 * 出生点搜索黑名单：出生点候选区块若为星幻之地群系，则跳过该区块，
 * 使原版的螺旋搜索（±5 区块）只会落在正常群系上。
 */
@Mixin(PlayerRespawnLogic.class)
public abstract class PlayerRespawnLogicMixin {
	@Inject(method = "getSpawnPosInChunk", at = @At("HEAD"), cancellable = true)
	private static void murmol$skipAstralSpawnChunk(ServerLevel level, ChunkPos pos, CallbackInfoReturnable<BlockPos> cir) {
		BlockPos probe = pos.getMiddleBlockPosition(level.getSeaLevel());
		if (level.getBiome(probe).is(MurmolModBiomes.ASTRAL_INFECTION_BIOME))
			cir.setReturnValue(null);
	}
}
