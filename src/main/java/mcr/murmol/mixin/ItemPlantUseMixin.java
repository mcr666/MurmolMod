package mcr.murmol.mixin;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FormDiet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 苔叶兽食用植物方块物品的入口：拦截 {@code Item#use}。
 * <p>
 * 植物方块物品没有 FOOD 数据组件，原版 {@code use} 不会进入进食分支；且
 * {@code Item#getFoodProperties} 在 1.21.1 字节码中不存在，无法像旧版那样注入
 * 让其「看起来可食」。故在此直接接管：苔叶兽（PLANT）右键植物方块物品时立即进食
 * （应用 plantFood 属性 + 播放进食音效），实际数值在
 * {@link ItemFoodPropertiesMixin}（LivingEntity#eat）中统一处理。
 * 其余形态不拦截 → 物品按原版行为放置。
 */
@Mixin(Item.class)
public abstract class ItemPlantUseMixin {

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void murmol$plantBlockEat(Level level, Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		ItemStack stack = player.getItemInHand(hand);
		FeralForm form = FeralFormManager.getForm(player);
		if (!form.isFeral() || form.getDiet() != FormDiet.PLANT || !FormDiet.isPlantFood(stack)) {
			return;
		}

		if (!level.isClientSide && player.getFoodData().needsFood()) {
			// 实际进食（数值/消耗）仅在服务端执行，在 LivingEntity#eat 中按 plantFood 属性应用
			player.eat(level, stack);
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.5F + 0.5F * player.getRandom().nextFloat(),
				player.getRandom().nextFloat() * 0.1F + 0.9F);
		player.swing(hand, true);
		cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide()));
	}
}
