package mcr.murmol.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FeralForms;

public class ChSoulItem extends Item {
	public ChSoulItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(0).saturationModifier(0f).alwaysEdible().build()));
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		return 80;
	}

	/** 使用动画：拉弓姿势 */
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		// 显形药效期间禁止变形
		if (mcr.murmol.potion.RevealManager.isLocked(entity)) {
			if (entity instanceof net.minecraft.world.entity.player.Player p)
				p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.murmol.reveal.blocked"), true);
			return itemstack;
		}
		// 形态被两仪禁用时：失败提示（在 transformFromItem 中），不消耗物品
		if (!FeralFormManager.transformFromItem(world, entity.getX(), entity.getY(), entity.getZ(), entity, FeralForms.CHEN_HUANG, itemstack))
			return itemstack;
		return super.finishUsingItem(itemstack, world, entity);
	}
}