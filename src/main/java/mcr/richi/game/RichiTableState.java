package mcr.richi.game;

/**
 * 一张风盘的牌局状态（纯数据，行为在 RichiTableManager）。
 * 各处牌均用牌谱记法字符串存储（如 "12345m123p456s11z"）。
 * 家序索引 0..3 = 東南西北（座位名另见 FengPanBlock）。
 */
public class RichiTableState {
	/** 各家手牌（13 张） */
	public final String[] hands = new String[4];
	/** 各家牌河（切出的牌，面朝上） */
	public final String[] rivers = new String[4];
	/** 各家副露（吃碰杠，含声明牌） */
	public final String[] melds = new String[4];
	/** 中央宝牌指示区（5 叠 × 2 张 = 10 张，叠序存放） */
	public String doraWall = "";
	/** 牌山剩余（未摸的牌，暂不渲染） */
	public String wall = "";
	/** 各家点数 */
	public final int[] points = new int[4];
	/** 当前回合座位（0..3），-1 = 未开局/已流局（简单对局：回合家摸一张→打出→下家摸牌） */
	public int turnSeat = -1;
	/** 入座玩家（UUID 字符串，null = 空位；座位 0..3 = 東南西北，发牌者坐東家） */
	public final String[] players = new String[4];
	/** 当前局数（東/南 X 局，1..4） */
	public int round = 1;
	/** 当前局风（0=東，1=南；半庄战南场局风） */
	public int roundWind = 0;
	/** 对局长度（开局前在大厅选择）：0=一局 1=东风战(4局) 2=半庄战(8局) */
	public int gameType = 0;
	/** 思考时间档位：{每巡时限秒, 读秒(长思考库)秒}；thinkIdx 由管理界面切换，开局时套用 */
	public static final int[][] THINK_PRESETS = { { 5, 20 }, { 10, 30 }, { 60, 0 }, { 3, 5 } };
	public int thinkIdx = 0;
	/** AI 打牌速度档：0=快(现行) 1=中(×2) 2=慢(×3)；AI 动作延迟统一乘该系数 */
	public static final int[] AI_SPEED_MUL = { 1, 2, 3 };
	public int aiSpeedIdx = 0;
	/** 本场数（连庄/流局累加，庄家输牌清零；本场费 300/本场） */
	public int honba = 0;
	/** 各座位独立 AI 形象模式（索引同座位；null/空 = 随机形态） */
	public final String[] aiAvatarModes = new String[4];
	/** AI 形象脏标记：预约/形象模式变化后置 true，resolveAvatars 解析一次后清除（避免每次广播重复解析阻塞主线程） */
	public transient boolean avatarsDirty = true;
	/** 各 AI 座位最终形态 id（deal 时按 aiAvatarModes 解析随机并固定；空 = 非 AI/未开局）。随座位轮转迁移，经同步包下发客户端渲染假玩家 */
	public final String[] aiAvatarForms = new String[4];
	/** 各 AI 座位人类模式显示名（human 模式随机名，其余空串） */
	public final String[] aiAvatarNames = new String[4];
	/** 各 AI 座位人类模式皮肤档案 UUID（空串 = 未解析到正版档案） */
	public final String[] aiAvatarSkins = new String[4];
	/**
	 * 各 AI 座位流派（resolveAvatars 按形象/名字分配）：
	 * 0 一般流(20%次优切牌，可副露可防守) / 1 野猪流(可副露无防守) / 2 门清流(无副露有防守) /
	 * 3 御无双(无副露无防守) / 4 魂天流(可副露会防守) / 5 闪电流(只摸切) / 6 鬼神境(知他家手牌，可副露可防守)
	 */
	public final int[] aiFlows = new int[4];
	/** 明牌（默认关）：关时参战雀士看不到他人手牌牌面（打出/推倒后可见），显示未知牌面；观战者始终可见全部 */
	public boolean openHand = false;
	/** 当前回合截止时刻（游戏刻，0 = 无计时）；超时自动摸切 */
	public long turnDeadline;
	/** 当前回合限时长（刻）：短考 5s（每回合重置）；进入长考后 = 长考银行剩余 */
	public int turnLimit = 5 * 20;
	/** 当前回合是否处于长考（短考耗尽后进入，开始消耗长考银行） */
	public boolean turnInLong;
	/** 各家长考银行（刻）：短考耗尽后逐回合扣减，仅每局重置（新牌局创建时回满） */
	public final int[] longBank = { 20 * 20, 20 * 20, 20 * 20, 20 * 20 };
	/** 牌桌阶段：0=等待中（可排队）1=对局中 2=已结束（等待重开） */
	public static final int PHASE_WAITING = 0, PHASE_PLAYING = 1, PHASE_FINISHED = 2;
	public int phase = PHASE_WAITING;
	/** 等待队列（UUID，先到先坐；开局时按序补入空座位） */
	public final java.util.List<String> queue = new java.util.ArrayList<>();
	/** 管理界面预约的 AI 座位（等待阶段设置，开局时该座位固定安排 AI） */
	public final boolean[] reservedAI = new boolean[4];
	/** 各座位是否为 AI（对局中有效，虚拟 UUID 见 RiichiBot） */
	public final boolean[] aiSeat = new boolean[4];
	/** 各家立直点棒数（宣言成功后 +1） */
	public final int[] riichiSticks = new int[4];
	/** 各家立直宣言牌在牌河中的下标（横摆渲染；-1 = 无 / 已被鸣牌取走） */
	public final int[] riichiRiverIdx = { -1, -1, -1, -1 };
	/** 立直宣言牌被鸣后待补标记：该家下一张切牌改为横摆 */
	public final boolean[] riichiPending = new boolean[4];
	/** 局终手牌展示：0=对局中立牌，1=推倒（面朝上，和牌者），2=盖牌（面朝下） */
	public final int[] handsExposed = new int[4];
	/** 对局中"投票结束对局"：各人类座位是否已投票（全员同意 → 强制终局） */
	public final boolean[] endVotes = new boolean[4];
	/** 各家最近一次打牌是否手切（1 = 手切，0 = 摸切/无）：隐藏手牌的牌背动画 key 错位用 */
	public final int[] lastTedashi = new int[4];
	/** 手牌展示常量 */
	public static final int HAND_STAND = 0, HAND_FACE_UP = 1, HAND_FACE_DOWN = 2;
	/** 已揭开的宝牌指示牌叠数（开局 1，杠后 +1；渲染翻开对应上张） */
	public int revealedIndicators = 1;
	/** 当前选中的手牌（-1 = 无选中；渲染时该牌升高，再次右键打出，见 FengPanBlock） */
	public int selectedSeat = -1;
	public int selectedIndex = -1;
	/** 当前回合家已摸牌未打（-1 = 无）：该家手牌最右一张为刚摸的牌，渲染时与原手牌隔开一个牌位 */
	public int drawnSeat = -1;
	/** 结算横幅文本（瞬态，客户端 text_display 渲染；finishRound 设置，resetHand 清空） */
	public String banner = "";

