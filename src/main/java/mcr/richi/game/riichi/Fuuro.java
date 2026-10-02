package mcr.richi.game.riichi;

/**
 * 副露（吃/碰/杠）记录（纯数据）。
 * tiles 为副露全部牌（杠 4 张、吃碰 3 张），calledTile 为被声明的那张（渲染为纵牌），
 * fromSeat 为被吃/碰/杠的玩家座位。
 */
public class Fuuro {
	public enum Type { CHII, PON, MINKAN, ANKAN, KAKAN }

	public final Type type;
	/** 副露牌面代码（含声明牌） */
	public final int[] tiles;
	/** 被声明牌（tiles 中的某一张，渲染为纵牌） */
	public final int calledTile;
	/** 被吃/碰/杠的玩家座位（自杠 = 自己） */
	public final int fromSeat;

	public Fuuro(Type type, int[] tiles, int calledTile, int fromSeat) {
		this.type = type;
		this.tiles = tiles;
		this.calledTile = calledTile;
		this.fromSeat = fromSeat;
	}

	/** 渲染顺序：横牌在前（长轴指向桌心），声明牌在最后（纵向）；同码牌（碰/杠）顺序无关 */
	public int[] displayOrder() {
		int[] order = new int[tiles.length];
		int idx = 0;
		boolean calledPlaced = false;
		for (int t : tiles) {
			if (!calledPlaced && t == calledTile) {
				calledPlaced = true; // 跳过第一张声明牌，其余横牌按原序保留
				continue;
			}
			order[idx++] = t;
		}
		order[tiles.length - 1] = calledTile;
		return order;
	}
}
