package mcr.murmol.feral;

import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import mcr.murmol.init.MurmolModItems;

/**
 * 狛犬形态。
 * 身体模型 komainu（尾巴骨骼链在模型内），纹理 komainu.png。
 * 动画与乘黄/春花狐龙一致（通用 feral 动画 + tail_idle/tail_walk 尾巴动画）。
 * 特性：静止 5 秒进入石像状态（石头纹理、潜行动画、抗性提升、远处怪物无法察觉）。
 */
public class KomainuForm extends FeralForm {

	public static final String ID = "komainu";

	public KomainuForm() {
		super(ID,
				ResourceLocation.fromNamespaceAndPath("murmol", "textures/entities/komainu.png"),
				null,
				ResourceLocation.fromNamespaceAndPath("murmol", "komainu"),
				null,
				() -> mcr.murmol.item.FormSoulItem.forForm("komainu"),
				ResourceLocation.fromNamespaceAndPath("murmol", "get_komainu"),
				Map.of(),
				List.of(
							new ItemStack(Items.STONE),
							new ItemStack(Items.CHERRY_SAPLING),
							new ItemStack(Items.BONE),
							new ItemStack(Items.DIAMOND)));
		// 该形态不显示第一人称手臂
		enableFirstPersonArm();
		// 形态专属动画文件（通用 idle/walk/sneak + tail_idle/tail_walk，初始内容与乘黄一致）
		setAnimationFile(ResourceLocation.fromNamespaceAndPath("murmol", "player_animations/komainu_anim.json"));
	}

	@Override
	public boolean hasStatueState() {
		return true;
	}
}
