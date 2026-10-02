package mcr.richi.game;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 风盘设置持久化（按维度存储）：记录每张风盘（桌角原点）的"上一次配置"——对局类型/思考时限/明牌/
 * AI 预约座位/各座位独立 AI 形象。存于世界数据 data/mahjong_tables.dat，服务器重启后恢复。
 * 写入时机：大厅设置动作变更时（MahjongLobby.saveSettings）；读取时机：新建 RichiTableState
 * （getOrCreate / reset）时应用，让每张桌子记住上次的配置。
 */
public final class MahjongTableSavedData extends SavedData {
	private static final String DATA_NAME = "mahjong_tables";

	/** 原点 "x,y,z" → 该桌设置 */
	private final Map<String, Entry> tables = new HashMap<>();

	/** 一张风盘的可持久化设置（RichiTableState 中对应字段的快照） */
	public record Entry(int gameType, int thinkIdx, int aiSpeedIdx, boolean openHand, boolean[] reservedAI,
			String[] avatarModes) {
	}

	public MahjongTableSavedData() {
	}

	public static MahjongTableSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(MahjongTableSavedData::new, MahjongTableSavedData::load, null), DATA_NAME);
	}

	private static String key(BlockPos pos) {
		return pos.getX() + "," + pos.getY() + "," + pos.getZ();
	}

	/** 快照当前设置（设置变更后调用；只落盘等待阶段的配置字段） */
	public void set(ServerLevel level, BlockPos origin, RichiTableState st) {
		boolean[] reserved = new boolean[4];
		String[] modes = new String[4];
		for (int i = 0; i < 4; i++) {
			reserved[i] = st.reservedAI[i];
			String m = st.aiAvatarModes[i];
			modes[i] = m == null ? "" : m;
		}
		tables.put(key(origin), new Entry(st.gameType, st.thinkIdx, st.aiSpeedIdx, st.openHand, reserved, modes));
		setDirty();
	}

	/** 新建状态时应用持久化设置（等待阶段字段，幂等） */
	public void applyTo(BlockPos origin, RichiTableState st) {
		Entry e = tables.get(key(origin));
		if (e == null)
			return;
		st.gameType = e.gameType();
		st.thinkIdx = e.thinkIdx();
		st.aiSpeedIdx = e.aiSpeedIdx();
		st.openHand = e.openHand();
		for (int i = 0; i < 4; i++) {
			st.reservedAI[i] = e.reservedAI()[i];
			st.aiAvatarModes[i] = e.avatarModes()[i];
		}
	}

	private static MahjongTableSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		MahjongTableSavedData data = new MahjongTableSavedData();
		for (String k : tag.getAllKeys()) {
			CompoundTag t = tag.getCompound(k);
			boolean[] reserved = new boolean[4];
			int mask = t.getInt("r");
			for (int i = 0; i < 4; i++)
				reserved[i] = (mask & 1 << i) != 0;
			ListTag list = t.getList("a", Tag.TAG_STRING);
			String[] modes = new String[4];
			for (int i = 0; i < 4 && i < list.size(); i++)
				modes[i] = list.getString(i);
			data.tables.put(k, new Entry(t.getInt("g"), t.getInt("t"), t.getInt("s"), t.getBoolean("o"),
					reserved, modes));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		for (Map.Entry<String, Entry> e : tables.entrySet()) {
			CompoundTag t = new CompoundTag();
			Entry en = e.getValue();
			t.putInt("g", en.gameType());
			t.putInt("t", en.thinkIdx());
			t.putInt("s", en.aiSpeedIdx());
			t.putBoolean("o", en.openHand());
			int mask = 0;
			ListTag list = new ListTag();
			for (int i = 0; i < 4; i++) {
				if (en.reservedAI()[i])
					mask |= 1 << i;
				list.add(StringTag.valueOf(en.avatarModes()[i] == null ? "" : en.avatarModes()[i]));
			}
			t.putInt("r", mask);
			t.put("a", list);
			tag.put(e.getKey(), t);
		}
		return tag;
	}
}
