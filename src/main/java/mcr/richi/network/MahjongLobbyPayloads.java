package mcr.richi.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;
import mcr.richi.game.MahjongLobby;

/**
 * 麻将大厅（等待队列 / 管理）网络包。
 * <ul>
 * <li>SyncMessage（S→C）：全量同步一张桌的大厅状态（阶段/座位/队列/管理权限），
 * 客户端缓存按原点存放，管理界面与队列渲染据此刷新。</li>
 * <li>ActionMessage（C→S）：玩家操作（排队/退出/开始/安排与移除 AI/踢出）。</li>
 * <li>OpenMessage（S→C）：潜行右击风盘后请求客户端打开管理界面。</li>
 * </ul>
 * 客户端缓存只存原始类型与字符串（该类服务端也会加载）。
 */
public class MahjongLobbyPayloads {
	/** 客户端缓存：原点（打包 long）→ 最近一次 Sync */
	public static final java.util.Map<Long, LobbySync> CLIENT_SYNC = new java.util.HashMap<>();

	/** 座位/队列状态快照（客户端只读）；seatFlows：AI 座位流派索引，非 AI = -1；thinkIdx：思考时间档位；aiSpeedIdx：AI 速度档 */
	public record LobbySync(int phase, String[] seatNames, boolean[] seatAI,
			List<String> queueNames, boolean canManage, int gameType, int voteCount,
			boolean openHand, String[] seatAvatarModes, int[] seatFlows, int thinkIdx, int aiSpeedIdx) {
	}

	/** C→S 动作类型 */
	public static final int ACT_JOIN_QUEUE = 0, ACT_LEAVE_QUEUE = 1, ACT_START = 2,
			ACT_ADD_AI = 3, ACT_REMOVE_AI = 4, ACT_KICK = 5, ACT_SET_TYPE = 6, ACT_CLEAR = 7,
			ACT_VOTE_END = 8, ACT_SET_OPEN = 10, ACT_SET_AVATAR_SEAT = 12, ACT_SET_THINK = 13, ACT_SET_SPEED = 14;

