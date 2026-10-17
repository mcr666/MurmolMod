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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
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
 * 鱼泡（原名水泡头盔）：戴在头上时眼睛被判定为浸在水中（见 EntityWaterBubbleMixin）。
 * 无护甲值、无盔甲渲染层（透明水泡，不显示额外模型）。
 * 耐久上限 1000，新获得时初始受损 900（即初始耐久 100/1000）。
 * 修复：耐久不满的鱼泡在工作台或铁砧上用水瓶（potion 水）合成，可 +100 耐久（仅耐久不满时可修）。
 */
@EventBusSubscriber
public class WaterBubbleHelmetItem extends ArmorItem {

	/** 每次修复恢复的耐久 */
	public static final int REPAIR_AMOUNT = 100;
	/** 耐久上限 */
	public static final int MAX_DURABILITY = 1000;
	/** 初始受损值（新物品初始耐久 = MAX_DURABILITY - INITIAL_DAMAGE = 100） */
	public static final int INITIAL_DAMAGE = 900;

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
			registerHelper.register(ResourceLocation.parse("murmol:fish_bubble"), material);
			ARMOR_MATERIAL = BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(material);
		});
	}

	public WaterBubbleHelmetItem() {
		super(ARMOR_MATERIAL, Type.HELMET,
				new Item.Properties().stacksTo(1).durability(MAX_DURABILITY));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
		tooltipComponents.add(Component.translatable("item.murmol.fish_bubble.desc"));
	}

	/**
	 * 让所有新生成的鱼泡（/give、创造、指令、掉落等）初始就受损 900 点，即初始耐久 100/1000。
	 * 仅当堆栈尚未设置 DAMAGE 组件时生效，因此复制/已持久化/已修复的堆栈保持其自身耐久。
	 */
	@Override
	public void verifyComponentsAfterLoad(ItemStack stack) {
		if (!stack.has(net.minecraft.core.component.DataComponents.DAMAGE)) {
			stack.setDamageValue(INITIAL_DAMAGE);
		}
	}

	/**
	 * 铁砧修复：左槽为耐久不满的鱼泡 + 右槽为水瓶（potion）时，恢复 REPAIR_AMOUNT 点耐久并消耗一个水瓶。
	 */
	@SubscribeEvent
	public static void onAnvilUpdate(net.neoforged.neoforge.event.AnvilUpdateEvent event) {
		ItemStack left = event.getLeft();
		ItemStack right = event.getRight();
		if (!(left.getItem() instanceof WaterBubbleHelmetItem))
			return;
		// 水瓶：与合成配方一致，以 minecraft:potion 表示水
		if (!right.is(net.minecraft.world.item.Items.POTION))
			return;
		if (left.getDamageValue() <= 0)
			return; // 耐久已满，不可修复
		ItemStack out = left.copy();
		out.setDamageValue(Math.max(0, out.getDamageValue() - REPAIR_AMOUNT));
		event.setOutput(out);
		event.setCost(1);
		event.setMaterialCost(1);
		event.setCanceled(true);
	}

	/**
	 * NeoForge 用 LivingBreatheEvent 取代了原版扣氧链路：钩子按眼睛位置的真实 FluidType 判定，
	 * 陆地上为 EMPTY -> 默认 canBreathe=true。标签版 isEyeInFluid 欺骗只让事件每 tick 触发，
	 * 这里在"真实眼流体为空气 + 头戴鱼泡"时把判定改为不能呼吸，氧气按原版速率消耗，
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
