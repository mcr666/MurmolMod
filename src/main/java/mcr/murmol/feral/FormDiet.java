package mcr.murmol.feral;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.SugarCaneBlock;

/**
 * 形态食性（Diet）。
 * <p>
 * 参照 DragonSurvival 的食性系统实现：每个形态声明一种偏好食性，
 * 吃「不符合食性」的食物时，不直接禁止，而是按 {@link #badFood()} 的
 * 「坏食物」属性处理——营养极低 + 饥饿 + 中毒，即「能吃但不健康」。
 * 数据驱动的物品分类（肉 / 素）用物品标签定义：{@code murmol:diet/meat}、
 * {@code murmol:diet/vegetable}（见 data/murmol/tags/item/diet/）。
 */
public enum FormDiet {

	/** 杂食：无食性限制（默认） */
	OMNIVORE,
	/** 肉食：只吃肉 / 鱼 */
	CARNIVORE,
	/** 素食：只吃常规素食物（面包 / 蔬果等） */
	HERBIVORE,
	/** 植物方块食性：只吃植物类方块物品（苔藓 / 树叶 / 蘑菇等，苔叶兽特色） */
	PLANT;

	private static final TagKey<Item> MEAT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("murmol", "diet/meat"));
	private static final TagKey<Item> VEGETABLE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("murmol", "diet/vegetable"));

	/**
	 * 该食性是否允许吃给定（普通）食物。
	 * 植物方块物品不再注入食物属性，由 {@link mcr.murmol.mixin.ItemFoodPropertiesMixin}
	 * 在原版食物读取入口动态分发（仅苔叶兽可食），不经过本食性匹配。
	 */
	public static boolean matches(FormDiet diet, Player player, ItemStack stack) {
		return switch (diet) {
			case OMNIVORE -> true;
			case CARNIVORE -> stack.is(MEAT);
			case HERBIVORE -> stack.is(VEGETABLE);
			case PLANT -> stack.is(VEGETABLE); // 苔叶兽吃素；植物方块由 ItemFoodPropertiesMixin 动态分发
		};
	}

	/** 苔叶兽的植物方块食物属性：与原版胡萝卜相同（营养 3、饱和 0.6） */
	public static FoodProperties plantFood() {
		return new FoodProperties.Builder()
				.nutrition(3).saturationModifier(0.6F).build();
	}

	/** 是否为可食用的植物方块物品（苔叶兽的植物方块食性判定） */
	public static boolean isPlantFood(ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof BlockItem blockItem) || item instanceof ItemNameBlockItem)
			return false;
		return isPlantBlock(blockItem.getBlock());
	}

	/** 植物类方块判定：花草 / 树苗 / 蕨类等 BushBlock、藤蔓海带等 GrowingPlantBlock、树叶，
	 * 以及苔藓、孢子花、甘蔗、竹子、仙人掌、紫颂、巨型蘑菇、发光地衣、南瓜 / 西瓜、大垂滴叶 */
	public static boolean isPlantBlock(net.minecraft.world.level.block.Block block) {
		return block instanceof BushBlock
				|| block instanceof GrowingPlantBlock
				|| block instanceof LeavesBlock
				|| block instanceof MossBlock
				|| block == net.minecraft.world.level.block.Blocks.MOSS_CARPET
				|| block instanceof SporeBlossomBlock
				|| block instanceof SugarCaneBlock
				|| block instanceof net.minecraft.world.level.block.BambooStalkBlock
				|| block instanceof CactusBlock
				|| block instanceof ChorusFlowerBlock
				|| block instanceof ChorusPlantBlock
				|| block instanceof HugeMushroomBlock
				|| block instanceof GlowLichenBlock
				|| block instanceof PumpkinBlock
				|| block == net.minecraft.world.level.block.Blocks.MELON
				|| block instanceof BigDripleafBlock
				|| block instanceof BigDripleafStemBlock;
	}

	/** 不符合食性时的「坏食物」属性：营养 1 + 饥饿 + 中毒（参照 DragonSurvival getBadFoodProperties） */
	public static FoodProperties badFood() {
		return new FoodProperties.Builder()
				.nutrition(1)
				.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)
				.effect(() -> new MobEffectInstance(MobEffects.POISON, 600, 0), 0.5F)
				.build();
	}
}
