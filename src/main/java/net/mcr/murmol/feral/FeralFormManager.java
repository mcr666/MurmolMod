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
	 * 物品触发的变形（如堕落者图腾、灵魂物品）。直接变形为指定形态。
	 * 目标形态与当前形态相同则 no-op（防 NeoOrigins 起源联动循环触发）。
	 */
	public static void transformFromItem(LevelAccessor world, double x, double y, double z, Entity entity, FeralForm form, ItemStack itemstack) {
		if (entity == null)
			return;
		if (getForm(entity) == form)
			return;
		playTransformSound(world, x, y, z);
		setForm(entity, form);
		awardAdvancement(entity, form);
		addTransformEffects(entity);
		// NeoOrigins 联动：变形后同步起源（软依赖，未安装时静默跳过）
		if (entity instanceof ServerPlayer player)
			net.mcr.murmol.compat.NeoOriginsCompat.syncOrigin(player, form);
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
		// 悬停飞行形态免疫摔落伤害（每 tick 清零累计摔落距离）
		if (form.isFeral() && form.canHoverFlight() && entity.fallDistance > 0) {
			entity.fallDistance = 0;
		}
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

	/** 该玩家是否站在磐座（banza）上：取脚底略下方（minY - 0.05）所在方块，
	 * 不能用 getBlockPosBelowThatAffectsMyMovement——磐座高度仅 4/16，该方法会返回磐座下方的方块 */
	public static boolean isOnBanza(LivingEntity entity) {
		return entity.level().getBlockState(BlockPos.containing(entity.getX(), entity.getBoundingBox().minY - 0.05D, entity.getZ()))
				.is(net.mcr.murmol.init.MurmolModBlocks.BANZA.get());
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
			// 石像维持：磐座上仅按 shift 解除；离开磐座后移动即解除
			if (isOnBanza(player)) {
				if (player.isShiftKeyDown())
					track.idleTicks = 0;
			} else if (moved) {
				track.idleTicks = 0;
			}
		} else {
			// 进入条件：静止且不潜行（潜行静止不会进入石像状态）
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
			// 抗性提升 I，短时长每 tick 续期，退出即失效
			entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
			// 磐座上的石像物理定身：清零速度（含击退），防止被推离磐座导致状态被移动解除
			if (isOnBanza(player)) {
				entity.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
				player.hurtMarked = true;
			}
			// 磐座上的石像缓慢回血：每 2 秒回复半颗心
			if (isOnBanza(player) && entity.tickCount % 40 == 0 && entity.getHealth() < entity.getMaxHealth()) {
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
	 * 苔叶兽环境加成：在洞穴中（所处位置天空光为 0）获得移速 +0.01、跳跃力 +0.2、挖掘效率 +1；
	 * 在地面上则改为移速 -0.05。其余形态无环境修饰符。
	 */
	private static void updateMossBeastEnvironmentModifiers(LivingEntity entity, FeralForm form) {
		if (form != FeralForms.MOSS_BEAST)
			return;
		boolean inCave = entity.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, entity.blockPosition()) == 0;
		if (inCave) {
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, CAVE_MODIFIER_ID, 0.01);
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH, CAVE_MODIFIER_ID, 0.2);
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY, CAVE_MODIFIER_ID, 1);
			removeModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, SURFACE_MODIFIER_ID);
		} else {
			setTransientModifier(entity, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, SURFACE_MODIFIER_ID, -0.05);
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
		if (!form.isFeral() || !form.canHoverFlight())
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
		// 量子态附魔：装备在变形时不会自动脱落
		if (hasQuantumState(stack, entity))
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

	/** 量子态附魔检查：该装备是否附有 murmol:quantum_state */
	private static boolean hasQuantumState(ItemStack stack, LivingEntity entity) {
		var key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("murmol:quantum_state"));
		return stack.getEnchantmentLevel(entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key)) > 0;
	}

	/**
	 * 量子态附魔获取途径：图书管理员村民 4 级（专家）出售附魔书（12 绿宝石）。
	 */
	@SubscribeEvent
	public static void onVillagerTrades(net.neoforged.neoforge.event.village.VillagerTradesEvent event) {
		if (event.getType() != net.minecraft.world.entity.npc.VillagerProfession.LIBRARIAN)
			return;
		event.getTrades().computeIfAbsent(4, k -> new java.util.ArrayList<>()).add((trader, rand) -> {
			ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
			var key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("murmol:quantum_state"));
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
