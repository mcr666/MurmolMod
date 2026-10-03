package mcr.murmol.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;

import mcr.murmol.entity.AstralCoreProjectileEntity;
import mcr.murmol.init.MurmolModEntities;

/**
 * 星幻奥秘：无耐久手持书，右键发射 astral_core 弹射物，3 秒冷却，不消耗自身。
 */
public class AstralCodexItem extends Item {
	public static final int COOLDOWN_TICKS = 60; // 3s

	public AstralCodexItem() {
		super(new Item.Properties().stacksTo(1).fireResistant());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		ItemStack stack = entity.getItemInHand(hand);
		if (!world.isClientSide()) {
			if (entity.getCooldowns().isOnCooldown(this))
				return InteractionResultHolder.fail(stack);
			AstralCoreProjectileEntity projectile = new AstralCoreProjectileEntity(MurmolModEntities.ASTRAL_CORE_PROJECTILE.get(), entity, world);
			projectile.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
			world.addFreshEntity(projectile);
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 0.9F);
			entity.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		}
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
	}
}
