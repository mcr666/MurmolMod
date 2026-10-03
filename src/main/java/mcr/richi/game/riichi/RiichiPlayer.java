package mcr.richi.game.riichi;

import java.util.ArrayList;
import java.util.List;

import mcr.richi.game.MahjongTileNotation;

/**
 * 立直麻雀玩家（纯数据）：手牌/副露/立直/点数。座位 0..3 = 東南西北（東家 = 庄家）。
 * 手牌列表约定：前 13 张（或更少，副露后）保持理牌序，刚摸的牌追加在末尾（渲染为摸牌位）。
 */
public class RiichiPlayer {
	/** 座位（0..3；轮庄座位轮转时可变） */
	public int seat;
	/** 玩家 UUID，AI 为虚拟 id（RiichiBot.aiUuid） */
	public final String uuid;
	/** 显示名（玩家名 / AI 名；踢出换 AI 时更新） */
	public String name;
	/** 是否 AI（踢出后可由 AI 接管，故可变） */
	public boolean ai;
	/** 手牌（末尾为刚摸的牌，若有） */
	public final List<Integer> hand = new ArrayList<>();
	/** 副露（吃碰杠） */
	public final List<Fuuro> melds = new ArrayList<>();
	/** 牌河（打出顺序） */
	public final List<Integer> river = new ArrayList<>();
	/** 立直状态 */
	public boolean riichi;
	/** 立直振听：立直后放过可荣和的牌（永久振听，自摸不受限） */
	public boolean riichiFuriten;
	/** 同巡/后巡振听：放过可荣和的牌（自己的下次打牌时解除，自摸不受限） */
	public boolean tempFuriten;
	/** 双立直（本局首次打牌即立直且无人鸣牌） */
	public boolean doubleRiichi;
	/** 本局已打牌次数（首次摸牌/首打判定用） */
	public int discardCount;
	/** 本局是否已摸过第一次牌（九种九牌判定用） */
	public boolean firstDrawDone;
	/** 本局点数 */
	public int points;

	public RiichiPlayer(int seat, String uuid, String name, boolean ai, int startPoints) {
		this.seat = seat;
		this.uuid = uuid;
		this.name = name;
		this.ai = ai;
		this.points = startPoints;
	}

	/** 门清（无副露，暗杠不破门清但此处简化为无任何副露即门清） */
	public boolean isMenzen() {
		for (Fuuro f : melds)
			if (f.type != Fuuro.Type.ANKAN)
				return false;
		return true;
	}

	/** 理牌：1-9m → 1-9p → 1-9s → 1-7z，红五（code=0）按 5 排序 */
	public void sortHand() {
		// 归一化排序键：红5万(0)→5、红5饼(10)→15、红5索(20)→25，否则红5饼/索会排到花色开头
		hand.sort(java.util.Comparator.comparingInt(c -> c == 0 ? 5 : c == 10 ? 15 : c == 20 ? 25 : c));
	}

	/** 手牌记法（渲染用）：理牌序 + 摸牌在末尾 */
	public String handNotation() {
		return MahjongTileNotation.format(hand);
	}

	/** 副露记法（渲染用）：组间 ";"，每组 = "声明家:牌码"，组内声明牌（纵牌）在末尾 */
	public String meldNotation() {
		StringBuilder sb = new StringBuilder();
		for (Fuuro f : melds) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(f.fromSeat).append(':');
			List<Integer> codes = new ArrayList<>(f.tiles.length);
			for (int t : f.displayOrder())
				codes.add(t);
			sb.append(MahjongTileNotation.format(codes));
		}
		return sb.toString();
	}

	/** 牌河记法（渲染用） */
	public String riverNotation() {
		return MahjongTileNotation.format(river);
	}
}
