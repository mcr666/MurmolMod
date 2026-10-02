package mcr.richi.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;

/**
 * 结算界面推送（S→C）：
 * <ul>
 * <li>mode=0 和牌结算：和牌者 + 荣和/自摸 + 役种翻符 + 手牌/副露牌面（客户端直绘贴图）+ 四家点数</li>
 * <li>mode=1 终局结算：四家最终点数排名</li>
 * <li>mode=2 关闭：服务端推进下一局时关闭已打开的结算界面</li>
 * </ul>
 * 所有字符串字段可能为空，读写均做判空（writeUtf(null) 会断连）。
 */
public class MahjongSettlementPayload {
	/** mode：和牌结算 / 终局结算 / 关闭 */
	public static final int MODE_HAND_WIN = 0, MODE_FINAL = 1, MODE_CLOSE = 2;

	/** 客户端缓存：最近一次收到的结算（打开结算界面） */
	public static volatile Settlement last;

	public record Settlement(int mode, long originPacked, String winnerName, String winType, String yakuInfo,
			String handCsv, String meldsGroups, int winTile, String uraCsv, String doraCsv, String yakuList,
			int han, int fu, int gain, String[] names, int[] points) {
	}

	public record SettlementMessage(int mode, long originPacked, String winnerName, String winType, String yakuInfo,
			String handCsv, String meldsGroups, int winTile, String uraCsv, String doraCsv, String yakuList,
			int han, int fu, int gain,
			java.util.List<String> names, java.util.List<Integer> points) implements CustomPacketPayload {
		public static final Type<SettlementMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_settlement"));

		public static final StreamCodec<RegistryFriendlyByteBuf, SettlementMessage> STREAM_CODEC = StreamCodec.of(
				MahjongSettlementPayload::write, MahjongSettlementPayload::read);

		@Override
		public Type<SettlementMessage> type() {
			return TYPE;
		}

		public static void handleData(final SettlementMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					if (message.mode() == MODE_CLOSE) {
						mcr.richi.client.MahjongSettlementScreen.closeIfOpen();
						return;
					}
					String[] ns = new String[4];
					for (int i = 0; i < 4; i++)
						ns[i] = message.names().get(i) == null ? "" : message.names().get(i);
					int[] pts = new int[4];
					for (int i = 0; i < 4; i++)
						pts[i] = message.points().get(i);
					MahjongSettlementPayload.last = new Settlement(message.mode(), message.originPacked(),
							orEmpty(message.winnerName()), orEmpty(message.winType()), orEmpty(message.yakuInfo()),
							orEmpty(message.handCsv()), orEmpty(message.meldsGroups()),
							message.winTile(), orEmpty(message.uraCsv()), orEmpty(message.doraCsv()),
							orEmpty(message.yakuList()), message.han(), message.fu(), message.gain(), ns, pts);
					mcr.richi.client.MahjongSettlementScreen.open();
				});
			}
		}
	}

	private static String orEmpty(String s) {
		return s == null ? "" : s;
	}

	private static void write(RegistryFriendlyByteBuf buf, SettlementMessage msg) {
		buf.writeVarInt(msg.mode());
		buf.writeLong(msg.originPacked());
		buf.writeUtf(orEmpty(msg.winnerName()), 64);
		buf.writeUtf(orEmpty(msg.winType()), 16);
		buf.writeUtf(orEmpty(msg.yakuInfo()), 256);
		buf.writeUtf(orEmpty(msg.handCsv()), 256);
		buf.writeUtf(orEmpty(msg.meldsGroups()), 256);
		buf.writeVarInt(msg.winTile());
		buf.writeUtf(orEmpty(msg.uraCsv()), 64);
		buf.writeUtf(orEmpty(msg.doraCsv()), 64);
		buf.writeUtf(orEmpty(msg.yakuList()), 512);
		buf.writeVarInt(msg.han());
		buf.writeVarInt(msg.fu());
		buf.writeVarInt(msg.gain());
		for (int i = 0; i < 4; i++)
			buf.writeUtf(orEmpty(msg.names().get(i)), 64);
		for (int i = 0; i < 4; i++)
			buf.writeVarInt(msg.points().get(i));
	}

	private static SettlementMessage read(RegistryFriendlyByteBuf buf) {
		int mode = buf.readVarInt();
		long origin = buf.readLong();
		String winner = buf.readUtf(64);
		String winType = buf.readUtf(16);
		String yaku = buf.readUtf(256);
		String hand = buf.readUtf(256);
		String melds = buf.readUtf(256);
		int winTile = buf.readVarInt();
		String ura = buf.readUtf(64);
		String dora = buf.readUtf(64);
		String yakuList = buf.readUtf(512);
		int han = buf.readVarInt();
		int fu = buf.readVarInt();
		int gain = buf.readVarInt();
		java.util.List<String> names = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			names.add(buf.readUtf(64));
		java.util.List<Integer> points = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++)
			points.add(buf.readVarInt());
		return new SettlementMessage(mode, origin, winner, winType, yaku, hand, melds, winTile, ura, dora, yakuList,
				han, fu, gain, names, points);
	}

	/** 服务端便捷方法：向某玩家推结算界面 */
	public static void sendTo(net.minecraft.server.level.ServerPlayer player, Settlement s) {
		net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
				new SettlementMessage(s.mode(), s.originPacked(), s.winnerName(), s.winType(), s.yakuInfo(),
						s.handCsv(), s.meldsGroups(), s.winTile(), s.uraCsv(), s.doraCsv(), s.yakuList(),
						s.han(), s.fu(), s.gain(),
						java.util.Arrays.asList(s.names()),
						java.util.List.of(s.points[0], s.points[1], s.points[2], s.points[3])));
	}

	/** 服务端便捷方法：关闭某玩家已打开的结算界面（推进下一局时） */
	public static void sendClose(net.minecraft.server.level.ServerPlayer player) {
		net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
				new SettlementMessage(MODE_CLOSE, 0, "", "", "", "", "", -1, "", "", "", 0, 0, 0,
						java.util.List.of("", "", "", ""), java.util.List.of(0, 0, 0, 0)));
	}

	public static void register() {
		MurmolMod.addNetworkMessage(SettlementMessage.TYPE, SettlementMessage.STREAM_CODEC,
				SettlementMessage::handleData);
	}
}
