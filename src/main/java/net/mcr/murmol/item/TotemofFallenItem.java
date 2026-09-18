package net.mcr.murmol.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.murmol.feral.FeralForms;

public class TotemofFallenItem extends Item {
	public TotemofFallenItem() {
		super(new Item.Properties().stacksTo(1).food((new FoodProperties.Builder()).nutrition(0).saturationModifier(0f).alwaysEdible().build()));
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		return 80;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		FeralFormManager.transformFromItem(world, x, y, z, entity, FeralForms.HUMAN, itemstack);
		return retval;
	}

	/** 右键：变形状态下取出当前形态对应的灵魂物品（人类形态无效） */
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			FeralForm form = FeralFormManager.getForm(player);
			if (!form.isFeral() || form.getSoulItem().isEmpty()) {
				player.displayClientMessage(Component.translatable("item.murmol.totemof_fallen.not_feral"), true);
				return InteractionResultHolder.fail(stack);
			}
			// 给予当前形态对应的灵魂物品 1 个
			ItemStack soul = form.getSoulItem().copy();
			if (!player.getInventory().add(soul))
				player.drop(soul, false);
			// 消耗 1 个不死图腾（创造模式不消耗）
			if (!player.getAbilities().instabuild)
				stack.shrink(1);
			world.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
			player.getCooldowns().addCooldown(this, 40);
		}
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
	}
}