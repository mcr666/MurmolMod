package net.mcr.murmol.feral;

import java.util.List;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.init.MurmolModItems;

/**
 * 文鳐形态（Wenyao）。
 * 特性：悬停飞行（同月蛾）；只能在水中呼吸（陆地上会逐渐缺氧窒息）；
 * 陆地行走极慢，水中游泳迅捷。
 * 渲染：整体替换模型（纯鱼形），渲染端取消原版玩家模型，渲染鱼模型并播放动画。
 */
public class WenyaoForm extends FeralForm {

	public static final String ID = "wenyao";

	public WenyaoForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/wenyao.png"),
				null,
				null,
				null,
				() -> new ItemStack(MurmolModItems.WENYAO_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "get_wenyao"),
				null,
				List.of(
						new ItemStack(Items.PHANTOM_MEMBRANE),
						new ItemStack(Items.SEAGRASS),
						new ItemStack(Items.WATER_BUCKET)));
		// 祭品中的"任意鱼"按 minecraft:fishes 标签匹配
		setTagMaterial(ItemTags.FISHES);
		// 悬停飞行：长按跳跃键缓慢上升，松手自然下落（同月蛾）
		enableHoverFlight();
		// 整模形态（无人类手臂模型）：第一人称隐藏手臂、保留手持物品；第三人称物品见 FeralItemInHandLayerMixin
		enableFirstPersonArm();
		// 碰撞箱高度固定 0.5 格（视角/眼高随尺寸自动降低），宽度保持野性形态默认扩展
		setHitboxHeight(0.5F);
		// 整体替换渲染：纯鱼形模型 + 专属 Bedrock 动画（fish_idle/fish_moving_land/fish_moving_swim/fish_flying）
		setWholeModelLayer(ResourceLocation.fromNamespaceAndPath("murmol", "wenyao"));
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/wenyao_anim.json"));
	}
}
