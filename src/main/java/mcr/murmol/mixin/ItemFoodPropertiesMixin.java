package mcr.murmol.mixin;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FormDiet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 形态食性注入（进食时点）。
 * <p>
 * 1.21.1 中 {@code Item#getFoodProperties} 已被数据组件取代（Item / ItemStack 字节码
 * 均无该方法），且 NeoForge {@code IItemExtension} 为接口 default 方法，接口内
 * @Inject 在正式环境不传回调（cir 为 null）。故改拦截进食入口
 * {@code LivingEntity#eat(Level, ItemStack)}（确定存在于字节码的类方法）：
 * <ul>
 *   <li>普通食物：符合形态食性（或人类/杂食）→ 保留原属性原版进食；
 *       不符合 → 改按「坏食物」（营养 1 + 饥饿 + 中毒）进食，可吃但不健康。</li>
 *   <li>植物方块物品：仅苔叶兽（PLANT）能走到这里（能否开吃由 ItemPlantUseMixin
 *       在 Item#use 控制），按 plantFood（胡萝卜同款属性）进食。</li>
 * </ul>
 * 注意：女仆等非玩家生物 entity 非 Player，完全走原版逻辑。
 */
@Mixin(LivingEntity.class)
public abstract class ItemFoodPropertiesMixin {

	@Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
			at = @At("HEAD"), cancellable = true)
	private void murmol$applyFormDiet(Level level, ItemStack food, CallbackInfoReturnable<ItemStack> cir) {
		if (!((Object) this instanceof Player player)) {
			return;
		}

		FeralForm form = FeralFormManager.getForm(player);
		// 人类 / 杂食或未变形 → 原版逻辑
		if (!form.isFeral() || form.getDiet() == FormDiet.OMNIVORE) {
			return;
		}

		// 植物方块物品：仅苔叶兽可食（PLANT），按胡萝卜属性进食
		if (FormDiet.isPlantFood(food)) {
			cir.setReturnValue(((LivingEntity) (Object) this).eat(level, food, FormDiet.plantFood()));
			return;
		}

		// 普通食物：不符合食性 → 按「坏食物」属性进食
		FoodProperties original = food.getFoodProperties(player);
		if (original != null && !FormDiet.matches(form.getDiet(), player, food)) {
			cir.setReturnValue(((LivingEntity) (Object) this).eat(level, food, FormDiet.badFood()));
		}
	}
}
