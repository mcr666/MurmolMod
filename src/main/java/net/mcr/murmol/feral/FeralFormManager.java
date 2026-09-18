package net.mcr.murmol.feral;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import net.mcr.murmol.init.MurmolModMobEffects;
import net.mcr.murmol.init.MurmolModItems;
import net.mcr.murmol.network.MurmolModVariables;

/**
 * 变形系统行为中心。集中处理：
 * - 形态读写（基于 PlayerVariables.feralFormId 附件）
 * - 属性修饰符应用/清除
 * - 完整变形流程（音效、成就、效果）
 * - 幻星秘典材料匹配
 * - 事件监听：玩家 tick（属性刷新）、装备变更（野性形态掉护甲）、伤害（人类杀手附魔）
 */
@EventBusSubscriber
public class FeralFormManager {

	private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("murmol", "tf");

	// ==================== 形态读写 ====================

	public static FeralForm getForm(Entity entity) {
		if (entity == null)
			return FeralForms.HUMAN;
		if (entity instanceof Player player && player.hasData(MurmolModVariables.PLAYER_VARIABLES)) {
			String id = player.getData(MurmolModVariables.PLAYER_VARIABLES).feralFormId;
			return FeralForms.byId(id);
		}
		return FeralForms.HUMAN;
	}

	public static void setForm(Entity entity, FeralForm form) {
		if (!(entity instanceof Player player))
			return;
		FeralForm oldForm = getForm(entity);
		player.getData(MurmolModVariables.PLAYER_VARIABLES).feralFormId = form.getId();
		player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
		clearModifiers(player);
		applyModifiers(player, form);
	}

	// ==================== 属性修饰符 ====================

	public static void applyModifiers(LivingEntity entity, FeralForm form) {
		for (var entry : form.getAttributeModifiers().entrySet()) {
			var attr = entity.getAttribute(entry.getKey());
			if (attr == null)
				continue;
			var modifier = entry.getValue();
			if (!attr.hasModifier(modifier.id())) {
				attr.addTransientModifier(modifier);
			}
		}
	}

