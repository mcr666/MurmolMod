package mcr.murmol.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;
import mcr.murmol.init.MurmolModGameRules;

/**
 * 会飞形态飞行机制游戏规则（{@link MurmolModGameRules#FERAL_ELYTRA_FLIGHT}）的网络同步。
 * GameRules 运行时被 /gamerule 修改后不会自动同步到客户端，故登录/规则变更时由服务端主动下发；
 * 客户端缓存于 {@link #CLIENT_ELYTRA}，供飞行判定（鞘翅启动/动画）本地读取。
 */
public class FeralFlightPayloads {
	/** 客户端缓存：true=鞘翅式滑翔；默认 false（悬停飞行），登录后由服务端权威值覆盖 */
	public static volatile boolean CLIENT_ELYTRA = false;

	public record SyncMessage(boolean elytra) implements CustomPacketPayload {
		public static final Type<SyncMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "feral_flight_sync"));

		public static final StreamCodec<RegistryFriendlyByteBuf, SyncMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, SyncMessage msg) -> buf.writeBoolean(msg.elytra()),
				(RegistryFriendlyByteBuf buf) -> new SyncMessage(buf.readBoolean()));

		@Override
		public Type<SyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final SyncMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> CLIENT_ELYTRA = message.elytra());
			}
		}
	}

	/** 向单个玩家同步当前规则值（登录时） */
	public static void syncTo(ServerPlayer player) {
		boolean value = player.serverLevel().getGameRules().getBoolean(MurmolModGameRules.FERAL_ELYTRA_FLIGHT);
		PacketDistributor.sendToPlayer(player, new SyncMessage(value));
	}

	/** 规则变更后向全体在线玩家广播 */
	public static void syncToAll(MinecraftServer server) {
		boolean value = server.overworld().getGameRules().getBoolean(MurmolModGameRules.FERAL_ELYTRA_FLIGHT);
		PacketDistributor.sendToAllPlayers(new SyncMessage(value));
	}

	public static void register() {
		MurmolMod.addNetworkMessage(SyncMessage.TYPE, SyncMessage.STREAM_CODEC, SyncMessage::handleData);
	}
}
