package net.mcr.murmol.feral;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.mcr.murmol.feral.client.FeralFormModels;

/**
 * 变形形态的纯数据基类。每个形态是一个子类，在构造函数中声明自身全部数据。
 * 行为逻辑由 {@link FeralFormManager} 统一处理。
 */
public abstract class FeralForm {

	/** 人类形态的字符串 id */
	public static final String HUMAN_ID = "human";

	private final String id;
	private final ResourceLocation texture;
	private final ResourceLocation tailTexture;
	private final ResourceLocation bodyLayer;
	private final ResourceLocation tailLayer;
	private final Supplier<ItemStack> soulItem;
	private final ResourceLocation advancement;
	private final Map<Holder<Attribute>, AttributeModifier> modifiers;
	private final List<ItemStack> transformMaterials;

	/** 变形状态下是否渲染第一人称手臂模型（配合全局配置 renderFirstPersonArm 使用，默认关闭） */
	private boolean showFirstPersonArm = false;

	protected FeralForm(String id, ResourceLocation texture, ResourceLocation tailTexture,
			ResourceLocation bodyLayer, ResourceLocation tailLayer,
			Supplier<ItemStack> soulItem, ResourceLocation advancement,
			Map<Holder<Attribute>, AttributeModifier> modifiers, List<ItemStack> transformMaterials) {
		this.id = id;
		this.texture = texture;
		this.tailTexture = tailTexture;
		this.bodyLayer = bodyLayer;
		this.tailLayer = tailLayer;
		this.soulItem = soulItem;
		this.advancement = advancement;
		this.modifiers = modifiers == null ? Collections.emptyMap() : modifiers;
		this.transformMaterials = transformMaterials == null ? Collections.emptyList() : transformMaterials;
	}

	public String getId() {
		return id;
	}

	/** 开启该形态的第一人称手臂渲染 */
	protected void enableFirstPersonArm() {
		this.showFirstPersonArm = true;
	}

	public boolean showFirstPersonArm() {
		return showFirstPersonArm;
	}

	public ResourceLocation getTexture() {
		return texture;
	}

	/** 尾巴纹理，为 null 时使用身体纹理 */
	public ResourceLocation getTailTexture() {
		return tailTexture;
	}

	public ResourceLocation getBodyLayer() {
		return bodyLayer;
	}

	public ResourceLocation getTailLayer() {
		return tailLayer;
	}

	public ItemStack getSoulItem() {
		return soulItem == null ? ItemStack.EMPTY : soulItem.get();
	}

	public ResourceLocation getAdvancement() {
		return advancement;
	}

	public Map<Holder<Attribute>, AttributeModifier> getAttributeModifiers() {
		return modifiers;
	}

	public List<ItemStack> getTransformMaterials() {
		return transformMaterials;
	}

	public boolean isFeral() {
		return !HUMAN_ID.equals(id);
	}

	@OnlyIn(Dist.CLIENT)
	public net.minecraft.client.model.PlayerModel getBodyModel() {
		return bodyLayer == null ? null : FeralFormModels.getBodyModel(bodyLayer);
	}

	@OnlyIn(Dist.CLIENT)
	public net.minecraft.client.model.EntityModel<?> getTailModel() {
		return tailLayer == null ? null : FeralFormModels.getTailModel(tailLayer);
	}

	/** 尾巴待机动画（循环摆动），为 null 表示无尾巴动画 */
	@OnlyIn(Dist.CLIENT)
	public net.minecraft.client.animation.AnimationDefinition getTailIdleAnimation() {
		return null;
	}

	/** 尾巴行走/跑动动画，为 null 表示无尾巴动画 */
	@OnlyIn(Dist.CLIENT)
	public net.minecraft.client.animation.AnimationDefinition getTailWalkAnimation() {
		return null;
	}
}