	public RichiTableState() {
		java.util.Arrays.fill(points, 25000);
	}

	/** 清除选牌状态（打出/重置时调用） */
	public void clearSelection() {
		selectedSeat = -1;
		selectedIndex = -1;
	}

	/** 各家手牌解析为牌面代码（渲染用；记法非法时返回空列表，不让渲染中断牌局） */
	public java.util.List<Integer> handCodes(int seat) {
		return safeParse(hands[seat]);
	}

	public java.util.List<Integer> riverCodes(int seat) {
		return safeParse(rivers[seat]);
	}

	/** 各家副露分组解析：melds 字符串以 ";" 分隔多组，每组 "声明家:牌码"（声明家 = 被吃/碰/杠的来源座位） */
	public java.util.List<java.util.List<Integer>> meldGroups(int seat) {
		java.util.List<java.util.List<Integer>> groups = new java.util.ArrayList<>();
		for (MeldGroup g : meldGroupInfos(seat))
			groups.add(g.codes());
		return groups;
	}

	/** 副露组（含声明来源座位） */
	public record MeldGroup(int fromSeat, java.util.List<Integer> codes) {
	}

	public java.util.List<MeldGroup> meldGroupInfos(int seat) {
		java.util.List<MeldGroup> groups = new java.util.ArrayList<>();
		if (melds[seat] == null || melds[seat].isEmpty())
			return groups;
		for (String part : melds[seat].split(";")) {
			if (part.isEmpty())
				continue;
			int from = -1;
			String codes = part;
			int colon = part.indexOf(':');
			if (colon > 0) {
				try {
					from = Integer.parseInt(part.substring(0, colon));
					codes = part.substring(colon + 1);
				} catch (NumberFormatException ignored) {
					from = -1;
					codes = part;
				}
			}
			groups.add(new MeldGroup(from, safeParse(codes)));
		}
		return groups;
	}

	public java.util.List<Integer> doraCodes() {
		return safeParse(doraWall);
	}

	public java.util.List<Integer> wallCodes() {
		return safeParse(wall);
	}

	/** 某座位的入座玩家（未入座/离线返回 null） */
	public net.minecraft.server.level.ServerPlayer playerOf(net.minecraft.server.level.ServerLevel level, int seat) {
		String u = players[seat];
		if (u == null)
			return null;
		try {
			return level.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(u));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static java.util.List<Integer> safeParse(String notation) {
		try {
			return MahjongTileNotation.parse(notation);
		} catch (RuntimeException e) {
			com.mojang.logging.LogUtils.getLogger().error("[Richi] 牌谱解析失败: {}", notation, e);
			return java.util.List.of();
		}
	}
}
