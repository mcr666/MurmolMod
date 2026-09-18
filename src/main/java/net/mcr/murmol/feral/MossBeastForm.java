package net.mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;

import net.mcr.murmol.init.MurmolModItems;
import net.mcr.murmol.client.model.animations.leaftailAnimation;

import net.minecraft.client.animation.AnimationDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 苔叶兽形态。
 * 身体模型 modelleaf，尾巴 modelleaftail，身体纹理 furleaf.png，尾巴纹理 leaftail.png。
 */
public class MossBeastForm extends FeralForm {

	public static final String ID = "moss_beast";

	public MossBeastForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/furleaf.png"),
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/leaftail.png"),
				ResourceLocation.fromNamespaceAndPath("murmol", "modelleaf"),
				ResourceLocation.fromNamespaceAndPath("murmol", "modelleaftail"),
				() -> new ItemStack(MurmolModItems.MOSS_SOUL.get()),
				ResourceLocation.fromNamespaceAndPath("murmol", "stands_a_loquat_tree"),
				Map.of(
						Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 4, AttributeModifier.Operation.ADD_VALUE),
						Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.5, AttributeModifier.Operation.ADD_VALUE),
						Attributes.JUMP_STRENGTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.2, AttributeModifier.Operation.ADD_VALUE),
						Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.01, AttributeModifier.Operation.ADD_VALUE),
						Attributes.MINING_EFFICIENCY, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 1, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(MurmolModItems.TREE_HEART.get()),
							new ItemStack(Blocks.MOSS_BLOCK),
							new ItemStack(Blocks.VINE),
							new ItemStack(Blocks.SPORE_BLOSSOM)));
		// 该形态显示第一人称手臂（配合全局配置 renderFirstPersonArm）
		enableFirstPersonArm();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public AnimationDefinition getTailIdleAnimation() {
		return leaftailAnimation.pre_parallel0;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public AnimationDefinition getTailWalkAnimation() {
		return leaftailAnimation.run;
	}
}
