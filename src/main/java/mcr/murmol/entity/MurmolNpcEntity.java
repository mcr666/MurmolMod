package mcr.murmol.entity;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import mcr.richi.game.MahjongLobby;

/**
 * Murmol NPC：友好型 AI（不攻击、不反击），艾利克斯模型 + Murmol 皮肤（见 MurmolNpcRenderer）。
 * 右键弹出可点击聊天选项：确认后加入 16 格内最近的麻雀桌等候队列（占据方式同玩家，可被移出）；
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
	// 右键交互：可点击聊天选项加入/退出麻雀队列
	// ==================================================================

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer sp) {
			if (queuedTable != -1) {
				MahjongLobby.leaveNpc(sp.serverLevel(), this);
				return InteractionResult.SUCCESS;
			}
			MutableComponent accept = Component.literal("§a[接受]§r")
					.withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
							"/murmol npc join " + getId())));
			MutableComponent decline = Component.literal("§7[拒绝]§r")
					.withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
							"/murmol npc decline")));
			sp.sendSystemMessage(Component.literal("§6<Murmol>§r 想和我来一局麻雀吗？ ")
					.append(accept).append(" ").append(decline));
		}
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
