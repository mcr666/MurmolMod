package mcr.richi.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;

/**
 * 牌局渲染状态包（S→C）：服务端按观看者过滤后的整桌快照，客户端据此全量重建 client-side
 * 显示实体——服务端不再下发带他人牌面的实体 NBT（防抓包看牌）。
 * hands[seat] 为空且 hidden[seat] &gt; 0 = 该家手牌对本次观看者隐藏，客户端渲染为 hidden[seat] 张牌背。
 * phase = WAITING（无对局）时客户端清除该桌显示实体。所有字符串字段判空（writeUtf(null) 会断连）。
	 * tedashi[seat] = 1 表示该家最近一次打牌为手切（0 = 摸切/无）：隐藏手牌的牌背 key 据此错位，
	 * 使手切动画不会永远表现为刚摸的那张消失（摸切观感）。
	 */
	public class MahjongTablePayload {
	public record ViewMessage(long originPacked, int viewerSeat, boolean openHand, int dealAnim,
			String[] hands, int[] hidden, int[] tedashi, String[] rivers, String[] melds,
			int[] riichiRiverIdx, int[] riichiSticks, int[] handsExposed, int[] points,
			String[] names, String[] avatarForms, String[] avatarNames, String[] avatarSkins,
			String doraWall, int revealedIndicators, int wallCount,
			int turnSeat, int round, int roundWind, int honba,
			int selectedSeat, int selectedIndex, int drawnSeat, int phase, String bannerText) implements CustomPacketPayload {

		public static final Type<ViewMessage> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "richi_table"));

		public static final StreamCodec<RegistryFriendlyByteBuf, ViewMessage> STREAM_CODEC = StreamCodec.of(
				MahjongTablePayload::write, MahjongTablePayload::read);

		@Override
		public Type<ViewMessage> type() {
			return TYPE;
		}

		public static void handleData(final ViewMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> mcr.richi.client.MahjongTableClient.apply(message));
			}
		}
	}

	private static String orEmpty(String s) {
		return s == null ? "" : s;
	}

	private static void write(RegistryFriendlyByteBuf buf, ViewMessage msg) {
		buf.writeLong(msg.originPacked());
		buf.writeVarInt(msg.viewerSeat());
		buf.writeBoolean(msg.openHand());
		buf.writeVarInt(msg.dealAnim());
		for (int i = 0; i < 4; i++) {
			buf.writeUtf(orEmpty(msg.hands()[i]), 256);
			buf.writeVarInt(msg.hidden()[i]);
			buf.writeVarInt(msg.tedashi()[i]);
			buf.writeUtf(orEmpty(msg.rivers()[i]), 256);
			buf.writeUtf(orEmpty(msg.melds()[i]), 256);
			buf.writeVarInt(msg.riichiRiverIdx()[i]);
			buf.writeVarInt(msg.riichiSticks()[i]);
			buf.writeVarInt(msg.handsExposed()[i]);
			buf.writeVarInt(msg.points()[i]);
			buf.writeUtf(orEmpty(msg.names()[i]), 64);
			buf.writeUtf(orEmpty(msg.avatarForms()[i]), 64);
			buf.writeUtf(orEmpty(msg.avatarNames()[i]), 64);
			buf.writeUtf(orEmpty(msg.avatarSkins()[i]), 64);
		}
		buf.writeUtf(orEmpty(msg.doraWall()), 256);
		buf.writeVarInt(msg.revealedIndicators());
		buf.writeVarInt(msg.wallCount());
		buf.writeVarInt(msg.turnSeat());
		buf.writeVarInt(msg.round());
		buf.writeVarInt(msg.roundWind());
		buf.writeVarInt(msg.honba());
		buf.writeVarInt(msg.selectedSeat());
		buf.writeVarInt(msg.selectedIndex());
		buf.writeVarInt(msg.drawnSeat());
		buf.writeVarInt(msg.phase());
		buf.writeUtf(orEmpty(msg.bannerText()), 1024);
	}

	private static ViewMessage read(RegistryFriendlyByteBuf buf) {
		long origin = buf.readLong();
		int viewerSeat = buf.readVarInt();
		boolean openHand = buf.readBoolean();
		int dealAnim = buf.readVarInt();
		String[] hands = new String[4];
		int[] hidden = new int[4];
		int[] tedashi = new int[4];
		String[] rivers = new String[4];
		String[] melds = new String[4];
		int[] riichiRiverIdx = new int[4];
		int[] riichiSticks = new int[4];
		int[] handsExposed = new int[4];
		int[] points = new int[4];
		String[] names = new String[4];
		String[] avatarForms = new String[4];
		String[] avatarNames = new String[4];
		String[] avatarSkins = new String[4];
		for (int i = 0; i < 4; i++) {
			hands[i] = buf.readUtf(256);
			hidden[i] = buf.readVarInt();
			tedashi[i] = buf.readVarInt();
			rivers[i] = buf.readUtf(256);
			melds[i] = buf.readUtf(256);
			riichiRiverIdx[i] = buf.readVarInt();
			riichiSticks[i] = buf.readVarInt();
			handsExposed[i] = buf.readVarInt();
			points[i] = buf.readVarInt();
			names[i] = buf.readUtf(64);
			avatarForms[i] = buf.readUtf(64);
			avatarNames[i] = buf.readUtf(64);
			avatarSkins[i] = buf.readUtf(64);
		}
		String doraWall = buf.readUtf(256);
		int revealedIndicators = buf.readVarInt();
		int wallCount = buf.readVarInt();
		int turnSeat = buf.readVarInt();
		int round = buf.readVarInt();
		int roundWind = buf.readVarInt();
		int honba = buf.readVarInt();
		int selectedSeat = buf.readVarInt();
		int selectedIndex = buf.readVarInt();
		int drawnSeat = buf.readVarInt();
		int phase = buf.readVarInt();
		String bannerText = buf.readUtf(1024);
		return new ViewMessage(origin, viewerSeat, openHand, dealAnim, hands, hidden, tedashi, rivers, melds,
				riichiRiverIdx, riichiSticks, handsExposed, points, names,
				avatarForms, avatarNames, avatarSkins, doraWall,
				revealedIndicators, wallCount, turnSeat, round, roundWind, honba,
				selectedSeat, selectedIndex, drawnSeat, phase, bannerText);
	}

	public static void register() {
		MurmolMod.addNetworkMessage(ViewMessage.TYPE, ViewMessage.STREAM_CODEC, ViewMessage::handleData);
	}
}
