package mcr.murmol.feral;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;

import mcr.murmol.init.MurmolModParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import mcr.murmol.init.MurmolModMobEffects;
import mcr.murmol.init.MurmolModItems;
import mcr.murmol.network.MurmolModVariables;

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
	private static final ResourceLocation CAVE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("murmol", "tf_cave");
	private static final ResourceLocation SURFACE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("murmol", "tf_surface");
	private static final ResourceLocation DAY_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("murmol", "tf_day");
	private static final ResourceLocation NIGHT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("murmol", "tf_night");
	private static final ResourceLocation WENYAO_WATER_ID = ResourceLocation.fromNamespaceAndPath("murmol", "tf_wenyao_water");
	private static final ResourceLocation WENYAO_LAND_ID = ResourceLocation.fromNamespaceAndPath("murmol", "tf_wenyao_land");

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

	// ==================== 塑魂之约（形态绑定） ====================

	/** 该实体是否已缔结塑魂之约（绑定到某形态） */
	public static boolean isSoulbound(Entity entity) {
		return entity instanceof Player player
				&& player.hasData(MurmolModVariables.PLAYER_VARIABLES)
				&& !player.getData(MurmolModVariables.PLAYER_VARIABLES).soulboundFormId.isEmpty();
	}

	/** 该实体绑定的形态（未绑定时返回 null） */
	public static FeralForm getBoundForm(Entity entity) {
		if (!(entity instanceof Player player) || !player.hasData(MurmolModVariables.PLAYER_VARIABLES))
			return null;
		String id = player.getData(MurmolModVariables.PLAYER_VARIABLES).soulboundFormId;
		return id.isEmpty() ? null : FeralForms.byId(id);
	}

	/** 服务端：把玩家绑定到当前形态（须为野性形态、且未绑定）。 */
	public static boolean bindSoul(ServerPlayer player, FeralForm form) {
		if (form == null || !form.isFeral() || isSoulbound(player))
			return false;
		player.getData(MurmolModVariables.PLAYER_VARIABLES).soulboundFormId = form.getId();
		player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
		// 绑定即锁定当前形态（防止绑定瞬间正处于被显形临时替换等异常态）
		setForm(player, form);
		return true;
	}

	/** 服务端：解除玩家的塑魂之约绑定。 */
	public static void unbindSoul(ServerPlayer player) {
		if (!player.hasData(MurmolModVariables.PLAYER_VARIABLES))
			return;
		player.getData(MurmolModVariables.PLAYER_VARIABLES).soulboundFormId = "";
		player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
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
			// 一并清除环境条件修饰符（如苔叶兽洞穴/地面、月蛾昼夜）
			instance.removeModifier(MODIFIER_ID);
			instance.removeModifier(CAVE_MODIFIER_ID);
			instance.removeModifier(SURFACE_MODIFIER_ID);
			instance.removeModifier(DAY_MODIFIER_ID);
			instance.removeModifier(NIGHT_MODIFIER_ID);
			instance.removeModifier(WENYAO_WATER_ID);
			instance.removeModifier(WENYAO_LAND_ID);
		}
	}

	// ==================== 变形流程 ====================

	/**
	 * 物品触发的变形（如照妖镜、灵魂物品）。直接变形为指定形态。
	 * 目标形态与当前形态相同则 no-op（防 NeoOrigins 起源联动循环触发）。
	 *
	 * @return 是否成功变形（false = 形态被两仪禁用，已提示失败；调用方不应消耗物品）
	 */
	public static boolean transformFromItem(LevelAccessor world, double x, double y, double z, Entity entity, FeralForm form, ItemStack itemstack) {
		return transformFromItem(world, x, y, z, entity, form, itemstack, false);
	}

	/**
	 * 物品触发的变形（如照妖镜、灵魂物品）。直接变形为指定形态。
	 * 目标形态与当前形态相同则 no-op（防 NeoOrigins 起源联动循环触发）。
	 *
	 * @param ignoreToggle true = 忽略两仪禁用（用于 /murmol 指令等管理操作）
	 * @return 是否成功变形（false = 形态被两仪禁用，已提示失败；调用方不应消耗物品）
	 */
	public static boolean transformFromItem(LevelAccessor world, double x, double y, double z, Entity entity, FeralForm form, ItemStack itemstack, boolean ignoreToggle) {
		if (entity == null)
			return false;
		if (getForm(entity) == form)
			return true;
		// 塑魂之约：已绑定形态的玩家无法以非作弊手段变形为其他形态或人类（/murmol 等管理指令 ignoreToggle 不受限）
		if (!ignoreToggle && isSoulbound(entity)) {
			FeralForm bound = getBoundForm(entity);
			if (bound != null && bound != form) {
				if (entity instanceof Player player && !world.isClientSide())
					player.displayClientMessage(Component.translatable("message.murmol.soulbind.blocked", bound.getDisplayName()), true);
				return false;
			}
		}
		// 两仪开关：禁用的形态无法变形，给予失败提示，调用方不消耗物品（指令可忽略）
		if (!ignoreToggle && !FeralFormToggles.isEnabled(form, world.isClientSide())) {
			if (entity instanceof Player player && !world.isClientSide())
				player.displayClientMessage(Component.translatable("message.murmol.two_principles.disabled",
						form.getDisplayName()), true);
			return false;
		}
		// 解锁限制：游戏规则 murmolRequireFormUnlocked 开启且形态未解锁（进度 murmol:unlock/<id> 未完成）→ 拦截；
		// 管理操作 ignoreToggle 不受限
		if (!ignoreToggle && entity instanceof ServerPlayer player && isUnlockLocked(player, form)) {
			player.displayClientMessage(Component.translatable("message.murmol.transform.locked"), true);
			return false;
		}
		playTransformSound(world, x, y, z);
		setForm(entity, form);
		awardAdvancement(entity, form);
		addTransformEffects(entity);
		spawnTransformRings(entity);
		// NeoOrigins 联动：变形后同步起源（软依赖，未安装时静默跳过）
		if (entity instanceof ServerPlayer player)
			mcr.murmol.compat.NeoOriginsCompat.syncOrigin(player, form);
		return true;
	}

	// ==================== 解锁限制 ====================

	/**
	 * 服务端判定：解锁检查规则开启且该形态存在解锁进度且未完成（如人类无进度则不限制）。
	 */
	private static boolean isUnlockLocked(ServerPlayer player, FeralForm form) {
		if (form == FeralForms.HUMAN
				|| !player.serverLevel().getGameRules().getBoolean(mcr.murmol.init.MurmolModGameRules.REQUIRE_FORM_UNLOCKED))
			return false;
		AdvancementHolder unlock = player.server.getAdvancements().get(
				ResourceLocation.fromNamespaceAndPath(mcr.murmol.MurmolMod.MODID, "unlock/" + form.getId()));
		return unlock != null && !player.getAdvancements().getOrStartProgress(unlock).isDone();
	}

	/**
	 * 灵魂物品使用入口的统一拦截（两侧行为一致）：显形药效期间，或解锁检查开启且形态未解锁时不可使用。
	 * 返回 fail 结果表示拦截（已发送提示），null = 放行。
	 */
	public static InteractionResultHolder<ItemStack> checkSoulItemUse(Level world, Player player, ItemStack stack, FeralForm form) {
		// 塑魂之约：已绑定形态时，绑定形态以外的形态灵魂物品不可用
		if (isSoulbound(player)) {
			FeralForm bound = getBoundForm(player);
			if (bound != null && bound != form) {
				if (!world.isClientSide())
					player.displayClientMessage(Component.translatable("message.murmol.soulbind.blocked", bound.getDisplayName()), true);
				return InteractionResultHolder.fail(stack);
			}
		}
		// 显形药效期间禁止变形（药水效果两侧可见）
		if (mcr.murmol.potion.RevealManager.isLocked(player)) {
			if (!world.isClientSide())
				player.displayClientMessage(Component.translatable("message.murmol.reveal.blocked"), true);
			return InteractionResultHolder.fail(stack);
		}
		// 解锁检查开启且形态未解锁：服务端以进度为准，客户端以同步的已解锁集合为准
		boolean locked = player instanceof ServerPlayer serverPlayer ? isUnlockLocked(serverPlayer, form)
				: !player.getData(MurmolModVariables.PLAYER_VARIABLES).unlockedForms.contains(form.getId());
		if (locked) {
			if (!world.isClientSide())
				player.displayClientMessage(Component.translatable("message.murmol.transform.locked"), true);
			return InteractionResultHolder.fail(stack);
		}
		return null;
	}

	/**
	 * 服务端：按当前解锁进度与游戏规则刷新玩家的已解锁形态集合，并标记同步（客户端拦截判定依赖）。
	 * 解锁规则关闭时写入全部形态 id。
	 */
	public static void syncUnlockState(ServerPlayer player) {
		var data = player.getData(MurmolModVariables.PLAYER_VARIABLES);
		data.unlockedForms.clear();
		boolean ruleOn = player.serverLevel().getGameRules()
				.getBoolean(mcr.murmol.init.MurmolModGameRules.REQUIRE_FORM_UNLOCKED);
		for (FeralForm form : FeralForms.all()) {
			if (form == FeralForms.HUMAN)
				continue;
			if (!ruleOn) {
				data.unlockedForms.add(form.getId());
				continue;
			}
			AdvancementHolder unlock = player.server.getAdvancements().get(
					ResourceLocation.fromNamespaceAndPath(mcr.murmol.MurmolMod.MODID, "unlock/" + form.getId()));
			if (unlock == null || player.getAdvancements().getOrStartProgress(unlock).isDone())
				data.unlockedForms.add(form.getId());
		}
		data.markSyncDirty();
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
			le.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 1));
			le.addEffect(new MobEffectInstance(MurmolModMobEffects.CREATIVE_SHOCK, 60, 1));
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
		}
	}

	/**
	 * 变形瞬间在原地生成两圈半径为 1 的 astral_burst 粒子环：
	 * 一圈在脚部高度、一圈在胸口高度（各 24 颗）。
	 */
	private static void spawnTransformRings(Entity entity) {
		if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel server))
			return;
		double x = entity.getX(), y = entity.getY(), z = entity.getZ();
		double[] ringHeights = { 0.1D, 1.2D };
		int particles = 24;
		for (double h : ringHeights) {
			for (int i = 0; i < particles; i++) {
				double angle = Math.PI * 2 * i / particles;
				server.sendParticles(MurmolModParticleTypes.ASTRAL_BURST.get(),
						x + Math.cos(angle), y + h, z + Math.sin(angle),
						0, 0.0D, 0.0D, 0.0D, 1.0D);
			}
		}
	}

	// ==================== 材料匹配 ====================

	/**
	 * 「死亡后保留形态」规则关闭时的死亡掉落：从当前形态配方的固定材料中，
	 * 随机选取 3/4（向下取整，至少 1 个）个**不重复**的材料类型，在死亡处各掉落 1 个。
	 * 只统计 getTransformMaterials() 里的具体物品（标签类祭品如"任意鱼"不参与，无法确定掉落实体）。
	 */
	public static void dropPartialTransformMaterials(Entity deadEntity) {
		if (deadEntity == null || !(deadEntity.level() instanceof ServerLevel serverLevel))
			return;
		FeralForm form = getForm(deadEntity);
		if (form == null || !form.isFeral())
			return;
		java.util.LinkedHashSet<net.minecraft.world.item.Item> pool = new java.util.LinkedHashSet<>();
		for (ItemStack stack : form.getTransformMaterials())
			if (stack != null && !stack.isEmpty())
				pool.add(stack.getItem());
		if (pool.isEmpty())
			return;
		int total = pool.size();
		int dropCount = Math.min(total, Math.max(1, (int) Math.floor(total * 3.0 / 4.0)));
		java.util.List<net.minecraft.world.item.Item> shuffled = new java.util.ArrayList<>(pool);
		net.minecraft.util.RandomSource rng = serverLevel.getRandom();
		for (int i = shuffled.size() - 1; i > 0; i--) // Fisher-Yates：RandomSource 不兼容 Collections.shuffle
			java.util.Collections.swap(shuffled, i, rng.nextInt(i + 1));
		double x = deadEntity.getX(), y = deadEntity.getY(), z = deadEntity.getZ();
		for (int i = 0; i < dropCount; i++) {
			net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
					serverLevel, x, y + 0.5D, z, new ItemStack(shuffled.get(i)));
			drop.setDeltaMovement((serverLevel.getRandom().nextDouble() - 0.5) * 0.2, 0.2D,
					(serverLevel.getRandom().nextDouble() - 0.5) * 0.2);
			serverLevel.addFreshEntity(drop);
		}
	}

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
			if (matchesMaterials(slotItems, form))
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

	/** 形态祭品匹配：固定材料按物品计数比对，另有 getTagMaterial() 标签材料（如文鳐的任意鱼）匹配任意一个属于该标签的槽位 */
	private static boolean matchesMaterials(java.util.List<ItemStack> slotItems, FeralForm form) {
		java.util.List<ItemStack> required = form.getTransformMaterials();
		if (required.isEmpty())
			return false;
		java.util.Map<net.minecraft.world.item.Item, Integer> counts = countItems(slotItems);
		int total = counts.values().stream().mapToInt(Integer::intValue).sum();
		TagKey<net.minecraft.world.item.Item> tag = form.getTagMaterial();
		if (total != required.size() + (tag == null ? 0 : 1))
			return false;
		if (tag != null) {
			// 从计数中移除任意一个属于标签的物品（如任意鱼）
			net.minecraft.world.item.Item tagged = counts.keySet().stream()
					.filter(item -> item.builtInRegistryHolder().is(tag))
					.findFirst().orElse(null);
			if (tagged == null)
				return false;
			counts.compute(tagged, (item, n) -> n == 1 ? null : n - 1);
		}
		java.util.Map<net.minecraft.world.item.Item, Integer> need = new java.util.HashMap<>();
		for (ItemStack req : required)
			need.merge(req.getItem(), 1, Integer::sum);
		return counts.equals(need);
	}

	// ==================== 事件监听 ====================

	@SubscribeEvent
	public static void onCanPlayerSleep(net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent event) {
		// 月蛾昼夜均可睡觉：仅清除原版"只能在夜里睡觉"的判定，保留其他阻止原因（怪物等）
		if (getForm(event.getEntity()) == FeralForms.SILKMOTH
				&& event.getVanillaProblem() == Player.BedSleepingProblem.NOT_POSSIBLE_NOW)
			event.setProblem(null);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		LivingEntity entity = event.getEntity();
		FeralForm form = getForm(entity);
		applyModifiers(entity, form);
		updateMossBeastEnvironmentModifiers(entity, form);
		updateSilkmothEnvironmentModifiers(entity, form);
		updateKomainuStatueState(entity, form);
		updateWenyaoEnvironmentModifiers(entity, form);
		updateFerociousFearAura(entity, form);
		updateLuohongNatureAura(entity, form);
		updateChenHuangRiderBuffs(entity, form);
		// 悬停飞行模式免疫摔落伤害（每 tick 清零累计摔落距离）；
		// 鞘翅规则模式等同真鞘翅：滑翔中原版免伤，未展开滑翔的坠落保留原版风险
		if (form.isFeral() && form.canHoverFlight() && !useElytraFlight(entity) && entity.fallDistance > 0) {
			entity.fallDistance = 0;
		}
		// 飞行机制游戏规则无变更事件：服务端每秒轮询，变化时向全体玩家同步
		if (!entity.level().isClientSide && entity.tickCount % 20 == 0) {
			boolean now = entity.level().getGameRules().getBoolean(mcr.murmol.init.MurmolModGameRules.FERAL_ELYTRA_FLIGHT);
			if (now != elytraRuleSynced) {
				elytraRuleSynced = now;
				mcr.murmol.network.FeralFlightPayloads.syncToAll(entity.level().getServer());
			}
		}
		// 可飞行形态（canHoverFlight）每 10s 消耗半格饥饿值（服务端，仅玩家）
		if (!entity.level().isClientSide && form.isFeral() && form.canHoverFlight() && entity.tickCount % 200 == 0
				&& entity instanceof Player player) {
			player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 1));
		}
	}

	/** 最近一次已向客户端广播的飞行规则值（初始默认 false，玩家登录时另有单玩家同步） */
	private static boolean elytraRuleSynced = false;

	/**
	 * 该实体当前是否走"虚拟鞘翅"飞行模式：形态本身具备飞行能力（canHoverFlight），
	 * 且游戏规则 murmolFeralElytraFlight 开启。
	 * 客户端读服务端下发的规则缓存（GameRules 运行时变更不会自动同步）。
	 */
	public static boolean useElytraFlight(net.minecraft.world.entity.LivingEntity entity) {
		FeralForm form = getForm(entity);
		if (!form.isFeral() || !form.canHoverFlight())
			return false;
		if (entity.level().isClientSide)
			return mcr.murmol.network.FeralFlightPayloads.CLIENT_ELYTRA;
		return entity.level().getGameRules().getBoolean(mcr.murmol.init.MurmolModGameRules.FERAL_ELYTRA_FLIGHT);
	}

	// ==================== 怪物躲避变形玩家 ====================

	/** 规避参数与原版 Creeper 躲猫一致（1.21.1 Creeper.registerGoals：距离 6、走速 1.0、逃速 1.2） */
	private static final float FLEE_DISTANCE = 6.0F;
	private static final double FLEE_WALK_SPEED = 1.0D;
	private static final double FLEE_SPRINT_SPEED = 1.2D;
	/** 苦力怕规避优先级：与原版躲猫 goal 同为 3；addGoal 累加不覆盖原 goal，同一时刻 MOVE 由先启动者持有（与原版躲猫/躲豹猫两个 goal 的共存方式相同） */
	private static final int CREEPER_FLEE_GOAL_PRIORITY = 3;
	/** 亡灵规避优先级：须低于僵尸/骷髅等 2 号攻击 goal 才能抢占运行中的攻击移动（GoalSelector 只允许优先级数值更小的 goal 抢占），否则存在攻击目标时永远无法逃离 */
	private static final int UNDEAD_FLEE_GOAL_PRIORITY = 1;

	/**
	 * 实体加入世界时挂载"躲避变形玩家"AI（AI 仅服务端运行）：
	 * - 苦力怕：躲避已变形玩家（文鳐、月蛾除外），参数与原版躲猫相同；
	 * - 亡灵敌对生物（Monster 且属于 minecraft:undead 实体标签）：躲避狰形态玩家，或处于石像状态的狛犬形态玩家。
	 *
	 * 不会重复挂载：区块重载/跨维度/重登等"重新加入"场景中实体均为反序列化或新构建的全新实例，
	 * goalSelector 已由 registerGoals 重建清空，故每次事件挂载的都是新实例的一次性初始化。
	 */
	@SubscribeEvent
	public static void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide())
			return;
		if (event.getEntity() instanceof Creeper creeper) {
			creeper.goalSelector.addGoal(CREEPER_FLEE_GOAL_PRIORITY, new AvoidEntityGoal<>(creeper, Player.class,
					FLEE_DISTANCE, FLEE_WALK_SPEED, FLEE_SPRINT_SPEED, FeralFormManager::scaresCreeper));
		}
		if (event.getEntity() instanceof Monster monster && monster.getType().is(EntityTypeTags.UNDEAD)) {
			monster.goalSelector.addGoal(UNDEAD_FLEE_GOAL_PRIORITY, new AvoidEntityGoal<>(monster, Player.class,
					FLEE_DISTANCE, FLEE_WALK_SPEED, FLEE_SPRINT_SPEED, FeralFormManager::scaresUndead));
		}
	}

	/** 该玩家是否会吓到苦力怕：非创造/旁观（同原版躲猫过滤），且已变形、非文鳐、非月蛾 */
	private static boolean scaresCreeper(LivingEntity entity) {
		if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
			return false;
		FeralForm form = getForm(entity);
		return form.isFeral() && form != FeralForms.WENYAO && form != FeralForms.SILKMOTH;
	}

	/** 该玩家是否会吓跑亡灵：非创造/旁观，且为狰形态，或石像状态下的狛犬形态（isInStatue 内部已校验形态具备石像状态） */
	private static boolean scaresUndead(LivingEntity entity) {
		if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
			return false;
		return getForm(entity) == FeralForms.FEROCIOUS || isInStatue(entity);
	}

	/**
	 * 苔叶兽环境加成：在洞穴中（所处位置天空光为 0）获得移速 +0.01、跳跃力 +0.2、挖掘效率 +1；
	 * 在地面上则改为移速 -0.05。其余形态无环境修饰符。
	 */

	// ==================== 狛犬石像状态 ====================

	/** 石像触发所需静止时长（tick） */
	private static final int STATUE_IDLE_TICKS = 100;
	/** 判定为“移动”的位移阈值（格，平方距离） */
	private static final double STATUE_MOVE_THRESHOLD_SQR = 1.0E-4D;

	/** 服务端记录的狛犬静止状态：上帧位置 + 已静止 tick 数 */
	private static final java.util.WeakHashMap<Player, StatueTrack> STATUE_TRACKS = new java.util.WeakHashMap<>();

	private static final class StatueTrack {
		double lastX, lastY, lastZ;
		boolean initialized;
		int idleTicks;
	}

	/** 该实体（狛犬形态）当前是否处于石像状态 */
	public static boolean isInStatue(LivingEntity entity) {
		if (!(entity instanceof Player player) || !getForm(player).hasStatueState())
			return false;
		StatueTrack track = STATUE_TRACKS.get(player);
		return track != null && track.idleTicks >= STATUE_IDLE_TICKS;
	}

	/** 该玩家是否处于"磐座石像"模式（右键磐座激活的石像，区别于自然静止触发的普通石像）。
	 * 通过已同步的玩家变量 statueBanza 读取，双端一致。 */
	public static boolean isInBanzaStatue(LivingEntity entity) {
		return isInStatue(entity) && entity instanceof Player player
				&& player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza;
	}

	/**
	 * 磐座右键激活：狛犬站在磐座上右键可立即进入石像状态。
	 * @return 是否成功触发
	 */
	public static boolean tryActivateStatue(Player player) {
		if (!getForm(player).hasStatueState())
			return false;
		StatueTrack track = STATUE_TRACKS.computeIfAbsent(player, p -> new StatueTrack());
		track.initialized = true;
		track.idleTicks = STATUE_IDLE_TICKS;
		// 磐座石像模式：苔石贴图、物理定身、缓慢回血、仅 shift 解除（同步变量，双端可见）
		player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza = true;
		player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
		// 同步记录当前位置，否则下一 tick 判定为"已移动"导致石像状态立即被清除
		track.lastX = player.getX();
		track.lastY = player.getY();
		track.lastZ = player.getZ();
		// 磐座路径的进入粒子：tick 循环里状态无跳变，必须在此显式触发
		spawnStatueParticles(player);
		if (!player.level().isClientSide())
			playStatueSound(player);
		return true;
	}

	/**
	 * 狛犬石像状态：静止 5 秒进入——抗性提升 I、2 格以外怪物无法将其选为目标；
	 * 移动即退出。进出瞬间在身体周围播放石头破坏粒子。
	 */
	private static void updateKomainuStatueState(LivingEntity entity, FeralForm form) {
		if (!(entity instanceof Player player) || !form.hasStatueState()) {
			// 形态切换等情况下，非狛犬状态不留残影
			STATUE_TRACKS.remove(entity);
			return;
		}
		// 双端各自跟踪位置静止状态：服务端用于仇恨豁免与粒子，客户端用于渲染贴图/动画
		boolean statueBefore = isInStatue(player);
		StatueTrack track = STATUE_TRACKS.computeIfAbsent(player, p -> new StatueTrack());
		boolean moved = !track.initialized
				|| entity.position().distanceToSqr(track.lastX, track.lastY, track.lastZ) > STATUE_MOVE_THRESHOLD_SQR;

		if (statueBefore) {
			if (player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza) {
				// 磐座石像仅按 shift 解除：使用物品、挥动手臂等动作不打断石像状态
				if (player.isShiftKeyDown()) {
					track.idleTicks = 0;
					// 解除磐座石像：清除模式标记并同步
					player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza = false;
					player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
				}
			} else if (player.isUsingItem() || player.swinging) {
				// 普通石像维持：使用物品/挥动手臂等动作会立即解除石像
				track.idleTicks = 0;
			} else if (moved) {
				track.idleTicks = 0;
			}
		} else {
			// 进入条件：静止且不潜行（潜行静止不会进入石像状态）；自然进入为普通石像
			if (player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza) {
				player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza = false;
				player.getData(MurmolModVariables.PLAYER_VARIABLES).markSyncDirty();
			}
			if (player.isShiftKeyDown() || moved) {
				track.idleTicks = 0;
			} else if (track.idleTicks < STATUE_IDLE_TICKS) {
				track.idleTicks++;
			}
		}
		track.lastX = entity.getX();
		track.lastY = entity.getY();
		track.lastZ = entity.getZ();
		track.initialized = true;

		boolean statueNow = isInStatue(player);
		// 效果与粒子仅在服务端执行
		if (statueNow && !entity.level().isClientSide()) {
			// 抗性提升 II，短时长每 tick 续期，退出即失效
			entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, true, false));
			// 磐座石像物理定身：清零速度（含击退），防止被推离磐座导致状态被移动解除
			if (player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza) {
				entity.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
				player.hurtMarked = true;
			}
			// 磐座石像缓慢回血：每 2 秒回复半颗心
			if (player.getData(MurmolModVariables.PLAYER_VARIABLES).statueBanza
					&& entity.tickCount % 40 == 0 && entity.getHealth() < entity.getMaxHealth()) {
				entity.heal(1.0F);
			}
		}
		if (statueNow != statueBefore) {
			// 状态切换：石头破坏粒子 + 破坏音效
			spawnStatueParticles(player);
			if (!entity.level().isClientSide())
				playStatueSound(player);
		}
	}

	/** 石头破坏音效：进出石像均播放，服务端广播给附近玩家 */
	private static void playStatueSound(LivingEntity entity) {
		entity.level().playSound(null, entity.getX(), entity.getY() + 0.5D, entity.getZ(),
				net.minecraft.sounds.SoundEvents.STONE_BREAK, net.minecraft.sounds.SoundSource.PLAYERS,
				1.0F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	/** 石头破坏粒子：服务端广播给附近玩家，客户端额外本地补一层（保证发起者自己一定能看到） */
	private static void spawnStatueParticles(LivingEntity entity) {
		var state = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
		var option = new net.minecraft.core.particles.BlockParticleOption(
				net.minecraft.core.particles.ParticleTypes.BLOCK, state);
		if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			serverLevel.sendParticles(option, entity.getX(), entity.getY() + 1.0, entity.getZ(),
					60, 0.5, 0.9, 0.5, 0.15);
		} else if (entity.level() instanceof Level level) {
			// 客户端本地备份
			var random = level.random;
			for (int i = 0; i < 40; i++) {
				double x = entity.getX() + (random.nextDouble() - 0.5) * 1.0;
				double y = entity.getY() + 0.4 + random.nextDouble() * 1.6;
				double z = entity.getZ() + (random.nextDouble() - 0.5) * 1.0;
				level.addParticle(option, x, y, z, 0, 0, 0);
			}
		}
	}

	/**
	 * 苔叶兽环境加成：在洞穴中（所处位置天空光为 0）获得移速 +0.02（相对基础移速 +20%）、跳跃力 +0.2、挖掘效率 +1；
	 * 在地面上则改为移速 -0.015（相对基础移速 -15%）。其余形态无环境修饰符。
	 */
	private static void updateMossBeastEnvironmentModifiers(LivingEntity entity, FeralForm form) {
		if (form != FeralForms.MOSS_BEAST)
			return;
		boolean inCave = entity.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, entity.blockPosition()) == 0;
		if (inCave) {
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, CAVE_MODIFIER_ID, 0.02);
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH, CAVE_MODIFIER_ID, 0.2);
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY, CAVE_MODIFIER_ID, 1);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, SURFACE_MODIFIER_ID);
		} else {
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, SURFACE_MODIFIER_ID, -0.015);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, CAVE_MODIFIER_ID);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH, CAVE_MODIFIER_ID);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY, CAVE_MODIFIER_ID);
		}
	}

	/**
	 * 月蛾昼夜减益：白天露天下移速 -0.05、最大生命值 -4；夜晚露天则相反获得 +0.05/+4；
	 * 白天（或夜晚）黑暗处（天空光为 0）无任何修饰。
	 */
	private static void updateSilkmothEnvironmentModifiers(LivingEntity entity, FeralForm form) {
		if (form != FeralForms.SILKMOTH)
			return;
		boolean seeSky = entity.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, entity.blockPosition()) > 0;
		boolean isDay = entity.level().isDay();
		boolean dayBuffed = isDay && seeSky;
		boolean nightBuffed = !isDay && seeSky;
		if (dayBuffed) {
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, DAY_MODIFIER_ID, -0.05);
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, DAY_MODIFIER_ID, -4);
		} else {
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, DAY_MODIFIER_ID);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, DAY_MODIFIER_ID);
		}
		if (nightBuffed) {
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, NIGHT_MODIFIER_ID, 0.05);
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, NIGHT_MODIFIER_ID, 4);
		} else {
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, NIGHT_MODIFIER_ID);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, NIGHT_MODIFIER_ID);
		}
	}

	/** 文鳐陆上"缺氧量"持久计数器（服务端，每 tick +1，入水清零） */
	private static final java.util.WeakHashMap<LivingEntity, Integer> WENYAO_AIR_DEFICIT = new java.util.WeakHashMap<>();

	/**
	 * 文鳐水陆差异：水中呼吸正常（每 tick 回满氧气）且游速快（移速 +0.06、水动效率 +1.0，海豚式畅游）；
	 * 陆地上移动极慢（移速 -0.085）且无法呼吸——氧气逐 tick 消耗，耗尽后每 20 tick 受到 2 点溺水伤害。
	 */
	private static void updateWenyaoEnvironmentModifiers(LivingEntity entity, FeralForm form) {
		if (form != FeralForms.WENYAO)
			return;
		boolean inWater = entity.isEyeInFluid(net.minecraft.tags.FluidTags.WATER);
		if (inWater) {
			// 只能在水里呼吸：入水即回满氧气并清零陆上缺氧计数
			WENYAO_AIR_DEFICIT.remove(entity);
			entity.setAirSupply(entity.getMaxAirSupply());
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, WENYAO_WATER_ID, 0.06);
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY, WENYAO_WATER_ID, 1.0);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, WENYAO_LAND_ID);
		} else {
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, WENYAO_LAND_ID, -0.085);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, WENYAO_WATER_ID);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY, WENYAO_WATER_ID);
			// 陆上窒息：原版 baseTick 在陆上每 tick 回满氧气（先于本事件），无法从当前氧气反推累计值，
			// 改用持久"缺氧量"计数器逐 tick 加深并压制回充，气泡耗尽后再过 20 tick 受到 2 点溺水伤害并重置（创造模式豁免）
			if (!entity.level().isClientSide() && entity.isAlive()
					&& !(entity instanceof Player player && player.getAbilities().invulnerable)) {
				int max = entity.getMaxAirSupply();
				int deficit = WENYAO_AIR_DEFICIT.merge(entity, 1, Integer::sum);
				if (deficit > max + 20) {
					WENYAO_AIR_DEFICIT.put(entity, max);
					entity.hurt(entity.damageSources().drown(), 2.0F);
				}
				entity.setAirSupply(max - Math.min(deficit, max));
			}
		}
	}

	// ==================== 狰：恐惧光环 ====================

	/** 狰形态：每 20 tick（1 秒）对周围 6 格内敌对生物施加虚弱 I（2 秒），它们本就会因 AvoidEntityGoal 逃离 */
	private static void updateFerociousFearAura(LivingEntity entity, FeralForm form) {
		if (form != FeralForms.FEROCIOUS || entity.level().isClientSide() || entity.tickCount % 20 != 0)
			return;
		var mobs = entity.level().getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
				entity.getBoundingBox().inflate(6.0D));
		for (var mob : mobs) {
			mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false));
		}
	}

	// ==================== 春花狐龙：自然光环 ====================

	/** 春花狐龙：每 20 tick 随机催熟周围 5 格内作物（骨粉效果），每 100 tick 让周围动物进入繁殖状态 */
	private static void updateLuohongNatureAura(LivingEntity entity, FeralForm form) {
		if (form != FeralForms.LUOHONG || entity.level().isClientSide())
			return;
		var level = entity.level();
		// 催熟作物：每 20 tick 随机选一个周围作物方块催熟
		if (entity.tickCount % 20 == 0) {
			var random = level.random;
			for (int i = 0; i < 3; i++) {
				int dx = random.nextInt(11) - 5;
				int dz = random.nextInt(11) - 5;
				int dy = random.nextInt(3) - 1;
				var pos = entity.blockPosition().offset(dx, dy, dz);
				var state = level.getBlockState(pos);
				if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
						&& state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock mealable
						&& mealable.isValidBonemealTarget(level, pos, state)
						&& mealable.isBonemealSuccess(serverLevel, random, pos, state)) {
					mealable.performBonemeal(serverLevel, random, pos, state);
					level.levelEvent(1505, pos, 0); // 骨粉粒子
				}
			}
		}
		// 动物繁殖：每 100 tick 让周围 6 格内成年动物进入"爱"模式
		if (entity.tickCount % 100 == 0) {
			var animals = level.getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class,
					entity.getBoundingBox().inflate(6.0D));
			for (var animal : animals) {
				if (animal.isAlive() && !animal.isBaby() && animal.getLoveCause() == null) {
					animal.setInLove(null);
				}
			}
		}
	}

	// ==================== 乘黄：骑乘增益 ====================

	/** 乘黄形态：附近 2.5 格内的其他玩家持续获得抗性提升 I 与生命恢复 I（模拟骑乘增益） */
	private static void updateChenHuangRiderBuffs(LivingEntity entity, FeralForm form) {
		if (form != FeralForms.CHEN_HUANG || entity.level().isClientSide())
			return;
		// 先处理真正的乘客（若有）
		for (var passenger : entity.getPassengers()) {
			if (passenger instanceof LivingEntity living) {
				living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
				living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false));
			}
		}
		// 附近玩家（2.5 格内）也获得骑乘增益
		var nearby = entity.level().getEntitiesOfClass(Player.class,
				entity.getBoundingBox().inflate(2.5D), p -> p != entity && p.isAlive());
		for (var player : nearby) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false));
		}
	}

	// ==================== 月蛾：受伤吐丝 ====================

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent.Post event) {
		LivingEntity victim = event.getEntity();
		if (victim.level().isClientSide() || getForm(victim) != FeralForms.SILKMOTH)
			return;
		// 月蛾受伤：获得盘丝（WEAVING）5 秒
		victim.addEffect(new MobEffectInstance(MobEffects.WEAVING, 100, 0, true, false));
	}

	private static void setTransientModifier(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
			ResourceLocation id, double amount) {
		var instance = entity.getAttribute(attr);
		if (instance == null)
			return;
		if (instance.hasModifier(id)) {
			return;
		}
		instance.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, amount,
				net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
	}

	private static void removeModifier(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
			ResourceLocation id) {
		var instance = entity.getAttribute(attr);
		if (instance != null) {
			instance.removeModifier(id);
		}
	}

	/**
		 * 悬停飞行的运动逻辑（仅客户端每 tick 调用；客户端是自身移动的权威端，速度修改会随位置包同步）。
		 * 长按跳跃键（空格）缓慢上升，松手自然下落。
		 */
	public static void hoverFlightClientTick(Player player, FeralForm form) {
		if (!form.isFeral() || !form.canHoverFlight() || useElytraFlight(player))
			return;
		// 仅在按下跳跃键（空格）时缓慢上升，松手自然下落
		if (!net.minecraft.client.Minecraft.getInstance().options.keyJump.isDown())
			return;
		net.minecraft.world.phys.Vec3 v = player.getDeltaMovement();
		// 缓慢上升：逐步加速至约 0.36 格/tick（原 0.12 的 3 倍）
		player.setDeltaMovement(v.x, Math.min(v.y * 0.6 + 0.15, 0.36), v.z);
	}

	@SubscribeEvent
	public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
		LivingEntity entity = event.getEntity();
		FeralForm form = getForm(entity);
		if (!form.isFeral())
			return;
		// 野性形态下，非白名单护甲会掉落
		TagKey<net.minecraft.world.item.Item> nodiaoluo = ItemTags.create(ResourceLocation.parse("mod:nodiaoluo"));
		// 月蛾等形态允许穿胸甲：跳过胸部槽位判定
		if (!form.canWearChestArmor())
			dropArmorIfNeeded(entity, EquipmentSlot.CHEST, nodiaoluo, ItemTags.create(ResourceLocation.parse("minecraft:chest_armor")));
		dropArmorIfNeeded(entity, EquipmentSlot.LEGS, nodiaoluo, ItemTags.create(ResourceLocation.parse("minecraft:leg_armor")));
		dropArmorIfNeeded(entity, EquipmentSlot.FEET, nodiaoluo, ItemTags.create(ResourceLocation.parse("minecraft:foot_armor")));
	}

	private static void dropArmorIfNeeded(LivingEntity entity, EquipmentSlot slot,
			TagKey<net.minecraft.world.item.Item> whitelist, TagKey<net.minecraft.world.item.Item> armorTag) {
		ItemStack stack = entity.getItemBySlot(slot);
		if (stack.is(whitelist))
			return;
		if (!stack.is(armorTag))
			return;
		// 薛定谔附魔：装备在变形时不会自动脱落
		if (hasSchrodinger(stack, entity))
			return;
		// 原版丢弃机制：直接从实体身上掉落（保留 NBT、随机散布），而非销毁后再造掉落物
		if (entity instanceof Player player) {
			player.drop(stack, true);
		} else {
			entity.spawnAtLocation(stack);
		}
		// 丢弃后清空对应盔甲槽位
		if (entity instanceof Player player) {
			int idx = switch (slot) {
				case FEET -> 0;
				case LEGS -> 1;
				case CHEST -> 2;
				case HEAD -> 3;
				default -> -1;
			};
			if (idx >= 0) {
				player.getInventory().armor.set(idx, ItemStack.EMPTY);
				player.getInventory().setChanged();
			}
		} else {
			entity.setItemSlot(slot, ItemStack.EMPTY);
		}
	}

	/** 薛定谔附魔检查：该装备是否附有 murmol:schrodinger */
	private static boolean hasSchrodinger(ItemStack stack, LivingEntity entity) {
		var key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("murmol:schrodinger"));
		return stack.getEnchantmentLevel(entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key)) > 0;
	}

	/**
	 * 薛定谔附魔获取途径：图书管理员村民 4 级（专家）出售附魔书（12 绿宝石）。
	 */
	@SubscribeEvent
	public static void onVillagerTrades(net.neoforged.neoforge.event.village.VillagerTradesEvent event) {
		if (event.getType() != net.minecraft.world.entity.npc.VillagerProfession.LIBRARIAN)
			return;
		event.getTrades().computeIfAbsent(4, k -> new java.util.ArrayList<>()).add((trader, rand) -> {
			ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
			var key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("murmol:schrodinger"));
			var holder = trader.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
			book.enchant(holder, 1);
			return new net.minecraft.world.item.trading.MerchantOffer(
					new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, 12), book, 0, 3, 8);
		});
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
		// 人类杀手附魔：仅对掠夺者系列（灾厄村民）、村民和未变形（人类形态）玩家造成额外伤害
		boolean isHumanoid = target instanceof net.minecraft.world.entity.monster.AbstractIllager
				|| target instanceof net.minecraft.world.entity.npc.Villager
				|| (target instanceof net.minecraft.world.entity.player.Player p && !getForm(p).isFeral());
		if (isHumanoid) {
			target.hurt(target.damageSources().generic(), level * 2);
		}
	}

	/** 文鳐饮用任意药水/水瓶完成后回满氧气，并清零陆上缺氧计数 */
	@SubscribeEvent
	public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntity();
		if (getForm(entity) == FeralForms.WENYAO && event.getItem().is(Items.POTION)) {
			WENYAO_AIR_DEFICIT.remove(entity);
			entity.setAirSupply(entity.getMaxAirSupply());
		}
	}
}
