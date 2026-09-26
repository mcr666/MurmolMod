package net.mcr.astralcruse.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;

import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.murmol.feral.client.FeralFormModels;

/**
 * 参考 Changed mod 的渲染器偷换方案：劫持渲染器获取，
 * 整体替换形态（如文鳐）的玩家直接换成专属渲染器，原版人形渲染完全不执行。
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

	@WrapMethod(method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;")
	private EntityRenderer<?> astralCruse$overridePlayerRenderer(Entity entity, Operation<EntityRenderer<?>> original) {
		if (entity instanceof AbstractClientPlayer player
				&& FeralFormManager.getForm(player).getWholeModelPart() != null) {
			return FeralFormModels.getWenyaoRenderer(FeralFormModels.isSlim(player));
		}
		return original.call(entity);
	}
}
