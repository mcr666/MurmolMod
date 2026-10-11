package mcr.murmol.item;

import java.util.EnumMap;
import java.util.List;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.Util;

import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

/**
 * 水泡头盔：戴在头上时眼睛被判定为浸在水中（见 EntityWaterBubbleMixin）。
 * 无护甲值、无盔甲渲染层（透明水泡，不显示额外模型）。
 */
@EventBusSubscriber
public class WaterBubbleHelmetItem extends ArmorItem {

	public static Holder<ArmorMaterial> ARMOR_MATERIAL = null;

	@SubscribeEvent
	public static void registerArmorMaterial(RegisterEvent event) {
		event.register(Registries.ARMOR_MATERIAL, registerHelper -> {
			ArmorMaterial material = new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class), map -> map.put(ArmorItem.Type.HELMET, 0)),
					0,
					DeferredHolder.create(Registries.SOUND_EVENT, ResourceLocation.parse("item.armor.equip_turtle")),
					() -> Ingredient.of(new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET)),
					List.of(),
					0.0F, 0.0F);
			registerHelper.register(ResourceLocation.parse("murmol:water_bubble"), material);
			ARMOR_MATERIAL = BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(material);
		});
	}

	public WaterBubbleHelmetItem() {
		super(ARMOR_MATERIAL, Type.HELMET,
				new Item.Properties().stacksTo(1).durability(Type.HELMET.getDurability(15)));
	}

	/**
	 * NeoForge 用 LivingBreatheEvent 取代了原版扣氧链路：钩子按眼睛位置的真实 FluidType 判定，
	 * 陆地上为 EMPTY -> 默认 canBreathe=true。标签版 isEyeInFluid 欺骗只让事件每 tick 触发，
	 * 这里在"真实眼流体为空气 + 头戴水泡头盔"时把判定改为不能呼吸，氧气按原版速率消耗，
	 * 耗尽后由钩子自身发 LivingDrownEvent 并造成溺水伤害。
	 * 真在水中时钩子已按原版规则处理，不介入。
	 */
	@SubscribeEvent
	public static void onLivingBreathe(LivingBreatheEvent event) {
		LivingEntity entity = event.getEntity();
		if (!(entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof WaterBubbleHelmetItem))
			return;
		if (!entity.getEyeInFluidType().isAir())
			return;
		// 保留与真水一致的豁免：创造/旁观无敌、水下呼吸效果、天生水下呼吸生物
		if (entity instanceof Player player && player.getAbilities().invulnerable)
			return;
		if (MobEffectUtil.hasWaterBreathing(entity))
			return;
		if (entity.canBreatheUnderwater())
			return;
		event.setCanBreathe(false);
	}
}
