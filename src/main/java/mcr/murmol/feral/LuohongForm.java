package mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import mcr.murmol.init.MurmolModItems;

/**
 * 春花狐龙形态。
 * 身体模型 spring_flower，尾巴 spring_flower_tail，纹理 spring_flower.png。
 */
public class LuohongForm extends FeralForm {

	public static final String ID = "spring_flower";

	public LuohongForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/spring_flower.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "spring_flower"),
				ResourceLocation.fromNamespaceAndPath("murmol", "spring_flower_tail"),
				() -> mcr.murmol.item.FormSoulItem.forForm("spring_flower"),
				ResourceLocation.fromNamespaceAndPath("murmol", "witnessof_petal_spring"),
				Map.of(
							Attributes.MAX_HEALTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), -4, AttributeModifier.Operation.ADD_VALUE),
							Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.025, AttributeModifier.Operation.ADD_VALUE),
							Attributes.ATTACK_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.3, AttributeModifier.Operation.ADD_VALUE),
							Attributes.JUMP_STRENGTH, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), 0.2, AttributeModifier.Operation.ADD_VALUE),
							Attributes.SCALE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("murmol", "tf"), -0.25, AttributeModifier.Operation.ADD_VALUE)),
				List.of(
							new ItemStack(MurmolModItems.PETAL.get()),
							new ItemStack(Items.SWEET_BERRIES),
							new ItemStack(Items.GLISTERING_MELON_SLICE),
							new ItemStack(Items.APPLE)));
		// 该形态不显示第一人称手臂
		enableFirstPersonArm();
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/spring_flower_anim.json"));
		// 春花狐龙食性：只吃素食，吃肉/其他按坏食物处理
		setDiet(FormDiet.HERBIVORE);
	}
}
