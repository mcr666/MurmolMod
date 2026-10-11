package mcr.murmol.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import mcr.richi.game.MahjongLobby;

/**
 * Murmol NPC：友好型 AI（不攻击、不反击），艾利克斯模型 + Murmol 皮肤（见 MurmolNpcRenderer）。
 * 右键弹出对话叠加层（NpcDialogueHud，数字键选择）：确认后加入 16 格内最近的麻雀桌等候队列（占据方式同玩家，可被移出）；
 * 等候与对局期间钉在位置上（对局中取代 AI 假玩家形象打牌，动作时挥手）；终局/移出解除固定。
 */
public class MurmolNpcEntity extends PathfinderMob {
	public MurmolNpcEntity(EntityType<? extends PathfinderMob> type, Level world) {
		super(type, world);
		setPersistenceRequired(); // 刷怪蛋放置后不消失
		setCustomName(net.minecraft.network.chat.Component.literal("Murmol"));
		setCustomNameVisible(true);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	// ==================================================================
	// 伤害免疫：火焰/岩浆/闪电/溺水/药水魔法/环境伤害与本 mod 伤害类型免疫；
	// 直接攻击（近战、弹射物等）不免疫，仍可被击杀。
	// ==================================================================

	/** 本 mod 星界侵蚀伤害类型（data/murmol/damage_type/astral_infection_damage.json） */
	private static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType>
			ASTRAL_INFECTION_DAMAGE = net.minecraft.resources.ResourceKey.create(
					net.minecraft.core.registries.Registries.DAMAGE_TYPE,
					net.minecraft.resources.ResourceLocation.parse("murmol:astral_infection_damage"));

	@Override
	public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
		if (source.is(ASTRAL_INFECTION_DAMAGE) || IMMUNE_DAMAGE_TYPES.stream().anyMatch(source::is))
			return false;
		return super.hurt(source, amount);
	}

	/** 免疫的原版伤害类型（攻击类 MOB_ATTACK/PLAYER_ATTACK/ARROW/TRIDENT/MOB_PROJECTILE 不在内） */
	private static final java.util.List<net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType>>
			IMMUNE_DAMAGE_TYPES = java.util.List.of(
				// 火焰/高温
				net.minecraft.world.damagesource.DamageTypes.IN_FIRE,
				net.minecraft.world.damagesource.DamageTypes.ON_FIRE,
				net.minecraft.world.damagesource.DamageTypes.LAVA,
				net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR,
				// 闪电/溺水
				net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT,
				net.minecraft.world.damagesource.DamageTypes.DROWN,
				net.minecraft.world.damagesource.DamageTypes.DRY_OUT,
				// 药水/魔法
				net.minecraft.world.damagesource.DamageTypes.MAGIC,
				net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC,
				net.minecraft.world.damagesource.DamageTypes.WITHER,
				net.minecraft.world.damagesource.DamageTypes.WITHER_SKULL,
				net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH,
				// 环境/特殊
				net.minecraft.world.damagesource.DamageTypes.FALL,
				net.minecraft.world.damagesource.DamageTypes.CACTUS,
				net.minecraft.world.damagesource.DamageTypes.SWEET_BERRY_BUSH,
				net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK,
				net.minecraft.world.damagesource.DamageTypes.CRAMMING,
				net.minecraft.world.damagesource.DamageTypes.STARVE,
				net.minecraft.world.damagesource.DamageTypes.FREEZE,
				net.minecraft.world.damagesource.DamageTypes.FIREWORKS,
				net.minecraft.world.damagesource.DamageTypes.EXPLOSION,
				net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION,
				net.minecraft.world.damagesource.DamageTypes.OUTSIDE_BORDER,
				net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL);

	@Override
	public boolean ignoreExplosion(net.minecraft.world.level.Explosion explosion) {
		return true;
	}

	// ==================================================================
	// 死亡播报：模仿玩家死亡信息（原版生物死亡不进聊天框）
	// ==================================================================

