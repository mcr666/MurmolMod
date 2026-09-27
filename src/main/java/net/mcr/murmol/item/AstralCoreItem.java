package net.mcr.murmol.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;

import net.mcr.murmol.entity.AstralCoreProjectileEntity;
import net.mcr.murmol.init.MurmolModEntities;

public class AstralCoreItem extends Item {
	public AstralCoreItem() {
		super(new Item.Properties().stacksTo(16));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		ItemStack stack = entity.getItemInHand(hand);
		if (!world.isClientSide()) {
			AstralCoreProjectileEntity projectile = new AstralCoreProjectileEntity(MurmolModEntities.ASTRAL_CORE_PROJECTILE.get(), entity, world);
			projectile.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
			world.addFreshEntity(projectile);
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 0.9F);
			if (!entity.getAbilities().instabuild)
				stack.shrink(1);
		}
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
	}
}
