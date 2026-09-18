package net.mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.init.MurmolModItems;
import net.mcr.murmol.client.model.animations.furtalsAnimation;

import net.minecraft.client.animation.AnimationDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 乘黄形态。
 * 身体模型 model_c_hmodel，尾巴 model_c_htail，纹理 feralch.png。
 */
public class ChenHuangForm extends FeralForm {

	public static final String ID = "chen_huang";

	public ChenHuangForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/feralch.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "model_c_hmodel"),
				ResourceLocation.fromNamespaceAndPath("murmol", "model_c_htail"),
				() -> new ItemStack(MurmolModItems.CH_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "get_chenhuang"),
				Map.of(
						Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 4, AttributeModifier.Operation.ADD_VALUE),
						Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.5, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(Items.GOLDEN_APPLE),
							new ItemStack(MurmolModItems.MANGO.get()),
							new ItemStack(Items.SUGAR),
							new ItemStack(Items.HONEY_BOTTLE)));
		// 该形态显示第一人称手臂（配合全局配置 renderFirstPersonArm）
		enableFirstPersonArm();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public AnimationDefinition getTailIdleAnimation() {
		return furtalsAnimation.waving_tails;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public AnimationDefinition getTailWalkAnimation() {
		return furtalsAnimation.run;
	}
}
