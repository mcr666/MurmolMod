package mcr.murmol.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.mojang.datafixers.util.Pair;

import mcr.murmol.MurmolMod;
import mcr.murmol.init.MurmolModBiomes;
import mcr.murmol.init.MurmolModItems;

/**
 * 灵气指南针目标同步（S→C）。
 *
 * <p>服务端用 {@code ServerLevel.findClosestBiome3d} 定位最近的幻星瘟疫群系（与
 * {@code /locate biome} 同款：走区块生成器，半径 6400，不要求区块已加载），
 * 仅在玩家手持指南针、每 40 tick 或移动 16 格后重搜，目标变化才发包；
 * 客户端 CompassItemPropertyFunction 直接读 {@link #clientTarget}。</p>
 */
public class AstralCompassPayload {
	/** 客户端缓存的最近群系位置（null = 无目标，指针乱转） */
	public static volatile GlobalPos clientTarget;

	/** S→C：target 为空表示无目标 */
	public record Message(GlobalPos target) implements CustomPacketPayload {
		public static final Type<Message> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "astral_compass_target"));

		public static final StreamCodec<RegistryFriendlyByteBuf, Message> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, Message msg) -> {
					GlobalPos t = msg.target();
					buf.writeBoolean(t != null);
					if (t != null) {
						buf.writeResourceLocation(t.dimension().location());
						buf.writeBlockPos(t.pos());
					}
				},
				(RegistryFriendlyByteBuf buf) -> {
					if (!buf.readBoolean())
						return new Message(null);
					ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
					return new Message(GlobalPos.of(dim, buf.readBlockPos()));
				});

		@Override
		public Type<Message> type() {
			return TYPE;
		}

		public static void handleData(final Message message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND)
				context.enqueueWork(() -> clientTarget = message.target());
		}
	}

	public static void register() {
		MurmolMod.addNetworkMessage(Message.TYPE, Message.STREAM_CODEC, Message::handleData);
	}

	// ==================================================================
	// 服务端：持有时定期定位并同步
	// ==================================================================

	@EventBusSubscriber
	public static final class Tracker {
		/** 重搜间隔（tick）与最小移动距离（格） */
		private static final long RESEARCH_INTERVAL = 40L;
		private static final double RESEARCH_MOVE_SQR = 16.0D * 16.0D;
		/** 搜索半径（同 /locate biome） */
		private static final int SEARCH_RADIUS = 6400;

		private record State(long lastTick, BlockPos lastCenter, GlobalPos sent) {
		}

		private static final Map<UUID, State> STATES = new HashMap<>();

		@SubscribeEvent
		public static void onServerTick(ServerTickEvent.Post event) {
			var server = event.getServer();
			long tick = server.getTickCount();
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				boolean holding = player.getMainHandItem().is(MurmolModItems.ASTRAL_COMPASS.get())
						|| player.getOffhandItem().is(MurmolModItems.ASTRAL_COMPASS.get());
				State st = STATES.get(player.getUUID());
				if (!holding) {
					// 停止持有：清状态，若此前有目标则发空包让指针恢复乱转
					if (st != null) {
						STATES.remove(player.getUUID());
						if (st.sent() != null)
							PacketDistributor.sendToPlayer(player, new Message(null));
					}
					continue;
				}
				BlockPos center = player.blockPosition();
				if (st != null && tick - st.lastTick() < RESEARCH_INTERVAL
						&& center.distSqr(st.lastCenter()) <= RESEARCH_MOVE_SQR)
					continue;
				GlobalPos found = findNearest(player.serverLevel(), center);
				GlobalPos last = st == null ? null : st.sent();
				STATES.put(player.getUUID(), new State(tick, center, found));
				if (!java.util.Objects.equals(found, last))
					PacketDistributor.sendToPlayer(player, new Message(found));
			}
		}

		/** 走区块生成器搜索最近幻星瘟疫群系（无需区块已加载），无则 null */
		private static GlobalPos findNearest(ServerLevel level, BlockPos center) {
			Pair<BlockPos, ?> hit = level.findClosestBiome3d(
					holder -> holder.is(MurmolModBiomes.ASTRAL_INFECTION_BIOME),
					center, SEARCH_RADIUS, 32, 64);
			return hit == null ? null : GlobalPos.of(level.dimension(), hit.getFirst());
		}
	}
}