	public static void clearModifiers(LivingEntity entity) {
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.LUCK);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY);
		removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
	}

	private static void removeModifier(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr) {
		var instance = entity.getAttribute(attr);
		if (instance != null) {
			instance.removeModifier(MODIFIER_ID);
		}
	}

	// ==================== 变形流程 ====================

	/**
	 * 物品触发的变形（如堕落者图腾、灵魂物品）。直接变形为指定形态。
	 */
	public static void transformFromItem(LevelAccessor world, double x, double y, double z, Entity entity, FeralForm form, ItemStack itemstack) {
		if (entity == null)
			return;
		playTransformSound(world, x, y, z);
		setForm(entity, form);
		awardAdvancement(entity, form);
		addTransformEffects(entity);
	}

	private static void playTransformSound(LevelAccessor world, double x, double y, double z) {
		if (world instanceof Level level) {
			var sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.elder_guardian.curse"));
			if (!level.isClientSide()) {
				level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 1, 1);
			} else {
				level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, 1, 1, false);
			}
		}
	}

	private static void awardAdvancement(Entity entity, FeralForm form) {
		if (!(entity instanceof ServerPlayer player) || form.getAdvancement() == null)
			return;
		AdvancementHolder adv = player.server.getAdvancements().get(form.getAdvancement());
		if (adv == null)
			return;
		AdvancementProgress ap = player.getAdvancements().getOrStartProgress(adv);
		if (!ap.isDone()) {
			for (String criteria : ap.getRemainingCriteria())
				player.getAdvancements().award(adv, criteria);
		}
	}

	private static void addTransformEffects(Entity entity) {
		if (entity instanceof LivingEntity le && !le.level().isClientSide()) {
			le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1));
			le.addEffect(new MobEffectInstance(MurmolModMobEffects.CREATIVE_SHOCK, 60, 1));
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
		}
	}

	// ==================== 材料匹配 ====================

	/**
	 * 无序材料匹配：传入 4 个槽位的物品列表（可含 EMPTY），按物品 id 计数后与配方材料计数比对
	 * （每种材料出现次数相同即可），返回匹配到的形态，未匹配返回 null。
	 * 人类形态（变回人类）：水桶 + 不死图腾 + 绿宝石 + 任意船 特判在前。
	 */
	public static FeralForm findFormByMaterials(java.util.List<ItemStack> slotItems) {
		if (matchesHumanRevert(slotItems))
			return FeralForms.HUMAN;
		for (FeralForm form : FeralForms.all()) {
			if (form == FeralForms.HUMAN)
				continue;
			if (matchesMaterials(slotItems, form.getTransformMaterials()))
				return form;
		}
		return null;
	}

	/** 统计非空槽位中每种物品的数量（每个 ItemStack 槽位计 1 次） */
	private static java.util.Map<net.minecraft.world.item.Item, Integer> countItems(java.util.List<ItemStack> slotItems) {
		java.util.Map<net.minecraft.world.item.Item, Integer> counts = new java.util.HashMap<>();
		for (ItemStack stack : slotItems) {
			if (!stack.isEmpty())
				counts.merge(stack.getItem(), 1, Integer::sum);
		}
		return counts;
	}

	/** 人类配方：水桶 + 不死图腾 + 绿宝石 + 任意船（boats 标签），无序且每种材料出现次数须完全一致 */
	private static boolean matchesHumanRevert(java.util.List<ItemStack> slotItems) {
		java.util.Map<net.minecraft.world.item.Item, Integer> counts = countItems(slotItems);
		int total = counts.values().stream().mapToInt(Integer::intValue).sum();
		if (total != 4)
			return false;
		if (counts.getOrDefault(Items.WATER_BUCKET, 0) != 1
				|| counts.getOrDefault(Items.TOTEM_OF_UNDYING, 0) != 1
				|| counts.getOrDefault(Items.EMERALD, 0) != 1)
			return false;
		// 剩余第 4 个物品须为任意船
		return slotItems.stream().anyMatch(s -> !s.isEmpty() && s.is(ItemTags.BOATS));
	}

	private static boolean matchesMaterials(java.util.List<ItemStack> slotItems, java.util.List<ItemStack> required) {
		if (required.isEmpty())
			return false;
		java.util.Map<net.minecraft.world.item.Item, Integer> need = new java.util.HashMap<>();
		for (ItemStack req : required)
			need.merge(req.getItem(), 1, Integer::sum);
		return countItems(slotItems).equals(need);
	}

	// ==================== 事件监听 ====================

	/**
	 * 形态碰撞箱扩展：宽度按 hitboxWidthBonus 向四周各扩展（高度不变）。
	 * 变形时 SCALE 属性变化会触发 refreshDimensions，从而走到这里；复原后不再扩展。
	 */
	@SubscribeEvent
	public static void onEntitySize(net.neoforged.neoforge.event.entity.EntityEvent.Size event) {
		if (!(event.getEntity() instanceof Player player))
			return;
		FeralForm form = getForm(player);
		if (!form.isFeral() || form.getHitboxWidthBonus() == 0)
			return;
		net.minecraft.world.entity.EntityDimensions size = event.getNewSize();
		event.setNewSize(net.minecraft.world.entity.EntityDimensions.scalable(
				size.width() + form.getHitboxWidthBonus() * 2, size.height()));
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		LivingEntity entity = event.getEntity();
		FeralForm form = getForm(entity);
		applyModifiers(entity, form);
	}

	@SubscribeEvent
	public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
		LivingEntity entity = event.getEntity();
		FeralForm form = getForm(entity);
		if (!form.isFeral())
			return;
		// 野性形态下，非白名单护甲会掉落
		TagKey<net.minecraft.world.item.Item> nodiaoluo = ItemTags.create(ResourceLocation.parse("mod:nodiaoluo"));
		LevelAccessor world = entity.level();
		double x = entity.getX(), y = entity.getY(), z = entity.getZ();
		dropArmorIfNeeded(entity, world, x, y, z, EquipmentSlot.CHEST, nodiaoluo, ItemTags.create(ResourceLocation.parse("minecraft:chest_armor")));
		dropArmorIfNeeded(entity, world, x, y, z, EquipmentSlot.LEGS, nodiaoluo, ItemTags.create(ResourceLocation.parse("minecraft:leg_armor")));
		dropArmorIfNeeded(entity, world, x, y, z, EquipmentSlot.FEET, nodiaoluo, ItemTags.create(ResourceLocation.parse("minecraft:foot_armor")));
	}

	private static void dropArmorIfNeeded(LivingEntity entity, LevelAccessor world, double x, double y, double z,
			EquipmentSlot slot, TagKey<net.minecraft.world.item.Item> whitelist, TagKey<net.minecraft.world.item.Item> armorTag) {
		ItemStack stack = entity.getItemBySlot(slot);
		if (stack.is(whitelist))
			return;
		if (!stack.is(armorTag))
			return;
		if (world instanceof ServerLevel level) {
			ItemEntity drop = new ItemEntity(level, x, y, z, stack.copy());
			drop.setPickUpDelay(40);
			drop.setUnlimitedLifetime();
			level.addFreshEntity(drop);
		}
		if (entity instanceof Player player) {
			int idx = switch (slot) {
				case FEET -> 0;
				case LEGS -> 1;
				case CHEST -> 2;
				case HEAD -> 3;
				default -> -1;
			};
			if (idx >= 0) {
				player.getInventory().armor.set(idx, new ItemStack(Blocks.AIR));
				player.getInventory().setChanged();
			}
		} else {
			entity.setItemSlot(slot, new ItemStack(Blocks.AIR));
		}
	}

	@SubscribeEvent
	public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
		LivingEntity target = event.getEntity();
		Entity source = event.getSource().getEntity();
		if (target == null || source == null)
			return;
		ItemStack weapon = source instanceof LivingEntity le ? le.getMainHandItem() : ItemStack.EMPTY;
		var enchantmentKey = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("murmol:human_killer"));
		int level = weapon.getEnchantmentLevel(target.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentKey));
		if (level <= 0)
			return;
		// 人类杀手附魔：对人型生物（玩家、僵尸、骷髅、村民、灾厄村民、猪灵等）造成额外伤害
		boolean isHumanoid = target instanceof net.minecraft.world.entity.player.Player
				|| target instanceof net.minecraft.world.entity.monster.Zombie
				|| target instanceof net.minecraft.world.entity.monster.AbstractSkeleton
				|| target instanceof net.minecraft.world.entity.npc.AbstractVillager
				|| target instanceof net.minecraft.world.entity.monster.AbstractIllager
				|| target instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin;
		if (isHumanoid) {
			target.hurt(target.damageSources().generic(), level * 2);
		}
	}
}
