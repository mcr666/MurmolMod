package mcr.richi.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import mcr.richi.game.RichiTableState;
import mcr.richi.game.RichiTableSync;
import mcr.richi.render.DisplayEntityNbt;
import mcr.richi.render.TableLayout;

/**
 * 风盘（麻将牌桌）：类原版床的 2x2 多方块，一次放置铺满 2x2（中心四个 0.5x0.5 雪层拼成 1 格桌面），
 * 放置前检测周边 4x4 区域（含桌子本体）是否为空，有阻挡则放置失败并提示。破坏任意部分整桌消失且只掉落一个物品。
 * tx/tz 为相对桌角原点（0,0）的偏移；原点为放置点，沿玩家水平朝向及其右侧延伸。
 * 渲染：服务端不再生成牌面实体（防抓包看牌），客户端按 RichiTableSync 过滤广播的状态包自建显示实体；
 * 服务端仅维护手牌 Interaction 交互代理（纯坐标判定，无牌面数据），AI 形象由客户端假玩家渲染（见 resolveAvatars）。
 */
public class FengPanBlock extends Block {
	/** 桌子边长（2x2） */
	public static final int SIZE = 2;
	/** 放置检测区域边长（4x4，含桌子本体及周边一圈） */
	public static final int CHECK_SIZE = 4;
	/** 放置朝向（桌沿玩家朝向及右侧延伸），类原版床 */
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	/** 相对原点的偏移（0..3），tx=FACING 方向，tz=FACING 右侧 */
	public static final IntegerProperty TX = IntegerProperty.create("tx", 0, SIZE - 1);
	public static final IntegerProperty TZ = IntegerProperty.create("tz", 0, SIZE - 1);
	/** 牌局是否进行中：进行中隐藏风盘方块模型（未开局时显示两仪占位模型） */
	public static final net.minecraft.world.level.block.state.properties.BooleanProperty GAME_ACTIVE =
			net.minecraft.world.level.block.state.properties.BooleanProperty.create("game_active");

	/** 倒计时叠加层：各玩家上次发送的剩余秒数（变化才发包） */
	private static final java.util.Map<java.util.UUID, String> LAST_COUNTDOWN = new java.util.HashMap<>();
	/** 选牌判定半径（格，命中点与手牌位置的水平距离上限） */
	private static final double SELECT_RADIUS = 0.2;

