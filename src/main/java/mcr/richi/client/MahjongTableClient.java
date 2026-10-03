package mcr.richi.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import mcr.murmol.feral.client.VillagerAvatarHandler;
import mcr.murmol.network.MurmolModVariables;
import mcr.richi.MahjongTileItem;
import mcr.richi.block.FengPanBlock;
import mcr.richi.game.MahjongTileNotation;
import mcr.richi.game.RichiTableState;
import mcr.richi.game.riichi.RiichiWall;
import mcr.richi.network.MahjongTablePayload;
import mcr.richi.render.DisplayEntityNbt;
import mcr.richi.render.TableLayout;

/**
 * 麻雀牌桌客户端渲染：收到 {@link MahjongTablePayload}（服务端按观看者过滤后的快照）后
 * 增量更新 client-side 显示实体。每个逻辑牌位有稳定 key（手牌:座位:牌面:内序 / 牌河:座位:下标 /
 * 副露:座位:序 / 点棒:座位:序 / 宝牌:叠:底顶 / 座位标签等），按"签名"（kind+牌面+正反面+立平形态，
 * 文本实体为文本内容）比对：签名不变仅位置/朝向变化 → 复用实体并走 display 自带 pos-rot 插值
 * （teleport_duration，见 lerpTo）平滑移动；签名变化或实体丢失 → 只重建该实体；新 key 生成；
 * 消失 key 删除。不变的实体不再 discard/respawn，杜绝全桌闪烁。
 * 发牌动画（dealAnim&gt;0）仍整桌清场后按 Placement.delay 逐张生成（倒牌/盖牌两段：先立牌 delay、
 * 后平躺 delay+10，同 key 二次生成自动替换立牌）；选中升降增量移动对应实体（handIds 下标索引）。
 * 座位标签/中心信息/结算横幅为 text_display，内容变化就地 load 更新 text NBT 而不重建。
 */
public final class MahjongTableClient {
	private MahjongTableClient() {
	}

	/** 桌上某逻辑牌位的实体记录：实体 id + 外观签名（文本实体签名 = 文本内容） */
	private record Entry(int entityId, String sig) {
	}

	/** 摆位项 + 预计算稳定 key / 外观签名 / 文本内容（仅座位标签非空） */
	private record Slot(TableLayout.Placement p, String slotKey, String sig, String text) {
	}

	/** 一张桌的客户端渲染记录：key→实体清单 + 手牌(座位,下标)→实体 id（选中升降用） + 最近一包 */
	private static final class TableRender {
		final Map<String, Entry> keyed = new HashMap<>();
		final Map<Integer, Integer> handIds = new HashMap<>();
		MahjongTablePayload.ViewMessage last;
	}

	private static final Map<Long, TableRender> TABLES = new HashMap<>();
	/** 各桌 AI 座位假玩家（key = 桌 originPacked，value = 座位 → 假玩家记录） */
	private static final Map<Long, Map<Integer, Avatar>> AVATARS = new HashMap<>();
	/** 随机模式未揭示的盔甲架占位（等待阶段，桌边随机位置） */
	private static final Map<Long, Map<Integer, net.minecraft.world.entity.decoration.ArmorStand>> STANDS = new HashMap<>();
	/** 假玩家客户端实体 id 计数（负数且递减，与服务端实体 id 区间隔离） */
	private static int nextAvatarEntityId = -1_000_000;

	/** Murmol 本地内置皮肤（assets/murmol/textures/entity/murmol.png），不拉正版档案；人类形象名为 Murmol 时使用 */
	private static final net.minecraft.client.resources.PlayerSkin MURMOL_SKIN = new net.minecraft.client.resources.PlayerSkin(
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("murmol", "textures/entity/murmol.png"),
			null, null, null, net.minecraft.client.resources.PlayerSkin.Model.SLIM, false);

	/** 隐形坐骑载具的抬升（格）：玩家坐标由骑乘挂点跟随载具，调此值即调坐姿高度；human 形象不加 */
	private static final double SEAT_LIFT = 0.625;

	/** 座位抬升：human 形象无额外位移，其余（villager/各形态）抬高 */
	private static double seatLift(String form) {
		return mcr.murmol.feral.FeralForm.HUMAN_ID.equals(form) ? 0.0D : SEAT_LIFT;
	}

	/** AI 座位假玩家记录：实体 + 隐形骑乘载具（提供原版 riding 坐姿）+ 形态 id + 档案 UUID */
	private record Avatar(RemotePlayer player, net.minecraft.world.entity.decoration.ArmorStand seat,
			String form, UUID profileUuid) {
	}

	/** 待执行的动画任务（发牌 stagger / 倒牌级联），按桌分组 */
	private static final Map<Long, List<Scheduled>> SCHEDULED = new HashMap<>();
	private static int tickCounter;

	private record Scheduled(long at, Runnable run) {
	}

