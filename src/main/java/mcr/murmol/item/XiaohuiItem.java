package mcr.murmol.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;

import mcr.murmol.entity.XiaoHuiProjectileEntity;

/**
 * 无名徽章：类原版雪球机制——右键立即投掷；
 * ItemStack#consume 在创造模式下自动不消耗，无需手写 instabuild 分支。
 */
public class XiaohuiItem extends Item {
	public XiaohuiItem() {
		super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			XiaoHuiProjectileEntity projectile = XiaoHuiProjectileEntity.shoot(world, player, world.getRandom());
			// 创造模式投出的徽章仅创造玩家可拾取（与原版箭一致）
			if (player.getAbilities().instabuild) {
				projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
			}
			itemstack.consume(1, player);
		}
		return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
	}
}
