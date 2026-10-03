package mcr.murmol.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;

import mcr.murmol.init.MurmolModItems;

/**
 * 幻星工具：由对应下界合金工具在锻造台加原版升级模板与幻星锭升级获得。
 * 挖掘速度略快于下界合金（10 vs 9），耐久为下界合金的 1.25 倍（2031 * 1.25 = 2539）。
 */
public class AstralToolItem {
	private static final int DURABILITY = 2539;
	private static final float SPEED = 10f;
	private static final float ATTACK_DAMAGE_BONUS = 4f;

	public static final Tier TOOL_TIER = new Tier() {
		@Override
		public int getUses() {
			return DURABILITY;
		}

		@Override
		public float getSpeed() {
			return SPEED;
		}

		@Override
		public float getAttackDamageBonus() {
			return ATTACK_DAMAGE_BONUS;
		}

		@Override
		public TagKey<Block> getIncorrectBlocksForDrops() {
			return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
		}

		@Override
		public int getEnchantmentValue() {
			return 15;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.of(new ItemStack(MurmolModItems.ASTRAL_INGOT.get()));
		}
	};

	public static class Pickaxe extends PickaxeItem {
		public Pickaxe() {
			super(TOOL_TIER, new Properties().attributes(DiggerItem.createAttributes(TOOL_TIER, 1f, -2.8f)));
		}
	}

	public static class Shovel extends ShovelItem {
		public Shovel() {
			super(TOOL_TIER, new Properties().attributes(DiggerItem.createAttributes(TOOL_TIER, 1.5f, -3f)));
		}
	}

	public static class Axe extends AxeItem {
		public Axe() {
			super(TOOL_TIER, new Properties().attributes(DiggerItem.createAttributes(TOOL_TIER, 5f, -3f)));
		}
	}
}
