package mcr.astralcruse.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;

import mcr.murmol.feral.FeralFormManager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 整体替换模型形态（如文鳐）：原版玩家模型部件已隐藏，但附着在人形骨骼上的图层
 * （盔甲/鞘翅/披风/箭矢/头顶方块/鹦鹉等）仍会以原版人形位置渲染，浮在鱼形模型上方
 * （表现为"飘在空中的影子"）。这里在图层遍历处整体跳过，仅保留手持物品层
 * （物品已绑定到鱼模型的 right_item/left_item 挂点骨骼）。
 * 阴影与名牌由 EntityRenderDispatcher/LivingEntityRenderer 其余逻辑渲染，不受影响。
 *
 * 兼容性说明：使用 @WrapOperation 而非 @Redirect——多个 mod 可对同一调用点叠加包装，
 * 避免（如 Dragon Survival 等玩家外观 mod）对同一指令的独占式注入互斥崩溃；
 * require=0 表示注入失败时仅表现为旧版图层残留的视觉问题，不会崩溃。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class FeralWholeModelLayersMixin {

	@WrapOperation(
			method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"),
			require = 0
	)
	private Iterator<RenderLayer<?, ?>> astralCruse$skipWholeModelVanillaLayers(List<RenderLayer<?, ?>> layers, Operation<Iterator<RenderLayer<?, ?>>> original, net.minecraft.world.entity.LivingEntity entity) {
		if (entity instanceof Player player
				&& FeralFormManager.getForm(player).getWholeModelLayer() != null) {
			List<RenderLayer<?, ?>> keep = new ArrayList<>();
			for (RenderLayer<?, ?> layer : layers) {
				if (layer instanceof ItemInHandLayer) {
					keep.add(layer);
				}
			}
			return keep.iterator();
		}
		return original.call(layers);
	}
}
