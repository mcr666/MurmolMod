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

	/** 右键：先检查显形锁与解锁限制，通过后才进入蓄力 */
	@Override
	public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level world, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (FeralFormManager.checkSoulItemUse(world, player, stack, FeralForms.CHEN_HUANG) != null)
			return net.minecraft.world.InteractionResultHolder.fail(stack);
		player.startUsingItem(hand);
		return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
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