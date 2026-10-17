package mcr.richi.game;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * 参牌者注册表：把任意实体解析为 {@link MahjongParticipant}。
 * <p>
 * 解析顺序：实体本身实现接口（如 MurmolNpcEntity）→ 按实体类型查已注册工厂
 * （供外部 mod 如 TouhouLittleMaid 兼容层为女仆注册适配器，经 {@link #register} 注册）。
 */
public final class MahjongParticipantRegistry {
	private static final Map<EntityType<?>, Function<Entity, MahjongParticipant>> FACTORIES = new HashMap<>();

	private MahjongParticipantRegistry() {
	}

	/** 为指定实体类型注册参牌者工厂（返回 null 表示该实体不可入座） */
	public static void register(EntityType<?> type, Function<Entity, MahjongParticipant> factory) {
		FACTORIES.put(type, factory);
	}

	/** 解析实体为参牌者；不是参牌者返回 null */
	public static MahjongParticipant of(Entity entity) {
		if (entity instanceof MahjongParticipant p)
			return p;
		Function<Entity, MahjongParticipant> f = FACTORIES.get(entity.getType());
		return f == null ? null : f.apply(entity);
	}
}