	public record SyncMessage(long originPacked, int phase, List<String> seatNames, int aiMask,
			List<String> queueNames, boolean canManage, int gameType, int voteCount,
			boolean openHand, List<String> seatAvatarModes, List<Integer> seatFlows, int thinkIdx,
			int aiSpeedIdx) implements CustomPacketPayload {
		public static final Type<SyncMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_lobby_sync"));

		public static final StreamCodec<RegistryFriendlyByteBuf, SyncMessage> STREAM_CODEC = StreamCodec.of(
				MahjongLobbyPayloads::writeSync, MahjongLobbyPayloads::readSync);

		@Override
		public Type<SyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final SyncMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					String[] names = new String[4];
					for (int i = 0; i < 4; i++)
						names[i] = message.seatNames().get(i);
					boolean[] ai = new boolean[4];
					for (int i = 0; i < 4; i++)
						ai[i] = (message.aiMask() & (1 << i)) != 0;
					String[] seatModes = new String[4];
					for (int i = 0; i < 4; i++)
						seatModes[i] = message.seatAvatarModes().get(i);
					int[] seatFlows = new int[4];
					for (int i = 0; i < 4; i++)
						seatFlows[i] = message.seatFlows().get(i);
					CLIENT_SYNC.put(message.originPacked(),
							new LobbySync(message.phase(), names, ai, message.queueNames(),
									message.canManage(), message.gameType(), message.voteCount(),
									message.openHand(), seatModes, seatFlows, message.thinkIdx(),
									message.aiSpeedIdx()));
				});
			}
		}
	}

	/** C→S 操作：action 见 ACT_*，extra = 座位（ADD_AI/REMOVE_AI/KICK 用） */
	public record ActionMessage(long originPacked, int action, int extra) implements CustomPacketPayload {
		public static final Type<ActionMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_lobby_action"));

		public static final StreamCodec<RegistryFriendlyByteBuf, ActionMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, ActionMessage msg) -> {
					buf.writeLong(msg.originPacked());
					buf.writeVarInt(msg.action());
					buf.writeVarInt(msg.extra());
				},
				(RegistryFriendlyByteBuf buf) -> new ActionMessage(buf.readLong(), buf.readVarInt(), buf.readVarInt()));

		@Override
		public Type<ActionMessage> type() {
			return TYPE;
		}

		public static void handleData(final ActionMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (context.player() instanceof net.minecraft.server.level.ServerPlayer sp)
						MahjongLobby.handleAction(sp, BlockPos.of(message.originPacked()),
								message.action(), message.extra());
				});
			}
		}
	}

	/** S→C：打开管理界面 */
	public record OpenMessage(long originPacked) implements CustomPacketPayload {
		public static final Type<OpenMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_lobby_open"));

		public static final StreamCodec<RegistryFriendlyByteBuf, OpenMessage> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, OpenMessage msg) -> buf.writeLong(msg.originPacked()),
				(RegistryFriendlyByteBuf buf) -> new OpenMessage(buf.readLong()));

		@Override
		public Type<OpenMessage> type() {
			return TYPE;
		}

		public static void handleData(final OpenMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					if (net.minecraft.client.Minecraft.getInstance().screen == null)
						net.minecraft.client.Minecraft.getInstance().setScreen(
								new mcr.richi.client.MahjongLobbyScreen(BlockPos.of(message.originPacked())));
				});
			}
		}
	}

	private static void writeSync(RegistryFriendlyByteBuf buf, SyncMessage msg) {
		buf.writeLong(msg.originPacked());
		buf.writeVarInt(msg.phase());
		for (int i = 0; i < 4; i++) {
			String name = msg.seatNames().get(i);
			buf.writeUtf(name == null ? "" : name, 64);
		}
		buf.writeVarInt(msg.aiMask());
		buf.writeVarInt(msg.queueNames().size());
		for (String name : msg.queueNames())
			buf.writeUtf(name, 64);
		buf.writeBoolean(msg.canManage());
		buf.writeVarInt(msg.gameType());
		buf.writeVarInt(msg.voteCount());
		buf.writeBoolean(msg.openHand());
		for (int i = 0; i < 4; i++) {
			String mode = msg.seatAvatarModes().get(i);
			buf.writeUtf(mode == null ? "" : mode, 32);
		}
		for (int i = 0; i < 4; i++)
			buf.writeVarInt(msg.seatFlows().get(i));
		buf.writeVarInt(msg.thinkIdx());
		buf.writeVarInt(msg.aiSpeedIdx());
	}

	private static SyncMessage readSync(RegistryFriendlyByteBuf buf) {
		long origin = buf.readLong();
		int phase = buf.readVarInt();
		List<String> seats = new ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			seats.add(buf.readUtf(64));
		int aiMask = buf.readVarInt();
		int queueSize = buf.readVarInt();
		List<String> queue = new ArrayList<>(queueSize);
		for (int i = 0; i < queueSize; i++)
			queue.add(buf.readUtf(64));
		boolean canManage = buf.readBoolean();
		int gameType = buf.readVarInt();
		int voteCount = buf.readVarInt();
		boolean openHand = buf.readBoolean();
		List<String> seatModes = new ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			seatModes.add(buf.readUtf(32));
		List<Integer> seatFlows = new ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			seatFlows.add(buf.readVarInt());
		return new SyncMessage(origin, phase, seats, aiMask, queue, canManage, gameType, voteCount,
				openHand, seatModes, seatFlows, buf.readVarInt(), buf.readVarInt());
	}

	public static void register() {
		MurmolMod.addNetworkMessage(SyncMessage.TYPE, SyncMessage.STREAM_CODEC, SyncMessage::handleData);
		MurmolMod.addNetworkMessage(ActionMessage.TYPE, ActionMessage.STREAM_CODEC, ActionMessage::handleData);
		MurmolMod.addNetworkMessage(OpenMessage.TYPE, OpenMessage.STREAM_CODEC, OpenMessage::handleData);
	}
}
