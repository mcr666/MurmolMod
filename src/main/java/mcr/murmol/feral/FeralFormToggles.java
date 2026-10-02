package mcr.murmol.feral;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import mcr.murmol.network.FormTogglePayloads;

import java.util.HashMap;
import java.util.Map;

/**
 * 世界级形态启用/禁用开关（两仪物品管理）。
 * 禁用的形态无法通过变形入口进入；已处于该形态的玩家不做额外检查。
 * 数据随世界存档持久化（SavedData），默认全部启用。
 */
public class FeralFormToggles extends SavedData {
	private static final String DATA_NAME = "feral_form_toggles";

	/** 各形态启用状态，缺失 = 启用（默认值），key 为形态 id */
	private final Map<String, Boolean> enabledForms = new HashMap<>();

	/** 形态当前是否启用：服务端读世界存档，客户端读同步缓存（物理侧由调用方区分） */
	public static boolean isEnabled(FeralForm form, boolean clientSide) {
		if (clientSide) {
			return FormTogglePayloads.CLIENT_ENABLED.getOrDefault(form.getId(), Boolean.TRUE);
		}
		FeralFormToggles data = getServerData();
		if (data == null)
			return true;
		return data.enabledForms.getOrDefault(form.getId(), Boolean.TRUE);
	}

	/** 服务端切换形态启用状态（仅权限校验通过后调用），并向所有玩家同步 */
	public static void serverToggle(ServerPlayer operator, String formId) {
		FeralFormToggles data = getServerData();
		if (data == null)
			return;
		boolean now = !data.enabledForms.getOrDefault(formId, Boolean.TRUE);
		data.enabledForms.put(formId, now);
		data.setDirty();
		FormTogglePayloads.syncToAll(operator.server);
	}

	private static FeralFormToggles getServerData() {
		var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return null;
		return server.overworld().getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(FeralFormToggles::new, FeralFormToggles::load, null), DATA_NAME);
	}

	public static FeralFormToggles load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
		FeralFormToggles data = new FeralFormToggles();
		CompoundTag root = tag.getCompound("forms");
		for (String id : root.getAllKeys()) {
			data.enabledForms.put(id, root.getBoolean(id));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
		CompoundTag root = new CompoundTag();
		for (Map.Entry<String, Boolean> entry : enabledForms.entrySet()) {
			root.putBoolean(entry.getKey(), entry.getValue());
		}
		tag.put("forms", root);
		return tag;
	}

	/** 全部形态 id 列表（GUI 与同步用，顺序固定），不含人类 */
	public static java.util.List<String> formIds() {
		return FeralForms.all().stream()
				.filter(f -> f != FeralForms.HUMAN)
				.map(FeralForm::getId)
				.toList();
	}

	/** 全量开关状态快照（网络同步用）：直接读主世界存档 */
	public static Map<String, Boolean> serverStates(MinecraftServer server) {
		FeralFormToggles data = server.overworld().getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(FeralFormToggles::new, FeralFormToggles::load, null), DATA_NAME);
		Map<String, Boolean> states = new HashMap<>();
		for (String id : formIds()) {
			states.put(id, data.enabledForms.getOrDefault(id, Boolean.TRUE));
		}
		return states;
	}
}
