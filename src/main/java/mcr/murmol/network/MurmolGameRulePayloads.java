package mcr.murmol.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;
import mcr.murmol.init.MurmolModGameRules;

/**
 * 两仪「游戏规则」设置界面的网络同步：
 * - Request（C2S）：打开界面时请求当前全部规则值，服务端单发回 Sync
 * - Set（C2S）：2 级权限玩家切换/调整某条游戏规则（与 /gamerule 等效），成功后全员广播
 * - Sync（S2C）：规则 id -> 当前值（布尔/整数混合，按 RuleDef.kind 解码），客户端缓存于 {@link #CLIENT_RULES}
 *
 * GameRules 运行时值不会自动同步到客户端，故所有界面展示值均以服务端下发为准。
 */
public class MurmolGameRulePayloads {
	public enum Kind {
		BOOL,
		INT
	}

	/** 界面展示顺序、类型与取值范围（id 字面量须与 MurmolModGameRules 注册名一致） */
	public record RuleDef(String id, Kind kind, Object key, int min, int max) {
	}

	public static final List<RuleDef> RULES = List.of(
			new RuleDef("murmolRequireFormUnlocked", Kind.BOOL, MurmolModGameRules.REQUIRE_FORM_UNLOCKED, 0, 0),
			new RuleDef("murmolAllowScrapSoulRecipe", Kind.BOOL, MurmolModGameRules.ALLOW_SCRAP_SOUL_RECIPE, 0, 0),
			new RuleDef("murmolSpiritTableXpCost", Kind.BOOL, MurmolModGameRules.SPIRIT_TABLE_XP_COST, 0, 0),
			new RuleDef("murmolFeralElytraFlight", Kind.BOOL, MurmolModGameRules.FERAL_ELYTRA_FLIGHT, 0, 0),
			new RuleDef("murmolAstralInfectionRange", Kind.INT, MurmolModGameRules.ASTRAL_INFECTION_RANGE,
					MurmolModGameRules.ASTRAL_INFECTION_RANGE_MIN, MurmolModGameRules.ASTRAL_INFECTION_RANGE_MAX));

	/** 客户端缓存：规则 id -> 值（Boolean 或 Integer；未收到同步前缺失，界面显示加载态） */
	public static final Map<String, Object> CLIENT_RULES = new ConcurrentHashMap<>();

	private static void writeValue(FriendlyByteBuf buf, RuleDef def, Object value) {
		if (def.kind() == Kind.INT)
			buf.writeVarInt(((Number) value).intValue());
		else
			buf.writeBoolean((Boolean) value);
	}

	private static Object readValue(FriendlyByteBuf buf, RuleDef def) {
		return def.kind() == Kind.INT ? buf.readVarInt() : buf.readBoolean();
	}

	// ==================== S2C 全量同步 ====================

	public record SyncMessage(Map<String, Object> values) implements CustomPacketPayload {
		public static final Type<SyncMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "gamerules_sync"));

		public static final StreamCodec<FriendlyByteBuf, SyncMessage> STREAM_CODEC = StreamCodec.of(
				(FriendlyByteBuf buf, SyncMessage msg) -> {
					buf.writeVarInt(msg.values().size());
					msg.values().forEach((id, value) -> {
						buf.writeUtf(id);
						RuleDef def = byId(id);
						writeValue(buf, def == null ? new RuleDef(id, Kind.BOOL, null, 0, 0) : def, value);
					});
				},
				(FriendlyByteBuf buf) -> {
					int size = buf.readVarInt();
					Map<String, Object> values = new LinkedHashMap<>();
					for (int i = 0; i < size; i++) {
						String id = buf.readUtf();
						RuleDef def = byId(id);
						if (def != null)
							values.put(id, readValue(buf, def));
						else
							buf.skipBytes(1);
					}
					return new SyncMessage(values);
				});

		@Override
		public Type<SyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final SyncMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> CLIENT_RULES.putAll(message.values()));
			}
		}
	}

	// ==================== C2S 打开界面请求全量值 ====================

	public record RequestMessage() implements CustomPacketPayload {
		public static final Type<RequestMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "gamerules_request"));

		public static final StreamCodec<FriendlyByteBuf, RequestMessage> STREAM_CODEC = StreamCodec.unit(new RequestMessage());

		@Override
		public Type<RequestMessage> type() {
			return TYPE;
		}

		public static void handleData(final RequestMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player) {
				context.enqueueWork(() -> PacketDistributor.sendToPlayer(player,
						new SyncMessage(snapshot(player.serverLevel().getGameRules()))));
			}
		}
	}

	// ==================== C2S 切换规则 ====================

	public record SetMessage(String id, int value) implements CustomPacketPayload {
		public static final Type<SetMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "gamerules_set"));

		public static final StreamCodec<FriendlyByteBuf, SetMessage> STREAM_CODEC = StreamCodec.of(
				(FriendlyByteBuf buf, SetMessage msg) -> {
					buf.writeUtf(msg.id());
					buf.writeVarInt(msg.value());
				},
				(FriendlyByteBuf buf) -> new SetMessage(buf.readUtf(), buf.readVarInt()));

		@Override
		public Type<SetMessage> type() {
			return TYPE;
		}

		public static void handleData(final SetMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player) {
				context.enqueueWork(() -> {
					// 与原版 /gamerule 一致的 2 级权限
					if (!player.hasPermissions(2))
						return;
					RuleDef def = byId(message.id());
					if (def == null)
						return;
					GameRules rules = player.serverLevel().getGameRules();
					if (def.kind() == Kind.INT) {
						int clamped = Math.max(def.min(), Math.min(def.max(), message.value()));
						rules.getRule((GameRules.Key<GameRules.IntegerValue>) def.key()).set(clamped, player.server);
					} else {
						rules.getRule((GameRules.Key<GameRules.BooleanValue>) def.key()).set(message.value() != 0, player.server);
					}
					// 变更后全员广播，任何打开着界面的客户端即时刷新
					PacketDistributor.sendToAllPlayers(new SyncMessage(snapshot(rules)));
				});
			}
		}
	}

	private static RuleDef byId(String id) {
		for (RuleDef def : RULES)
			if (def.id().equals(id))
				return def;
		return null;
	}

	private static Map<String, Object> snapshot(GameRules rules) {
		Map<String, Object> values = new LinkedHashMap<>();
		for (RuleDef def : RULES)
			if (def.kind() == Kind.INT)
				values.put(def.id(), rules.getInt((GameRules.Key<GameRules.IntegerValue>) def.key()));
			else
				values.put(def.id(), rules.getBoolean((GameRules.Key<GameRules.BooleanValue>) def.key()));
		return values;
	}

	public static void register() {
		MurmolMod.addNetworkMessage(SyncMessage.TYPE, SyncMessage.STREAM_CODEC, SyncMessage::handleData);
		MurmolMod.addNetworkMessage(RequestMessage.TYPE, RequestMessage.STREAM_CODEC, RequestMessage::handleData);
		MurmolMod.addNetworkMessage(SetMessage.TYPE, SetMessage.STREAM_CODEC, SetMessage::handleData);
	}
}