	/** 客户端 tick（MahjongClient 转发）：驱动发牌/倒牌动画 */
	public static void tick() {
		tickCounter++;
		if (SCHEDULED.isEmpty())
			return;
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			SCHEDULED.clear();
			return;
		}
		for (Map.Entry<Long, List<Scheduled>> entry : SCHEDULED.entrySet()) {
			List<Scheduled> list = entry.getValue();
			List<Scheduled> rest = null;
			List<Runnable> due = null;
			for (Scheduled s : list) {
				if (s.at() <= tickCounter)
					(due == null ? due = new ArrayList<>() : due).add(s.run());
				else
					(rest == null ? rest = new ArrayList<>() : rest).add(s);
			}
			list.clear();
			if (rest != null)
				list.addAll(rest);
			if (due != null && TABLES.containsKey(entry.getKey()))
				due.forEach(Runnable::run);
		}
		SCHEDULED.entrySet().removeIf(e -> e.getValue().isEmpty());
	}

	private static void schedule(long key, int delay, Runnable run) {
		if (delay <= 0) {
			run.run();
			return;
		}
		// 发牌节流：计划延迟以半 tick 为单位（dealIndex*DEAL_INTERVAL_TICKS 的半粒度），隔 1 调度 1，实现两倍速
		SCHEDULED.computeIfAbsent(key, k -> new ArrayList<>()).add(new Scheduled(tickCounter + delay / 2, run));
	}

	/**
	 * 应用一包状态：仅选中变化 → 移动对应实体；发牌动画 → 清场逐张生成；其余 → 增量 diff 更新。
	 * 牌谱回放期间（{@link #beginReplay} 起至 {@link #endReplay}）屏蔽该桌实时同步，
	 * 改由回放侧用 {@link #applyReplay} 驱动同一渲染管线；结束时回放最后一包真实状态。
	 */
	public static void apply(MahjongTablePayload.ViewMessage msg) {
		if (replayOrigin != null && replayOrigin == msg.originPacked())
			return; // 回放中：实时包丢弃（结束后用最后真实包恢复）
		applyInternal(msg, false);
	}

	/** 回放专用：合成快照走同一渲染管线（不更新真实缓存、选中优化跳过） */
	public static void applyReplay(MahjongTablePayload.ViewMessage msg) {
		if (replayOrigin != null && replayOrigin == msg.originPacked())
			applyInternal(msg, true);
	}

	/** 进入回放模式：此后该桌的实时 ViewMessage 被忽略 */
	public static void beginReplay(long originPacked) {
		replayOrigin = originPacked;
	}

	/** 退出回放模式：用回放前最后收到的真实包恢复桌面 */
	public static void endReplay() {
		Long key = replayOrigin;
		replayOrigin = null;
		if (key == null)
			return;
		TableRender table = TABLES.get(key);
		if (table != null && table.last != null) {
			applyInternal(table.last, false);
		} else {
			ClientLevel level = Minecraft.getInstance().level;
			if (level != null && table != null)
				discardAll(level, table);
			discardAvatars(key);
			TABLES.remove(key);
		}
	}

	private static Long replayOrigin;

	private static void applyInternal(MahjongTablePayload.ViewMessage msg, boolean synthetic) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return;
		long key = msg.originPacked();
		TableRender table = TABLES.get(key);
		// 仅选中状态变化（升降）且无在途动画：移动对应实体，不重建
		if (!synthetic && table != null && table.last != null && table.last.dealAnim() == 0
				&& !SCHEDULED.containsKey(key) && sameExceptSelection(table.last, msg)) {
			moveSelection(level, table, msg);
			table.last = msg;
			return;
		}
		SCHEDULED.remove(key); // 重建即取消在途动画任务
		if (msg.phase() == RichiTableState.PHASE_WAITING) {
			// 无对局（局目间重置/大厅等待）：清除该桌客户端牌面实体；
			// 形象仍走 updateAvatars 增删建（预约即渲染，随机模式 = 盔甲架占位）
			if (table != null) {
				discardAll(level, table);
				TABLES.remove(key);
			}
			boolean anyForm = false;
			for (String f : msg.avatarForms())
				if (f != null && !f.isEmpty())
					anyForm = true;
			if (!anyForm)
				discardAvatars(key);
			BlockPos waitingOrigin = BlockPos.of(key);
			if (level.getBlockState(waitingOrigin).getBlock() instanceof FengPanBlock)
				updateAvatars(level, key, waitingOrigin, TableLayout.of(level, waitingOrigin), msg);
			return;
		}
		if (table == null) {
			table = new TableRender();
			TABLES.put(key, table);
		}
		if (!synthetic)
			table.last = msg;
		BlockPos origin = BlockPos.of(key);
		if (!(level.getBlockState(origin).getBlock() instanceof FengPanBlock)) {
			discardAll(level, table); // 区块未加载/桌子不存在：先清场，等下一包
			discardAvatars(key);
			return;
		}
		TableLayout.Geom geom = TableLayout.of(level, origin);
		// 状态镜像（plan 只依赖摆位相关字段）
		RichiTableState st = new RichiTableState();
		for (int i = 0; i < 4; i++) {
			st.hands[i] = orEmpty(msg.hands()[i]);
			st.rivers[i] = orEmpty(msg.rivers()[i]);
			st.melds[i] = orEmpty(msg.melds()[i]);
			st.points[i] = msg.points()[i];
			st.riichiRiverIdx[i] = msg.riichiRiverIdx()[i];
			st.riichiSticks[i] = msg.riichiSticks()[i];
			st.handsExposed[i] = msg.handsExposed()[i];
		}
		st.doraWall = orEmpty(msg.doraWall());
		st.revealedIndicators = msg.revealedIndicators();
		st.round = msg.round();
		st.roundWind = msg.roundWind();
		st.honba = msg.honba();
		st.turnSeat = msg.turnSeat();
		st.selectedSeat = msg.selectedSeat();
		st.selectedIndex = msg.selectedIndex();
		st.drawnSeat = msg.drawnSeat();
		st.phase = msg.phase();
		// 观战摊手（客户端配置）：观战者视角把立牌手牌全部改为面朝上平摊显示
		if (mcr.murmol.MurmolModConfig.SPECTATOR_OPEN_HANDS.get() && msg.viewerSeat() < 0)
			for (int s = 0; s < 4; s++)
				if (st.handsExposed[s] == RichiTableState.HAND_STAND)
					st.handsExposed[s] = RichiTableState.HAND_FACE_UP;
		// 手牌码：可见家解析记法；隐藏家按 hidden 数量渲染未知牌背（-1 = 无牌面组件）；
		// 盖牌（HAND_FACE_DOWN，流局未听/九种九牌途中流局）一律渲染牌背——自己的手牌也不显示牌面
		List<List<Integer>> handCodes = new ArrayList<>(4);
		for (int s = 0; s < 4; s++) {
			int count;
			if (msg.hidden()[s] > 0) {
				count = msg.hidden()[s];
			} else {
				try {
					count = MahjongTileNotation.parse(orEmpty(msg.hands()[s])).size();
				} catch (RuntimeException e) {
					count = 0;
				}
			}
			if (msg.handsExposed()[s] == RichiTableState.HAND_FACE_DOWN) {
				List<Integer> backs = new ArrayList<>();
				for (int k = 0; k < count; k++)
					backs.add(-1);
				handCodes.add(backs);
			} else if (msg.hidden()[s] > 0) {
				List<Integer> backs = new ArrayList<>();
				for (int k = 0; k < msg.hidden()[s]; k++)
					backs.add(-1);
				handCodes.add(backs);
			} else {
				try {
					handCodes.add(MahjongTileNotation.parse(orEmpty(msg.hands()[s])));
				} catch (RuntimeException e) {
					handCodes.add(List.of());
				}
			}
		}
		List<Slot> slots = slotPlan(TableLayout.plan(geom, st, handCodes, msg.dealAnim() > 0), msg);
		if (msg.dealAnim() > 0) {
			// 发牌动画：整桌清场后按 delay 逐张生成
			discardAll(level, table);
			for (Slot slot : slots)
				spawnSlot(level, table, key, geom, slot, true, true);
			spawnInfo(level, table, geom, st, msg);
			String banner = orEmpty(msg.bannerText());
			if (!banner.isEmpty())
				spawnKeyed(level, table, "result", banner,
						DisplayEntityNbt.textDisplay(level.registryAccess(), "richi_result", banner),
						geom.center().add(0, 1.2, 0), 0);
		} else {
			diffApply(level, table, geom, st, msg, slots);
		}
		// AI 座位假玩家（回放合成包不驱动；回放结束后由最后真实包恢复）
		if (!synthetic)
			updateAvatars(level, key, origin, geom, msg);
	}

	// ==================================================================
	// AI 座位假玩家（客户端-only RemotePlayer，替代旧 MahjongAvatarEntity）
	// ==================================================================

	/**
	 * 按同步包增删建本桌各 AI 座位的假玩家：avatarForms[seat] 空 = 无形象（移除）；
	 * 形态/档案变化或实体丢失 → 重建。每次调用把隐形载具钉在座位站位（面朝桌心），
	 * 玩家位置由原版骑乘定位（rideTick → positionRider 挂点跟随载具）驱动，不直接钉玩家坐标。
	 */
	private static void updateAvatars(ClientLevel level, long key, BlockPos origin, TableLayout.Geom geom,
			MahjongTablePayload.ViewMessage msg) {
		Map<Integer, Avatar> avatars = AVATARS.computeIfAbsent(key, k -> new HashMap<>());
		Map<Integer, net.minecraft.world.entity.decoration.ArmorStand> stands = STANDS.computeIfAbsent(key,
				k -> new HashMap<>());
		Vec3 center = geom.center();
		boolean waiting = msg.phase() == RichiTableState.PHASE_WAITING;
		for (int seat = 0; seat < 4; seat++) {
			String form = orEmpty(msg.avatarForms()[seat]);
			Avatar cur = avatars.get(seat);
			if (form.isEmpty()) {
				if (cur != null) {
					avatars.remove(seat);
					removeAvatar(cur);
				}
				removeStand(stands, seat);
				continue;
			}
			// NPC 参战座位（Murmol）：真实实体在服务端钉位渲染，客户端不建假玩家
			if (form.equals("npc")) {
				if (cur != null) {
					avatars.remove(seat);
					removeAvatar(cur);
				}
				removeStand(stands, seat);
				continue;
			}
			// 随机模式等待期占位：桌边随机位置立一个可见盔甲架（不揭示形态）
			if (form.equals("stand")) {
				if (cur != null) {
					avatars.remove(seat);
					removeAvatar(cur);
				}
				var stand = stands.get(seat);
				if (stand == null || stand.isRemoved()) {
					double ang = level.random.nextDouble() * Math.PI * 2;
					double r = 1.95 + level.random.nextDouble() * 1.4; // 随机站位向内收 0.25
					Vec3 p = center.add(Math.cos(ang) * r, 0, Math.sin(ang) * r);
					float sy = (float) Math.toDegrees(Math.atan2(-(center.x - p.x), center.z - p.z));
					var s = new net.minecraft.world.entity.decoration.ArmorStand(
							net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
					s.setId(--nextAvatarEntityId); // 客户端唯一负数 id
					s.moveTo(p.x, origin.getY(), p.z, sy, 0.0f);
					s.setOldPosAndRot();
					s.setSilent(true);
					s.noPhysics = true;
					level.addEntity(s);
					stands.put(seat, s);
				}
				continue;
			}
			removeStand(stands, seat);
			Vec3 pos = waiting
					// 等待阶段：预约后即出现，站在桌边随机位置（创建时定一次，不随同步重摆）；同座位标记向内收 0.25
				? center.add(level.random.nextDouble() * 3.5 - 1.75, 0,
						level.random.nextDouble() * 3.5 - 1.75)
					: center.add(geom.outs()[seat].scale(TableLayout.SEAT_DISTANCE - 0.25)); // AI 形象比座位标记更靠内 0.25 格
			float yaw = (float) Math.toDegrees(Math.atan2(-(center.x - pos.x), center.z - pos.z));
			UUID uuid = profileUuidOf(key, seat, msg);
			// 显示名：human 形象用随机人类名；其余形态直接用形态名称（av_* 语言键），不再显示 AI·座位(AI:流派)
			String name = orEmpty(msg.avatarNames()[seat]);
			if (name.isEmpty())
				name = Component.translatable("gui.richi.lobby.av_" + form).getString();
			if (cur == null || cur.player().isRemoved() || !cur.form().equals(form)
					|| !cur.profileUuid().equals(uuid)) {
				if (cur != null) {
					avatars.remove(seat);
					removeAvatar(cur);
				}
				cur = createAvatar(level, form, uuid, name, pos, yaw);
				if (cur != null)
					avatars.put(seat, cur);
			}
			// 等待阶段不钉位（保持创建时的随机站位）；开局后钉到座位站位
			if (cur != null && !waiting)
				pinAvatar(origin.getY(), pos, yaw, cur);
		}
		if (avatars.isEmpty())
			AVATARS.remove(key);
	}

	/** 移除某座位的盔甲架占位 */
	private static void removeStand(Map<Integer, net.minecraft.world.entity.decoration.ArmorStand> stands,
			int seat) {
		var s = stands.remove(seat);
		if (s != null)
			s.discard();
	}

	/**
	 * 创建客户端-only 假玩家：注入 PlayerInfo（渲染/皮肤查询）→ 本地写形态附件 → 加入 ClientLevel。
	 * 玩家 tick 置空（无物理/无 AI），位置完全由原版骑乘定位驱动（rideTick 挂点跟随载具），
	 * 与载具抬升天然一致，杜绝位置打架造成的沉地/弹跳。
	 * 坐姿用原版骑乘机制：挂一个隐形 marker 盔甲架作载具，isPassenger → 原版 riding 姿势（坐矿车同款）。
	 */
	private static Avatar createAvatar(ClientLevel level, String form, UUID uuid, String name,
			Vec3 pos, float yaw) {
		GameProfile profile = new GameProfile(uuid, name);
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() != null)
			mc.getConnection().playerInfoMap.put(uuid, new PlayerInfo(profile, false));
		var seat = new net.minecraft.world.entity.decoration.ArmorStand(
				net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
		seat.setId(--nextAvatarEntityId); // 客户端唯一负数 id
		seat.setInvisible(true);
		byte flags = seat.getEntityData().get(net.minecraft.world.entity.decoration.ArmorStand.DATA_CLIENT_FLAGS);
		seat.getEntityData().set(net.minecraft.world.entity.decoration.ArmorStand.DATA_CLIENT_FLAGS,
				(byte) (flags | 0x10)); // marker 位：无碰撞箱不渲染
		seat.setSilent(true);
		seat.noPhysics = true;
		seat.moveTo(pos.x, pos.y + seatLift(form), pos.z, yaw, 0.0f);
		seat.setOldPosAndRot();
		level.addEntity(seat);
		RemotePlayer player = new RemotePlayer(level, profile) {
			@Override
			public void tick() {
			}

			@Override
			public net.minecraft.client.resources.PlayerSkin getSkin() {
				// Murmol：本地内置皮肤，不拉正版档案
				if ("Murmol".equals(name))
					return MURMOL_SKIN;
				return super.getSkin();
			}
		};
		player.setId(--nextAvatarEntityId); // 客户端唯一负数 id
		player.noPhysics = true;
		player.startRiding(seat, true);
		// 出生即在载具挂点，避免首帧闪现原点；此后位置由 rideTick 维护
		player.setPos(pos.x, pos.y + seatLift(form), pos.z);
		player.setOldPosAndRot();
		// 形态写入本地玩家附件（不 sync）：feral 形态走 FeralFormRenderer 玩家渲染路径，
		// villager 用约定哨兵 id（VillagerAvatarHandler 拦截渲染），human = 原版玩家 + 档案皮肤
		player.getData(MurmolModVariables.PLAYER_VARIABLES).feralFormId =
				"villager".equals(form) ? VillagerAvatarHandler.FORM_ID : form;
		level.addEntity(player);
		return new Avatar(player, seat, form, uuid);
	}

	/**
	 * 钉住隐形载具在座位站位（外侧 3.5 格、面朝桌心，抬升 SEAT_LIFT），玩家朝向同步钉死。
	 * 玩家坐标不在此设置——由 rideTick 挂点跟随载具，两边不再打架。
	 */
	private static void pinAvatar(double baseY, Vec3 pos, float yaw, Avatar avatar) {
		RemotePlayer player = avatar.player();
		if (player.isRemoved())
			return;
		net.minecraft.world.entity.decoration.ArmorStand st = avatar.seat();
		st.moveTo(pos.x, baseY + seatLift(avatar.form()), pos.z, yaw, 0.0f);
		st.setOldPosAndRot();
		st.yBodyRot = yaw;
		st.yBodyRotO = yaw;
		// 玩家朝向：renderHumanoid 在 isPassenger 时身体 yaw 取自载具，这里补头部朝向
		player.yBodyRot = yaw;
		player.yBodyRotO = yaw;
		player.yHeadRot = yaw;
		player.yHeadRotO = yaw;
	}

	/** 删除假玩家实体（含隐形骑乘载具）；无其他假玩家引用同一档案时清理注入的 PlayerInfo（防标签页/皮肤缓存残留） */
	private static void removeAvatar(Avatar avatar) {
		avatar.seat().discard();
		avatar.player().discard();
		boolean inUse = AVATARS.values().stream().flatMap(m -> m.values().stream())
				.anyMatch(a -> a.profileUuid().equals(avatar.profileUuid()));
		if (!inUse) {
			var connection = Minecraft.getInstance().getConnection();
			if (connection != null)
				connection.playerInfoMap.remove(avatar.profileUuid());
		}
	}

	/** 删除本桌全部 AI 假玩家（等待阶段/桌子销毁/回放恢复失败） */
	private static void discardAvatars(long key) {
		Map<Integer, Avatar> avatars = AVATARS.remove(key);
		if (avatars != null)
			for (Avatar avatar : avatars.values())
				removeAvatar(avatar);
		Map<Integer, net.minecraft.world.entity.decoration.ArmorStand> stands = STANDS.remove(key);
		if (stands != null)
			for (var s : stands.values())
				s.discard();
	}

	/** 假玩家档案 UUID：human 有皮肤 UUID 用之（拉取正版皮肤），否则按桌+座位派生稳定 UUID */
	private static UUID profileUuidOf(long key, int seat, MahjongTablePayload.ViewMessage msg) {
		String skin = orEmpty(msg.avatarSkins()[seat]);
		if (!skin.isEmpty()) {
			try {
				return UUID.fromString(skin);
			} catch (IllegalArgumentException ignored) {
			}
		}
		return UUID.nameUUIDFromBytes(("murmol_avatar:" + key + ":" + seat)
				.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	/**
	 * 为整桌摆位项建立稳定 key 与外观签名（纯客户端索引，服务端数据/协议不变）：
	 * 手牌 hand:座位:牌面:同面内序（摸打/重排只换位置不换 key → 移动走插值）；
	 * 牌河 river:座位:下标；副露 meld:座位:序（组追加式，声明牌随组追加）；点棒 stick:座位:序；
	 * 宝牌 dora:叠:0底/1顶；座位标签 seat:座位。签名 = kind:牌面:正反面:立平（item）/ 文本（text）。
	 */
	private static List<Slot> slotPlan(List<TableLayout.Placement> plan, MahjongTablePayload.ViewMessage msg) {
		List<Slot> out = new ArrayList<>(plan.size());
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] handOcc = new HashMap[4];
		int[] meldSeq = new int[4];
		Map<Integer, Integer> doraOcc = new HashMap<>();
		for (TableLayout.Placement p : plan) {
			switch (p.kind()) {
				case TableLayout.KIND_SEAT -> {
					// 局目轮换（轮庄）后世界位置的东南西北随之改变：庄家位 = (round-1)%4，
					// 各位置风 = SEAT_NAMES[floorMod(座位 - 庄家位, 4)]（与服务端 broadcastRoundStart 同式）
					int dealer = (msg.round() - 1) % 4;
					String wind = msg.round() > 0
							? TableLayout.SEAT_NAMES[Math.floorMod(p.seat() - dealer, 4)]
							: TableLayout.SEAT_NAMES[p.seat()];
					String text = (msg.turnSeat() == p.seat() ? "▶ " : "") + wind
							+ "\n" + msg.points()[p.seat()];
					out.add(new Slot(p, "seat:" + p.seat(), "T:" + text, text));
				}
				case TableLayout.KIND_HAND -> {
					Map<Integer, Integer> occ = handOcc[p.seat()];
					if (occ == null)
						occ = handOcc[p.seat()] = new HashMap<>();
					int n = occ.merge(p.code(), 1, Integer::sum);
					// 隐藏手牌 + 最近打牌为手切：牌背 occ 整体 +1 错位——牌行中一张牌背消失，
					// 刚摸的那张（分离位）平滑滑回牌行尾；否则永远表现为刚摸的牌消失（摸切观感）
					int shift = msg.hidden()[p.seat()] > 0 && msg.tedashi()[p.seat()] == 1 ? 1 : 0;
					out.add(new Slot(p, "hand:" + p.seat() + ":" + p.code() + ":" + (n - 1 + shift), itemSig(p), null));
				}
				case TableLayout.KIND_RIVER ->
					out.add(new Slot(p, "river:" + p.seat() + ":" + p.index(), itemSig(p), null));
				case TableLayout.KIND_MELD ->
					out.add(new Slot(p, "meld:" + p.seat() + ":" + meldSeq[p.seat()]++, itemSig(p), null));
				case TableLayout.KIND_STICK ->
					out.add(new Slot(p, "stick:" + p.seat() + ":" + p.index(), itemSig(p), null));
				default -> {
					// KIND_DORA：底张固定 0，上张（翻开与否只影响签名）固定 1
					int n = doraOcc.merge(p.index(), 1, Integer::sum);
					out.add(new Slot(p, "dora:" + p.index() + ":" + (n - 1), itemSig(p), null));
				}
			}
		}
		return out;
	}

	/** item 外观签名：类别 + 牌面 + 正反面 + 立牌/平躺（位置与朝向不参与，变化走实体插值） */
	private static String itemSig(TableLayout.Placement p) {
		return p.kind() + ":" + p.code() + ":" + p.faceUp() + ":" + (p.axis() == null);
	}

	/**
	 * 增量 diff：签名未变 → 复用（位置/朝向变化走 display 自带插值 lerpTo 平滑移动）；
	 * 签名变化/实体丢失 → 只重建该实体（同帧替换）；新 key → 生成；消失 key → 删除。
	 * 座位标签/中心信息/横幅为文本牌位，内容变化就地 load 更新。
	 */
	private static void diffApply(ClientLevel level, TableRender table, TableLayout.Geom geom,
			RichiTableState st, MahjongTablePayload.ViewMessage msg, List<Slot> slots) {
		Map<String, Entry> fresh = new HashMap<>();
		table.handIds.clear();
		for (Slot slot : slots) {
			TableLayout.Placement p = slot.p();
			if (TableLayout.KIND_SEAT.equals(p.kind())) {
				applyText(level, table, fresh, slot.slotKey(), p.pos(), "richi_seat_" + p.seat(), slot.text());
				continue;
			}
			Entry old = table.keyed.get(slot.slotKey());
			Entity e = old == null ? null : level.getEntity(old.entityId());
			if (e != null && e.isRemoved())
				e = null;
			if (e != null && !old.sig().equals(slot.sig())) {
				e.discard(); // 外观变化：只重建该实体（同帧替换，不闪烁）
				e = null;
			}
			if (e == null) {
				e = spawnSlot(level, table, 0, geom, slot, false, false);
			} else if (moved(e, p)) {
				// 仅位置/朝向变化：实体自带插值（teleport_duration）平滑过渡，不重建
				e.lerpTo(p.pos().x, p.pos().y, p.pos().z, p.yRot(), 0, 0);
			}
			if (e != null) {
				fresh.put(slot.slotKey(), new Entry(e.getId(), slot.sig()));
				if (TableLayout.KIND_HAND.equals(p.kind()) && p.index() >= 0)
					table.handIds.put((p.seat() << 8) | p.index(), e.getId());
			}
		}
		// 中心信息牌（文本变化就地更新）与结算横幅（空 = 消失 → 走下方删除）
		applyText(level, table, fresh, "info", geom.center().add(0, 2.0, 0), "richi_info", infoText(st, msg));
		String banner = orEmpty(msg.bannerText());
		if (!banner.isEmpty())
			applyText(level, table, fresh, "result", geom.center().add(0, 1.2, 0), "richi_result", banner);
		// 消失的牌位 → 删除实体
		for (Map.Entry<String, Entry> en : table.keyed.entrySet()) {
			if (fresh.containsKey(en.getKey()))
				continue;
			Entity e = level.getEntity(en.getValue().entityId());
			if (e != null && !e.isRemoved())
				e.discard();
		}
		table.keyed.clear();
		table.keyed.putAll(fresh);
	}

	/** 位置或朝向是否变化（决定是否走插值移动） */
	private static boolean moved(Entity e, TableLayout.Placement p) {
		return e.distanceToSqr(p.pos().x, p.pos().y, p.pos().z) > 1.0E-8
				|| Math.abs(Mth.wrapDegrees(e.getYRot() - p.yRot())) > 0.01F;
	}

	/**
	 * 纯文本牌位（座位标签/中心信息/横幅）：签名 = 文本内容。
	 * 实体健在且位置一致：文本不变 → 复用；文本变化 → 就地 load 更新 text NBT（不重建）。
	 * 实体丢失/位置不符 → 重建。结果登记进 fresh。
	 */
	private static void applyText(ClientLevel level, TableRender table, Map<String, Entry> fresh,
			String slotKey, Vec3 pos, String tag, String text) {
		Entry old = table.keyed.get(slotKey);
		Entity e = old == null ? null : level.getEntity(old.entityId());
		if (e instanceof TextDisplay td && !td.isRemoved()
				&& td.distanceToSqr(pos.x, pos.y, pos.z) <= 1.0E-8) {
			if (old.sig().equals(text)) {
				fresh.put(slotKey, old); // 完全不变：复用
				return;
			}
			// 文本变化：就地更新 NBT（Entity.load 需带 Pos 保持位置）
			td.load(withPose(DisplayEntityNbt.textDisplay(level.registryAccess(), tag, text),
					new Vec3(td.getX(), td.getY(), td.getZ()), td.getYRot()));
			fresh.put(slotKey, new Entry(td.getId(), text));
			return;
		}
		if (e != null && !e.isRemoved())
			e.discard();
		Entity ne = spawnLocal(level, DisplayEntityNbt.textDisplay(level.registryAccess(), tag, text), pos, 0);
		if (ne != null)
			fresh.put(slotKey, new Entry(ne.getId(), text));
	}

	/** text NBT 附带 Pos/Motion/Rotation（Entity.load 缺 Pos 会把实体归零） */
	private static CompoundTag withPose(CompoundTag nbt, Vec3 pos, float yRot) {
		ListTag posTag = new ListTag();
		posTag.add(DoubleTag.valueOf(pos.x));
		posTag.add(DoubleTag.valueOf(pos.y));
		posTag.add(DoubleTag.valueOf(pos.z));
		nbt.put("Pos", posTag);
		nbt.put("Motion", new ListTag());
		ListTag rotTag = new ListTag();
		rotTag.add(FloatTag.valueOf(yRot));
		rotTag.add(FloatTag.valueOf(0.0F));
		nbt.put("Rotation", rotTag);
		return nbt;
	}

	/**
	 * 生成单个摆位项：animate 时按延迟调度（发牌 stagger / 推倒级联），否则立即生成并返回实体。
	 * 推倒/盖牌级联：先立牌（登记过渡签名），delay+10 平躺——同 key 二次生成自动替换立牌。
	 */
	/** 发牌时逐张的落牌音效：深板岩放置声（客户端本地播放，音量压低） */
	private static void playDealSound(ClientLevel level, Vec3 pos) {
		level.playLocalSound(pos.x, pos.y, pos.z,
				net.minecraft.sounds.SoundEvents.DEEPSLATE_PLACE,
				net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 0.85f, false);
	}

	private static Entity spawnSlot(ClientLevel level, TableRender table, long tableKey, TableLayout.Geom geom,
			Slot slot, boolean animate, boolean deal) {
		TableLayout.Placement p = slot.p();
		Entity out = null;
		switch (p.kind()) {
			case TableLayout.KIND_SEAT -> {
				if (animate)
					schedule(tableKey, p.delay(), () -> spawnKeyed(level, table, slot.slotKey(), slot.text(),
							DisplayEntityNbt.textDisplay(level.registryAccess(), "richi_seat_" + p.seat(), slot.text()),
							p.pos(), 0));
				else
					out = spawnKeyed(level, table, slot.slotKey(), slot.text(),
							DisplayEntityNbt.textDisplay(level.registryAccess(), "richi_seat_" + p.seat(), slot.text()),
							p.pos(), 0);
			}
			case TableLayout.KIND_HAND -> {
				String tag = TableLayout.handTag(p.seat(), p.index());
				if (p.axis() == null) {
					// 立牌（隐藏座位为未知牌面 ItemStack，不带座位标记）
					if (animate)
						schedule(tableKey, p.delay(), () -> {
							if (deal)
								playDealSound(level, p.pos());
							recordHand(table, p,
									spawnKeyed(level, table, slot.slotKey(), slot.sig(), handNbt(p, true, tag, level),
											p.pos(), p.yRot()));
						});
					else
						out = recordHand(table, p, spawnKeyed(level, table, slot.slotKey(), slot.sig(),
								handNbt(p, true, tag, level), p.pos(), p.yRot()));
				} else if (animate) {
					// 局终推倒/盖牌：先立牌后平躺（级联），立牌登记过渡签名以便中途 diff 重建
					float standYaw = (float) Math.toDegrees(
							Math.atan2(-geom.outs()[p.seat()].x, geom.outs()[p.seat()].z));
					Vec3 standPos = TableLayout.standingEntityPos(p.base());
					schedule(tableKey, p.delay(), () -> recordHand(table, p, spawnKeyed(level, table, slot.slotKey(),
							"standing:" + slot.sig(), handNbt(p, true, tag, level), standPos, standYaw)));
					schedule(tableKey, p.delay() + 10, () -> recordHand(table, p, spawnKeyed(level, table,
							slot.slotKey(), slot.sig(), handNbt(p, false, null, level), p.pos(), p.yRot())));
				} else {
					out = recordHand(table, p, spawnKeyed(level, table, slot.slotKey(), slot.sig(),
							handNbt(p, false, null, level), p.pos(), p.yRot()));
				}
			}
			case TableLayout.KIND_RIVER, TableLayout.KIND_MELD, TableLayout.KIND_DORA -> {
				if (animate)
					schedule(tableKey, p.delay(), () -> {
						if (deal)
							playDealSound(level, p.pos());
						spawnKeyed(level, table, slot.slotKey(), slot.sig(),
								flatNbt(p, level), p.pos(), p.yRot());
					});
				else
					out = spawnKeyed(level, table, slot.slotKey(), slot.sig(), flatNbt(p, level), p.pos(), p.yRot());
			}
			default -> {
				// KIND_STICK：立直点棒
				if (animate)
					schedule(tableKey, p.delay(), () -> spawnKeyed(level, table, slot.slotKey(), slot.sig(),
							stickNbt(level), p.pos(), p.yRot()));
				else
					out = spawnKeyed(level, table, slot.slotKey(), slot.sig(), stickNbt(level), p.pos(), p.yRot());
			}
		}
		return out;
	}

	/** 手牌立牌/平躺 item_display NBT */
	private static CompoundTag handNbt(TableLayout.Placement p, boolean standing, String tag, ClientLevel level) {
		return DisplayEntityNbt.itemDisplay(MahjongTileItem.create(p.code()),
				standing ? DisplayEntityNbt.standingMatrix(TableLayout.TILE_SCALE)
						: DisplayEntityNbt.flatMatrix(TableLayout.TILE_SCALE, p.faceUp()),
				standing ? tag : null, null, level.registryAccess());
	}

	/** 平躺牌（牌河/副露/宝牌）item_display NBT */
	private static CompoundTag flatNbt(TableLayout.Placement p, ClientLevel level) {
		return DisplayEntityNbt.itemDisplay(MahjongTileItem.create(p.code()),
				DisplayEntityNbt.flatMatrix(TableLayout.TILE_SCALE, p.faceUp()), null, null, level.registryAccess());
	}

	/** 立直点棒 item_display NBT */
	private static CompoundTag stickNbt(ClientLevel level) {
		return DisplayEntityNbt.itemDisplay(DisplayEntityNbt.riichiStickStack(),
				DisplayEntityNbt.stickMatrix(TableLayout.TILE_SCALE), null, null, level.registryAccess());
	}

	/** 手牌(座位,下标) → 实体 id 记录（选中升降增量定位用） */
	private static Entity recordHand(TableRender table, TableLayout.Placement p, Entity e) {
		if (e != null && p.index() >= 0)
			table.handIds.put((p.seat() << 8) | p.index(), e.getId());
		return e;
	}

	/** 中心信息牌文案（照旧 updateTableInfo）：局目 / 当前宝牌 / 牌山枚数 */
	private static String infoText(RichiTableState st, MahjongTablePayload.ViewMessage msg) {
		var dora = st.doraCodes();
		int shown = Math.max(0, st.revealedIndicators);
		StringBuilder doraNames = new StringBuilder();
		for (int i = 0; i < shown && 2 * i + 1 < dora.size(); i++) {
			if (doraNames.length() > 0)
				doraNames.append(' ');
			doraNames.append(MahjongTileItem.create(
					RiichiWall.doraTileOf(dora.get(2 * i + 1))).getHoverName().getString());
		}
		String doraName = doraNames.length() > 0 ? doraNames.toString() : "无";
		return (st.roundWind == 0 ? "東" : "南") + st.round + "局　" + st.honba + "本场\n宝牌: " + doraName
				+ "\n牌山: " + msg.wallCount() + " 枚";
	}

	/** 中心信息牌（发牌动画路径直接生成） */
	private static void spawnInfo(ClientLevel level, TableRender table, TableLayout.Geom geom,
			RichiTableState st, MahjongTablePayload.ViewMessage msg) {
		String text = infoText(st, msg);
		spawnKeyed(level, table, "info", text,
				DisplayEntityNbt.textDisplay(level.registryAccess(), "richi_info", text),
				geom.center().add(0, 2.0, 0), 0);
	}

	/**
	 * 客户端本地生成实体：LevelWriter.addFreshEntity 在 ClientLevel 上是空操作（默认 return false，
	 * 仅 ServerLevel 重写），必须走 ClientLevel.addEntity（与原版 ClientPacketListener.handleAddEntity
	 * 同路径），实体才会进入渲染/tick 管理。
	 */
	private static Entity spawnLocal(ClientLevel level, CompoundTag nbt, Vec3 pos, float yRot) {
		return net.minecraft.world.entity.EntityType.loadEntityRecursive(nbt, level, entity -> {
			entity.moveTo(pos.x, pos.y, pos.z, yRot, 0);
			level.addEntity(entity);
			return entity;
		});
	}

	/** 按 key 生成并登记实体；同 key 旧实体先删除（签名变化重建 / 动画两段替换共用） */
	private static Entity spawnKeyed(ClientLevel level, TableRender table, String slotKey, String sig,
			CompoundTag nbt, Vec3 pos, float yRot) {
		discardKeyed(level, table, slotKey);
		Entity e = spawnLocal(level, nbt, pos, yRot);
		if (e != null)
			table.keyed.put(slotKey, new Entry(e.getId(), sig));
		return e;
	}

	/** 删除某 key 登记的实体并从清单剔除 */
	private static void discardKeyed(ClientLevel level, TableRender table, String slotKey) {
		Entry old = table.keyed.remove(slotKey);
		if (old == null)
			return;
		Entity e = level.getEntity(old.entityId());
		if (e != null && !e.isRemoved())
			e.discard();
	}

	/** 删除本桌全部自建实体（服务端不再有对应真实实体，直接 discard 本端实体） */
	private static void discardAll(ClientLevel level, TableRender table) {
		for (Entry en : table.keyed.values()) {
			Entity e = level.getEntity(en.entityId());
			if (e != null && !e.isRemoved())
				e.discard();
		}
		table.keyed.clear();
		table.handIds.clear();
	}

	/** 选中升降：先降旧的、再升新的（±SELECT_LIFT），只移动对应显示实体 */
	private static void moveSelection(ClientLevel level, TableRender table, MahjongTablePayload.ViewMessage msg) {
		shiftHand(level, table, table.last.selectedSeat(), table.last.selectedIndex(), -TableLayout.SELECT_LIFT);
		shiftHand(level, table, msg.selectedSeat(), msg.selectedIndex(), TableLayout.SELECT_LIFT);
	}

	/** 按 (座位, 手牌下标) 找实体并走插值移动（display 自带 pos-rot 插值平滑升降） */
	private static void shiftHand(ClientLevel level, TableRender table, int seat, int index, double dy) {
		if (seat < 0 || index < 0)
			return;
		Integer id = table.handIds.get((seat << 8) | index);
		if (id == null)
			return;
		Entity e = level.getEntity(id);
		if (e == null || e.isRemoved())
			return;
		e.lerpTo(e.getX(), e.getY() + dy, e.getZ(), e.getYRot(), e.getXRot(), 0);
	}

	/** 除 selectedSeat/Index 外全部渲染相关字段一致（record 含数组需逐项比较） */
	private static boolean sameExceptSelection(MahjongTablePayload.ViewMessage a, MahjongTablePayload.ViewMessage b) {
		return a.dealAnim() == b.dealAnim()
				&& Arrays.equals(a.hands(), b.hands())
				&& Arrays.equals(a.hidden(), b.hidden())
				&& Arrays.equals(a.rivers(), b.rivers())
				&& Arrays.equals(a.melds(), b.melds())
				&& Arrays.equals(a.riichiRiverIdx(), b.riichiRiverIdx())
				&& Arrays.equals(a.riichiSticks(), b.riichiSticks())
				&& Arrays.equals(a.handsExposed(), b.handsExposed())
				&& Arrays.equals(a.points(), b.points())
				&& Arrays.equals(a.names(), b.names())
				&& Arrays.equals(a.avatarForms(), b.avatarForms())
				&& Arrays.equals(a.avatarNames(), b.avatarNames())
				&& Arrays.equals(a.avatarSkins(), b.avatarSkins())
				&& orEmpty(a.doraWall()).equals(orEmpty(b.doraWall()))
				&& a.revealedIndicators() == b.revealedIndicators()
				&& a.wallCount() == b.wallCount()
				&& a.turnSeat() == b.turnSeat()
				&& a.round() == b.round()
				&& a.roundWind() == b.roundWind()
				&& a.honba() == b.honba()
				&& a.drawnSeat() == b.drawnSeat()
				&& a.phase() == b.phase()
				&& orEmpty(a.bannerText()).equals(orEmpty(b.bannerText()));
	}

	private static String orEmpty(String s) {
		return s == null ? "" : s;
	}
}