	public FengPanBlock(Properties properties) {
		super(properties);
		registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH).setValue(TX, 0).setValue(TZ, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TX, TZ, GAME_ACTIVE);
	}

	/** 放置检测：桌子 2x2 及周边一圈共 4x4 区域必须全部为空（空气/可替换方块），否则放置失败并提示 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction forward = context.getHorizontalDirection();
		Direction right = forward.getClockWise();
		Level level = context.getLevel();
		BlockPos origin = context.getClickedPos();
		BlockPos checkStart = origin.relative(forward.getOpposite()).relative(right.getOpposite());
		for (int i = 0; i < CHECK_SIZE; i++) {
			for (int j = 0; j < CHECK_SIZE; j++) {
				if (!level.getBlockState(checkStart.relative(forward, i).relative(right, j)).canBeReplaced()) {
					if (context.getPlayer() != null)
						context.getPlayer().displayClientMessage(
								Component.translatable("message.richi.fengpan_blocked"), true);
					return null; // 有阻挡，放置失败
				}
			}
		}
		return this.defaultBlockState().setValue(FACING, forward);
	}

	/** 类原版床：放置成功后补齐其余 3 个部分 */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) {
			Direction forward = state.getValue(FACING);
			Direction right = forward.getClockWise();
			for (int i = 0; i < SIZE; i++) {
				for (int j = 0; j < SIZE; j++) {
					if (i == 0 && j == 0)
						continue;
					BlockPos p = pos.relative(forward, i).relative(right, j);
					level.setBlock(p, this.defaultBlockState().setValue(FACING, forward).setValue(TX, i).setValue(TZ, j), Block.UPDATE_ALL);
				}
			}
		}
	}

	/** 由任意部分状态+位置还原桌角原点（tx/tz 世界方向取决于 FACING，必须按朝向反推） */
	public static BlockPos getOrigin(BlockState state, BlockPos pos) {
		Direction forward = state.getValue(FACING);
		Direction right = forward.getClockWise();
		return pos.relative(forward.getOpposite(), state.getValue(TX))
				.relative(right.getOpposite(), state.getValue(TZ));
	}

	/** 桌面几何中心（2x2 区域中点，f/r 可为负方向），y 取桌面顶面 */
	public static Vec3 getTableCenter(BlockState state, BlockPos pos) {
		BlockPos origin = getOrigin(state, pos);
		Direction forward = state.getValue(FACING);
		Direction right = forward.getClockWise();
		return new Vec3(
				origin.getX() + (forward.getStepX() + right.getStepX() + 1) / 2.0,
				origin.getY() + 0.0625,
				origin.getZ() + (forward.getStepZ() + right.getStepZ() + 1) / 2.0);
	}

	/** 风盘方块模型渲染与否由 blockstate 的 game_active 决定（未开局=两仪占位模型，开局=空模型隐藏） */
	@Override
	protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
		return net.minecraft.world.level.block.RenderShape.MODEL;
	}

	/** 切换整桌 4 块的 game_active（牌局开始/结束时调用，同步到客户端决定模型显隐） */
	public static void setGameActive(net.minecraft.server.level.ServerLevel level, BlockPos origin, boolean active) {
		BlockState s = level.getBlockState(origin);
		if (!(s.getBlock() instanceof FengPanBlock) || s.getValue(GAME_ACTIVE) == active)
			return;
		Direction forward = s.getValue(FACING);
		Direction right = forward.getClockWise();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				BlockPos p = origin.relative(forward, i).relative(right, j);
				level.setBlock(p, level.getBlockState(p).setValue(GAME_ACTIVE, active), Block.UPDATE_ALL);
			}
		}
	}

	/** 准心触及范围：各部分的选中框均覆盖整张桌子（2x2 平面、高 1 格），任意部分都能选中全桌 */
	@Override
	protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, BlockGetter level,
			BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
		Direction forward = state.getValue(FACING);
		Direction right = forward.getClockWise();
		int tx = state.getValue(TX);
		int tz = state.getValue(TZ);
		// 整桌范围（本部分局部坐标）：沿 forward 从 -tx 到 SIZE-1-tx，沿 right 从 -tz 到 SIZE-1-tz
		double x1 = 0.5 + forward.getStepX() * -tx + right.getStepX() * -tz;
		double z1 = 0.5 + forward.getStepZ() * -tx + right.getStepZ() * -tz;
		double x2 = 0.5 + forward.getStepX() * (SIZE - 1 - tx) + right.getStepX() * (SIZE - 1 - tz);
		double z2 = 0.5 + forward.getStepZ() * (SIZE - 1 - tx) + right.getStepZ() * (SIZE - 1 - tz);
		return net.minecraft.world.phys.shapes.Shapes.box(
				Math.min(x1, x2), 0, Math.min(z1, z2),
				Math.max(x1, x2), 1.0, Math.max(z1, z2));
	}

	/**
	 * 碰撞箱：完全无碰撞（风盘为纯标记，不阻挡行走/实体）。
	 */
	@Override
	protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, BlockGetter level,
			BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
		return net.minecraft.world.phys.shapes.Shapes.empty();
	}

	/** 结构完整性：水平相邻部分缺失时连锁消失（不掉落）。不检查下方支撑（悬空保留）。 */
	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
			LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		Direction forward = state.getValue(FACING);
		Direction right = forward.getClockWise();
		// 液体（水/岩浆等）不算缺失：防止水流冲毁风盘触发连锁拆除
		if (neighborState.getFluidState() != null && !neighborState.getFluidState().isEmpty())
			return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
		int di = direction == forward ? 1 : direction == forward.getOpposite() ? -1 : 0;
		int dj = direction == right ? 1 : direction == right.getOpposite() ? -1 : 0;
		if (di == 0 && dj == 0)
			return super.updateShape(state, direction, neighborState, level, pos, neighborPos); // 上下/垂直方向不参与链式检查
		int nx = state.getValue(TX) + di;
		int nz = state.getValue(TZ) + dj;
		if (nx >= 0 && nx < SIZE && nz >= 0 && nz < SIZE && neighborState.getBlock() != this) {
			// 连锁拆除时一并清掉桌面上的牌
			if (level instanceof Level lvl && !lvl.isClientSide)
				killTableDisplays(lvl, getOrigin(state, pos), forward, right);
			return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	/** 类原版床：破坏任意部分，整桌消失且只掉落本次破坏的这个物品；桌面上的牌一并消失 */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide) {
			BlockPos origin = getOrigin(state, pos);
			Direction forward = state.getValue(FACING);
			killTableDisplays(level, origin, forward, forward.getClockWise());
			Direction right = forward.getClockWise();
			for (int i = 0; i < SIZE; i++) {
				for (int j = 0; j < SIZE; j++) {
					if (i == state.getValue(TX) && j == state.getValue(TZ))
						continue;
					level.removeBlock(pos.relative(forward, i - state.getValue(TX)).relative(right, j - state.getValue(TZ)), false);
				}
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	/** 移除桌面范围内的所有牌显示实体与座位标记，并清除服务端牌局状态 */
	private static void killTableDisplays(Level level, BlockPos origin, Direction forward, Direction right) {
		mcr.richi.game.RichiTableManager.remove(origin);
		clearDisplays(level, origin, forward, right);
		// 牌局结束：恢复风盘占位模型（仅服务端 Level 有效）
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			setGameActive(serverLevel, origin, false);
			// 通知客户端清掉牌面实体与 AI 假玩家/盔甲架（防拆桌后上一局角色残留原地）
			mcr.richi.game.RichiTableSync.broadcastGone(serverLevel, origin);
		}
	}

	/** 清空桌面显示实体与交互代理（保留牌局状态）；按原点方块朝向推盒 */
	public static void clearDisplays(Level level, BlockPos origin) {
		if (level.getBlockState(origin).getBlock() instanceof FengPanBlock) {
			BlockState s = level.getBlockState(origin);
			Direction forward = s.getValue(FACING);
			clearDisplays(level, origin, forward, forward.getClockWise());
		}
	}

	private static void clearDisplays(Level level, BlockPos origin, Direction forward, Direction right) {
		BlockPos farCorner = origin.relative(forward, SIZE - 1).relative(right, SIZE - 1);
		AABB tableBox = AABB.encapsulatingFullBlocks(origin, farCorner).expandTowards(0, 1, 0)
				.expandTowards(5, 4, 5).expandTowards(-5, 0, -5);
		for (Entity e : level.getEntitiesOfClass(Entity.class, tableBox,
				e -> e.getType() == EntityType.ITEM_DISPLAY || e.getType() == EntityType.TEXT_DISPLAY
						|| e.getType() == EntityType.INTERACTION))
			e.discard();
	}

	/** 按当前牌局状态立即重建：清交互代理 + 重新生成 + 状态广播（状态变化/refresh 指令用，无发牌动画） */
	public static void rerenderTable(net.minecraft.server.level.ServerLevel level, BlockPos origin) {
		RichiTableState st = mcr.richi.game.RichiTableManager.get(origin);
		if (st == null || !(level.getBlockState(origin).getBlock() instanceof FengPanBlock))
			return;
		mcr.richi.MahjongTicker.cancel(origin); // 防止动画残留任务重复生成代理
		clearDisplays(level, origin);
		spawnHandInteractors(level, origin, st, false);
		RichiTableSync.broadcast(level, origin);
	}

	/** 清掉桌区（含外扩范围）全部显示/交互实体：开局前自动清桌用（风盘被挖后仍可按坐标兜底清理） */
	public static void clearDisplaysAt(net.minecraft.server.level.ServerLevel level, BlockPos origin) {
		clearDisplays(level, origin);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!level.isClientSide) {
			// 桌角原点按朝向反推；确保状态存在（空大厅等待排队）
			BlockPos origin = getOrigin(state, pos);
			var serverLevel = (net.minecraft.server.level.ServerLevel) level;
			mcr.richi.game.RichiTableManager.getOrCreate(serverLevel, origin);
			// 右击（无论是否潜行）：打开管理界面（GUI 排队/退出/安排 AI/设置/开始对局）
			mcr.richi.game.MahjongLobby.openLobby((net.minecraft.server.level.ServerPlayer) player, origin);
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * 选牌/打出（服务端，右键手牌交互实体触发）：实体位置最近的手牌未选中则选中
	 * （广播后客户端抬升 SELECT_LIFT）；已选中则打出——手牌该牌自动理牌后移到牌河末尾。
	 */
	public static void handleHandInteract(net.minecraft.server.level.ServerLevel level, BlockPos origin,
			net.minecraft.world.entity.Interaction interactor, Player player) {
		RichiTableState st = mcr.richi.game.RichiTableManager.get(origin);
		if (st == null || !(level.getBlockState(origin).getBlock() instanceof FengPanBlock))
			return;
		Vec3 hit = interactor.position();
		TableLayout.Geom geom = TableLayout.of(level, origin);
		int seat = -1, index = -1;
		double best = SELECT_RADIUS * SELECT_RADIUS;
		for (int h = 0; h < 4; h++) {
			java.util.List<Integer> hand = st.handCodes(h);
			for (int k = 0; k < hand.size(); k++) {
				// 与渲染一致：刚摸的牌（drawnSeat 最右一张）落在隔开一个牌位的位置
				Vec3 t = TableLayout.handTarget(geom, h, k,
						h == st.drawnSeat && k == hand.size() - 1);
				double dx = hit.x - t.x, dz = hit.z - t.z;
				double d = dx * dx + dz * dz;
				if (d < best) {
					best = d;
					seat = h;
					index = k;
				}
			}
		}
		if (seat < 0)
			return;
		// 对局状态机接管：本家回合选中/打出，回合外提示（无对局时无手牌可点）
		var game = mcr.richi.game.RichiTableManager.getGame(origin);
		if (game != null)
			game.onTileClick(player, seat, index);
	}

	/**
	 * 左键自己手牌（交互代理被攻击）：当前回合家 = 摸切（直接打最右一张）；
	 * 回合外 = 过牌（预留：跳过吃/碰/杠）。
	 */
	public static void handleHandAttack(net.minecraft.server.level.ServerPlayer player, BlockPos origin,
			RichiTableState st, int seat) {
		var game = mcr.richi.game.RichiTableManager.getGame(origin);
		if (game != null)
			game.onTileAttack(player, seat); // 左键：摸切 / 取消立直 / 跳过鸣牌
		else
			player.displayClientMessage(Component.translatable("message.richi.skip_call"), true);
	}

	/** 向本桌所有已入座（在线）玩家发动作栏消息（超时自动摸切等无操作者场合使用） */
	public static void messageTable(net.minecraft.server.level.ServerLevel level,
			RichiTableState st, String key, Object... args) {
		for (int seat = 0; seat < 4; seat++) {
			var p = st.playerOf(level, seat);
			if (p != null)
				p.displayClientMessage(Component.translatable(key, args), true);
		}
	}

	/** 桌面几何中心（按当前方块朝向；大厅队列文本/结算横幅定位用） */
	public static Vec3 tableCenter(net.minecraft.server.level.ServerLevel level, BlockPos origin) {
		return TableLayout.of(level, origin).center();
	}

	/** 座位站位（外侧 3.5 格，与座位标记同轴）：开局随机入座后传送玩家用 */
	public static Vec3 seatStandPos(net.minecraft.server.level.ServerLevel level, BlockPos origin, int seat) {
		TableLayout.Geom geom = TableLayout.of(level, origin);
		Vec3 p = geom.center().add(geom.outs()[seat].scale(TableLayout.SEAT_DISTANCE));
		return new Vec3(p.x, origin.getY(), p.z);
	}

	/**
	 * 对局未开始（等待/终局保留）：4x4 正方形平面边缘少量 fengpan_edge 静止粒子（桌边长 4 格的边框）。
	 * overrideLimiter=true —— force 粒子，不受原版"视频设置→粒子数量"影响（最少也会显示），无需 mixin。
	 * 每 0.5s 由 MahjongTicker 调用一轮，单轮 8 个点沿边框推进 + 轻微抖动（粒子本体静止不动）。
	 */
	public static void spawnIdleParticles(net.minecraft.server.level.ServerLevel level, BlockPos origin) {
		if (!(level.getBlockState(origin).getBlock() instanceof FengPanBlock))
			return;
		mcr.richi.game.RichiTableState st = mcr.richi.game.RichiTableManager.get(origin);
		if (st == null || st.phase == mcr.richi.game.RichiTableState.PHASE_PLAYING)
			return;
		Vec3 c = tableCenter(level, origin);
		net.minecraft.world.phys.AABB box = net.minecraft.world.phys.AABB
				.encapsulatingFullBlocks(origin, origin).inflate(24, 24, 24);
		var players = level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, box);
		if (players.isEmpty())
			return;
		double y = c.y + 0.05;
		double half = 2.0; // 4x4 边框半边长
		double u0 = (level.getGameTime() / 10 % 4) * 1.0; // 每轮沿周长推进 1 格
		var particle = mcr.murmol.init.MurmolModParticleTypes.FENG_PAN_EDGE.get();
		for (int i = 0; i < 8; i++) {
			double u = (u0 + i * 2.0) % 16.0; // 周长 16 参数化，8 点间距 2 格
			double x, z;
			if (u < 4) { x = c.x - half + u; z = c.z - half; }
			else if (u < 8) { x = c.x + half; z = c.z - half + (u - 4); }
			else if (u < 12) { x = c.x + half - (u - 8); z = c.z + half; }
			else { x = c.x - half; z = c.z + half - (u - 12); }
			x += (level.random.nextDouble() - 0.5) * 0.15;
			z += (level.random.nextDouble() - 0.5) * 0.15;
			for (var p : players)
				level.sendParticles(p, particle, true, x, y, z, 1, 0, 0, 0, 0);
		}
	}

	/** 立直宣言牌特效：总共生成两个 riichi_burst 粒子圈（内圈 0.2 / 外圈 0.4，各 8 点） */
	public static void spawnRiichiBurst(net.minecraft.server.level.ServerLevel level, BlockPos origin, Vec3 center) {
		double[] radii = { 0.2, 0.4 };
		int points = 8;
		for (double radius : radii) {
			for (int i = 0; i < points; i++) {
				double a = Math.PI * 2 * i / points;
				level.sendParticles(mcr.murmol.init.MurmolModParticleTypes.RIICHI_BURST.get(),
						center.x + Math.cos(a) * radius,
						center.y + 0.12,
						center.z + Math.sin(a) * radius,
						1, 0, 0, 0, 0);
			}
		}
	}

	// ==================================================================
	// AI 形象（客户端假玩家：服务端只解析形态/名字/流派并存入状态，随桌面同步包下发）
	// ==================================================================

	/** 随机形态池（有独立烘焙模型且非人类的形态；形态同时决定 AI 流派） */
	private static final String[] AVATAR_FORMS = { "luohong", "chen_huang", "moss_beast", "silkmoth", "komainu",
			"wenyao", "ferocious" };

	/**
	 * 牌局开始（deal）时解析各 AI 座位形象并存入状态（不生成任何实体）：
	 * 模式：random=随机形态 / 指定形态 / human=随机名人类（能对应正版玩家则客户端拉取其皮肤）/ villager=村民。
	 * 结果（最终形态 id / 人类名 / 皮肤 UUID / 流派）由 RichiTableSync 随桌面快照下发，
	 * 客户端据此为每个 AI 座位创建 RemotePlayer 假玩家——座位轮转时随同步包天然搬移。
	 */
	public static void resolveAvatars(net.minecraft.server.level.ServerLevel level, RichiTableState st) {
		for (int seat = 0; seat < 4; seat++) {
			// 等待阶段按预约标记判定（aiSeat 开局才赋值），对局中按 aiSeat 判定
			boolean ai = st.phase == RichiTableState.PHASE_WAITING ? st.reservedAI[seat] : st.aiSeat[seat];
			if (!ai) {
				st.aiAvatarForms[seat] = "";
				st.aiAvatarNames[seat] = "";
				st.aiAvatarSkins[seat] = "";
				continue;
			}
			// 每座位独立模式；null/空 = 随机形态
			String seatMode = st.aiAvatarModes[seat];
			String mode = seatMode == null || seatMode.isEmpty() ? "random" : seatMode;
			// 等待阶段的"随机"不揭示具体形态：用 stand 占位（客户端渲染盔甲架），开局才确定
			String form = switch (mode) {
				case "random" -> st.phase == RichiTableState.PHASE_WAITING
						? "stand"
						: AVATAR_FORMS[level.random.nextInt(AVATAR_FORMS.length)];
				case "human", "villager" -> mode;
				default -> mode; // 指定形态
			};
			String name = "";
			String skin = "";
			if (form.equals("human")) {
				// 人类：随机名字；仅查在线玩家取皮肤 UUID（离线正版档案查询会阻塞主线程做 HTTP）
				name = randomHumanName(level);
				skin = skinUuidOf(level, name);
			}
			st.aiAvatarForms[seat] = form;
			st.aiAvatarNames[seat] = name;
			st.aiAvatarSkins[seat] = skin;
			// 流派随形象/名字确定（乘黄=一般流 落红=野猪流 苔叶兽=门清流 文鳐=御无双 狛犬=魂天流
			// 月蛾=闪电流；人类/村民随机 0-4，名为 Maocry55 的人类=鬼神境）
			st.aiFlows[seat] = mcr.richi.game.riichi.RiichiBot.flowOfAvatar(form, name,
					level.random::nextDouble);
		}
	}

	/** 人类皮肤 UUID 内存缓存（名字 → UUID，空串 = 无在线同名玩家） */
	private static final java.util.Map<String, String> SKIN_UUID = new java.util.concurrent.ConcurrentHashMap<>();

	/** 名字对应在线玩家的皮肤 UUID；仅查在线玩家列表（纯内存，不做正版档案 HTTP 解析） */
	private static String skinUuidOf(net.minecraft.server.level.ServerLevel level, String name) {
		return SKIN_UUID.computeIfAbsent(name, n -> {
			for (var p : level.getServer().getPlayerList().getPlayers())
				if (n.equals(p.getGameProfile().getName()))
					return p.getGameProfile().getId().toString();
			return "";
		});
	}

	/** 随机生成一个人类风格的名字（1/16 概率为 Maocry55 —— 对应鬼神境流派） */
	private static String randomHumanName(net.minecraft.server.level.ServerLevel level) {
		if (level.random.nextInt(16) == 0)
			return "Maocry55";
		String[] a = { "Sky", "Night", "Iron", "Cloud", "Storm", "Silent", "Red", "Myst", "Ember", "Frost" };
		String[] b = { "Wolf", "Fox", "Blade", "River", "Wind", "Leaf", "Stone", "Raven", "Flame", "Drift" };
		return a[level.random.nextInt(a.length)] + b[level.random.nextInt(b.length)] + (level.random.nextInt(90) + 10);
	}

	/** 牌河第 index 张的桌面位置（6 张一排；结算粒子定位用） */
	public static Vec3 riverTilePos(net.minecraft.server.level.ServerLevel level, BlockPos origin, int seat, int index) {
		return TableLayout.riverTarget(TableLayout.of(level, origin), seat, index);
	}

	/** 手牌第 index 张位置（含摸牌位 index≥13 外隔半牌宽；结算粒子定位用） */
	public static Vec3 handTilePos(net.minecraft.server.level.ServerLevel level, BlockPos origin, int seat, int index) {
		return TableLayout.handTarget(TableLayout.of(level, origin), seat, index);
	}

	/** 每服务端 tick 调用：回合倒计时（经验条显示）+ 超时自动摸切 */
	public static void processTurnTimers(net.minecraft.server.MinecraftServer server) {
		for (net.minecraft.server.level.ServerLevel lvl : server.getAllLevels()) {
			for (BlockPos origin : mcr.richi.game.RichiTableManager.originsIn(lvl)) {
				var st = mcr.richi.game.RichiTableManager.get(origin);
				if (st == null || st.turnSeat < 0 || st.turnDeadline <= 0)
					continue;
				long now = lvl.getGameTime();
				if (now < st.turnDeadline) {
					// 倒计时屏幕叠加层（客户端 HUD 渲染）："x+XX" 短考白 + 长考黄；长考中只显示黄色长考
					var p = st.playerOf(lvl, st.turnSeat);
					if (p != null) {
						int shortSec = st.turnInLong ? 0 : (int) Math.ceil((st.turnDeadline - now) / 20.0);
						int longSec = st.turnInLong
								? (int) Math.ceil((st.turnDeadline - now) / 20.0)
								: (st.longBank[st.turnSeat] + 19) / 20;
						String key = shortSec + ":" + longSec;
						if (!key.equals(LAST_COUNTDOWN.get(p.getUUID()))) {
							LAST_COUNTDOWN.put(p.getUUID(), key);
							net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p,
									new mcr.richi.network.MahjongCountdownPayload.CountdownMessage(shortSec, longSec));
						}
					}
					continue;
				}
				if (!st.turnInLong) {
					// 短考耗尽 → 进入长考：开始消耗本局长考银行（银行已空则立即自动摸切）
					int bank = st.longBank[st.turnSeat];
					st.turnInLong = true;
					if (bank > 0) {
						st.turnLimit = bank;
						st.turnDeadline = now + bank;
						continue;
					}
				}
				// 长考银行耗尽：归零并自动摸切（RiichiGame 内部结算本回合长考消耗）
				st.longBank[st.turnSeat] = 0;
				var game = mcr.richi.game.RichiTableManager.getGame(origin);
				if (game != null)
					game.autoDiscard();
				else
					st.turnSeat = -1; // 无对局（异常残留）清除回合防死循环
			}
		}
	}

	/** 按指定文本与标签生成 text_display（billboard 居中 + 默认背景；大厅队列文本用） */
	public static void spawnTextDisplay(Level level, Vec3 pos, String tag, String text) {
		DisplayEntityNbt.spawn(level, DisplayEntityNbt.textDisplay(level.registryAccess(), tag, text), pos, 0);
	}

	/**
	 * 开局发牌动画（RiichiGame.deal 调用）：清桌 → 隐藏风盘模型 → 生成手牌交互代理（随动画逐张出现）
	 * → 状态广播（dealAnim&gt;0，客户端按摆位 delay 重放发牌动画）。
	 *
	 * @return 动画总时长（刻），动画结束后应开始东家第一回合
	 */
	public static int startDealAnimation(net.minecraft.server.level.ServerLevel level, BlockPos origin,
			RichiTableState st) {
		mcr.richi.MahjongTicker.cancel(origin);
		clearDisplays(level, origin);
		setGameActive(level, origin, true);
		spawnHandInteractors(level, origin, st, true);
		RichiTableSync.broadcast(level, origin, 1);
		return 4 + 52 * TableLayout.DEAL_INTERVAL_TICKS; // 座位标记 4 + 手牌 52（空牌河/副露）
	}

	/**
	 * 按 plan() 的 HAND 立牌项生成手牌交互代理：不可见的 Interaction 实体（宽 0.16 ≈ 牌宽、
	 * 高 0.22 ≈ 牌长），准心指到牌右键/左键即服务端交互事件（richi_hand 标签），不含任何牌面数据。
	 * animate=true 时代理随发牌动画逐张出现（与客户端渲染同延迟）。
	 */
	private static void spawnHandInteractors(net.minecraft.server.level.ServerLevel level, BlockPos origin,
			RichiTableState st, boolean animate) {
		TableLayout.Geom geom = TableLayout.of(level, origin);
		List<TableLayout.Placement> placements = TableLayout.plan(geom, st,
				List.of(st.handCodes(0), st.handCodes(1), st.handCodes(2), st.handCodes(3)), animate);
		for (TableLayout.Placement p : placements) {
			if (!TableLayout.KIND_HAND.equals(p.kind()) || p.axis() != null)
				continue; // 仅立牌有代理（推倒/盖牌不可点击）
			if (animate)
				mcr.richi.MahjongTicker.schedule(p.delay(), origin,
						() -> spawnHandInteractor(level, p.base(), TableLayout.handTag(p.seat(), p.index())));
			else
				spawnHandInteractor(level, p.base(), TableLayout.handTag(p.seat(), p.index()));
		}
	}

	/**
	 * 手牌交互代理：不可见的 Interaction 实体（原版为 display 实体设计的交互载体），
	 * 尺寸贴合牌体（宽 0.16 ≈ 牌宽 0.156、高 0.22 ≈ 牌长 0.208）。准心指到牌右键时
	 * 服务端收到 EntityInteract 事件（richi_hand 标签），牌不在风盘方块上也能交互。
	 * extraTag = richi_hand_&lt;家&gt;_&lt;序号&gt;。
	 */
	private static void spawnHandInteractor(Level level, Vec3 pos, String extraTag) {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "minecraft:interaction");
		ListTag tags = new ListTag();
		tags.add(StringTag.valueOf("richi_display"));
		tags.add(StringTag.valueOf("richi_hand"));
		tags.add(StringTag.valueOf(extraTag));
		tag.put("Tags", tags);
		tag.putFloat("width", 0.16f);
		tag.putFloat("height", 0.22f);
		tag.putBoolean("response", true); // 响应右键交互
		EntityType.loadEntityRecursive(tag, level, entity -> {
			entity.moveTo(pos.x, pos.y, pos.z, 0, 0);
			level.addFreshEntity(entity);
			return entity;
		});
	}

	/**
	 * 摸牌后补建该张的交互代理：发牌时代理只覆盖 13 张常驻手牌，摸到的第 14 张
	 * （分离位）没有实体可供射线命中，导致"摸到的牌点不中"。
	 */
	public static void spawnDrawnInteractor(net.minecraft.server.level.ServerLevel level, BlockPos origin,
			int seat, int index) {
		if (!(level.getBlockState(origin).getBlock() instanceof FengPanBlock))
			return;
		removeHandInteractor(level, origin, seat, index);
		TableLayout.Geom geom = TableLayout.of(level, origin);
		spawnHandInteractor(level,
				TableLayout.standingEntityPos(TableLayout.handTarget(geom, seat, index, true)),
				TableLayout.handTag(seat, index));
	}

	/** 打出后移除某张手牌的交互代理（按标签精确清理，无则幂等） */
	public static void removeHandInteractor(net.minecraft.server.level.ServerLevel level, BlockPos origin,
			int seat, int index) {
		if (!(level.getBlockState(origin).getBlock() instanceof FengPanBlock))
			return;
		String tag = TableLayout.handTag(seat, index);
		Direction forward = level.getBlockState(origin).getValue(FACING);
		Direction right = forward.getClockWise();
		BlockPos farCorner = origin.relative(forward, SIZE - 1).relative(right, SIZE - 1);
		AABB box = AABB.encapsulatingFullBlocks(origin, farCorner).expandTowards(0, 1, 0)
				.expandTowards(5, 4, 5).expandTowards(-5, 0, -5);
		for (Entity e : level.getEntitiesOfClass(Entity.class, box,
				e -> e.getType() == EntityType.INTERACTION && e.getTags().contains(tag)))
			e.discard();
	}

	/** 顶雪模型透光 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}
}
