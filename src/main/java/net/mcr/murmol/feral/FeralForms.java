package net.mcr.murmol.feral;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 形态注册表。持有所有形态实例，按 id 查找。
 * 新增形态：创建 FeralForm 子类并在此注册一行即可。
 */
public final class FeralForms {

	public static final FeralForm HUMAN = new HumanForm();
	public static final FeralForm LUOHONG = new LuohongForm();
	public static final FeralForm CHEN_HUANG = new ChenHuangForm();
	public static final FeralForm MOSS_BEAST = new MossBeastForm();
	public static final FeralForm SILKMOTH = new SilkmothForm();
	public static final FeralForm KOMAINU = new KomainuForm();
	public static final FeralForm WENYAO = new WenyaoForm();

	private static final Map<String, FeralForm> BY_ID = new HashMap<>();

	static {
		register(HUMAN);
		register(LUOHONG);
		register(CHEN_HUANG);
		register(MOSS_BEAST);
		register(SILKMOTH);
		register(KOMAINU);
		register(WENYAO);
	}

	private FeralForms() {
	}

	private static void register(FeralForm form) {
		BY_ID.put(form.getId(), form);
	}

	public static FeralForm byId(String id) {
		return BY_ID.getOrDefault(id, HUMAN);
	}

	public static Collection<FeralForm> all() {
		return Collections.unmodifiableCollection(BY_ID.values());
	}
}