	@Override
	public void die(net.minecraft.world.damagesource.DamageSource source) {
		super.die(source);
		if (!level().isClientSide
				&& level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_SHOWDEATHMESSAGES)) {
			// 与玩家死亡播报同款文本（death.attack.* 翻译键），广播到全服聊天框
			Component msg = source.getLocalizedDeathMessage(this);
			((ServerLevel) level()).getServer().getPlayerList().broadcastSystemMessage(msg, false);
		}
		if (!level().isClientSide && queuedTable != -1) {
			MahjongLobby.leaveNpc((ServerLevel) level(), this); // 死亡即退出队列/对局并解除钉定
		}
	}

	// ==================================================================
	// 位置固定（等候队列/对局期间）
	// ==================================================================

	/** 钉定坐标与朝向（null = 未钉定）；钉定期间每刻归位，抑制漫步 AI */
	private Vec3 pinPos;
	private float pinYaw;
	/** 对局钉定：面向桌心，不注视玩家（等待期钉定仍允许看向玩家） */
	private boolean pinGame;
	/** 已加入的牌桌（origin 坐标 asLong，-1 = 未加入）；右键再点即退出队列 */
	private long queuedTable = -1;

	public boolean isPinned() {
		return pinPos != null;
	}

	/** 已加入的牌桌 origin 长坐标（-1 = 未加入） */
	public long queuedTable() {
		return queuedTable;
	}

	public void setQueuedTable(long origin) {
		this.queuedTable = origin;
	}

	/** 钉在指定位置与朝向（等候期停驻原地） */
	public void pinAt(Vec3 pos, float yaw) {
		this.pinPos = pos;
		this.pinYaw = yaw;
		this.pinGame = false;
	}

	/** 对局钉定：钉在座位站位并面向桌心（不注视玩家） */
	public void pinForGame(Vec3 pos, float yaw) {
		this.pinPos = pos;
		this.pinYaw = yaw;
		this.pinGame = true;
	}

	/** 解除位置固定（离开牌局/牌局结束） */
	public void unpin() {
		this.pinPos = null;
		this.pinGame = false;
		this.queuedTable = -1;
	}

	@Override
	public void tick() {
		super.tick();
		// 火焰伤害免疫连带视觉：身上不着火
		if (this.getRemainingFireTicks() > 0)
			this.setRemainingFireTicks(0);
		// 常驻生命恢复 1：每 40 刻续一次 80 刻的效果（visible=false → 不显示粒子与图标）
		if (!level().isClientSide && this.tickCount % 40 == 0)
			this.addEffect(new net.minecraft.world.effect.MobEffectInstance(
					net.minecraft.world.effect.MobEffects.REGENERATION, 80, 0, false, false));
		if (pinPos != null && !level().isClientSide) {
			this.getNavigation().stop();
			if (this.distanceToSqr(pinPos.x, pinPos.y, pinPos.z) > 0.0004) {
				// 1.21.1 无 (Level,...) 重载：手动转型到 ServerLevel 后带相对位移集合传送
				this.teleportTo((ServerLevel) level(), pinPos.x, pinPos.y, pinPos.z, java.util.Set.of(), pinYaw, 0.0f);
				this.setYRot(pinYaw);
			} else if (pinGame) {
				// 对局中：锁朝向为桌心方向，覆盖注视/环顾 AI 的转头
				//（tick 顺序在目标 AI 之后，直接覆写头/身转向即可，无需动 LookControl）
				this.setYRot(pinYaw);
				this.setYHeadRot(pinYaw);
				this.setXRot(0.0f);
			} else
				this.setYRot(pinYaw);
		}
	}

	// ==================================================================
	// 右键交互：对话叠加层（服务端推送选项，客户端数字键选择）
	// 服务端按玩家记忆对话状态（menu 根菜单 / join 麻雀邀约 / reply_* 仅回复无选项），选择后重算选项执行
	// ==================================================================

	/** 各玩家当前对话状态（menu/join/reply_about/reply_soul；离开对话即移除） */
	private static final java.util.Map<java.util.UUID, String> DIALOGUE_STATE = new java.util.HashMap<>();

	/** 指定状态的选项动作列表（顺序 = 数字键 1..n；动作标签键 gui.murmol.npc.act_<动作>；空 = 仅可离开） */
	private static java.util.List<String> optionsFor(String state) {
		return switch (state) {
			case "join" -> List.of("confirm_join", "back");
			case "reply_about", "reply_soul" -> List.of(); // 叠加层回复：无数字选项，只能 0 离开
			default -> List.of("join", "about", "soul");
		};
	}

	private static String titleFor(String state) {
		return switch (state) {
			case "join" -> "gui.murmol.npc.title_join";
			case "reply_about", "reply_soul" -> "gui.murmol.npc.reply_unavailable";
			default -> "gui.murmol.npc.title";
		};
	}

	private static void sendDialogue(ServerPlayer sp, MurmolNpcEntity npc, String state) {
		java.util.List<String> opts = optionsFor(state);
		// 空选项时传占位符 "-"：客户端保持面板打开但不渲染数字选项（空字符串 = 关闭对话）
		String payload = opts.isEmpty() ? "-" : String.join(";", opts);
		net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
				new mcr.murmol.network.NpcDialoguePayloads.OpenMessage(npc.getId(), titleFor(state), payload));
	}

	/** 服务端：向玩家推送对话叠加层（每次右键重置为根菜单） */
	public static void openDialogue(ServerPlayer sp, MurmolNpcEntity npc) {
		DIALOGUE_STATE.put(sp.getUUID(), "menu");
		sendDialogue(sp, npc, "menu");
	}

	/** 服务端：处理数字键选择（按当前状态重算选项后校验执行） */
	public static void handleDialogueSelect(ServerPlayer sp, int npcId, int index) {
		if (!(sp.serverLevel().getEntity(npcId) instanceof MurmolNpcEntity npc)
				|| sp.distanceToSqr(npc) > 36.0)
			return;
		String state = DIALOGUE_STATE.getOrDefault(sp.getUUID(), "menu");
		java.util.List<String> opts = optionsFor(state);
		if (index < 1 || index > opts.size())
			return;
		switch (opts.get(index - 1)) {
			case "join" -> {
				if (npc.queuedTable != -1) {
					sp.sendSystemMessage(Component.literal("§6<Murmol>§r ")
							.append(Component.translatable("gui.murmol.npc.already_queued")));
					sendDialogue(sp, npc, "menu");
				} else {
					DIALOGUE_STATE.put(sp.getUUID(), "join");
					sendDialogue(sp, npc, "join");
				}
			}
			case "back" -> {
				DIALOGUE_STATE.put(sp.getUUID(), "menu");
				sendDialogue(sp, npc, "menu");
			}
			case "confirm_join" -> {
				DIALOGUE_STATE.remove(sp.getUUID()); // 执行后退出对话
				ServerLevel level = sp.serverLevel();
				net.minecraft.core.BlockPos nearest = mcr.richi.game.RichiTableManager.nearest(level, npc.position(), 16);
				if (nearest != null && mcr.richi.game.MahjongLobby.npcJoinTable(level, npc, nearest)) {
					sp.sendSystemMessage(Component.literal("§6<幻星麻雀>§r Murmol 已加入等候队列"));
				} else {
					sp.sendSystemMessage(Component.literal("§c16 格内没有可加入的牌桌等候队列"));
				}
			}
			case "about", "soul" -> {
				// 叠加层回复：面板显示回复内容，无数字选项，只能 0 离开
				DIALOGUE_STATE.put(sp.getUUID(), "about".equals(opts.get(index - 1)) ? "reply_about" : "reply_soul");
				sendDialogue(sp, npc, DIALOGUE_STATE.get(sp.getUUID()));
			}
			default -> {
			}
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer sp)
			openDialogue(sp, this);
		return InteractionResult.SUCCESS;
	}

	// ==================================================================

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.ARMOR, 10.0) // 护甲值 10 点
				.add(Attributes.MOVEMENT_SPEED, 0.25)
				.add(Attributes.FOLLOW_RANGE, 16.0);
	}
}
