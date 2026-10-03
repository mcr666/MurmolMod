package mcr.richi.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import mcr.murmol.MurmolMod;

/**
 * 风盘点播唱片（S -> C）：任意唱片右击风盘时，服务端向周围玩家广播——
 * 在该位置播放唱片音乐（不消耗唱片），并临时压制原版 BGM 直到播放结束。
 * sound = 唱片音效 id，durationSec = 唱片时长（秒，来自 JukeboxSong），name = 曲名（客户端提示用）。
 */
public class FengPanMusicPayload {
	public record PlayRecord(double x, double y, double z, ResourceLocation sound,
			float durationSec, Component name) implements CustomPacketPayload {
		public static final Type<PlayRecord> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(MurmolMod.MODID, "fengpan_music"));

		/** Component 的 StreamCodec（RegistryFriendlyByteBuf 无 writeComponent，走 Codec 编码） */
		static final StreamCodec<io.netty.buffer.ByteBuf, Component> NAME_CODEC = ByteBufCodecs
				.fromCodec(ComponentSerialization.CODEC);

		public static final StreamCodec<RegistryFriendlyByteBuf, PlayRecord> STREAM_CODEC = StreamCodec.of(
				(RegistryFriendlyByteBuf buf, PlayRecord msg) -> {
					buf.writeDouble(msg.x());
					buf.writeDouble(msg.y());
					buf.writeDouble(msg.z());
					buf.writeResourceLocation(msg.sound());
					buf.writeFloat(msg.durationSec());
					NAME_CODEC.encode(buf, msg.name());
				},
				(RegistryFriendlyByteBuf buf) -> new PlayRecord(buf.readDouble(), buf.readDouble(), buf.readDouble(),
						buf.readResourceLocation(), buf.readFloat(), NAME_CODEC.decode(buf)));

		@Override
		public Type<PlayRecord> type() {
			return TYPE;
		}

		public static void handleData(final PlayRecord message, final IPayloadContext context) {
			// 客户端专属处理（FengPanMusicClient 引用客户端类，服务端不加载）
			context.enqueueWork(() -> mcr.richi.client.FengPanMusicClient.play(message));
		}
	}

	public static void register() {
		MurmolMod.addNetworkMessage(PlayRecord.TYPE, PlayRecord.STREAM_CODEC, PlayRecord::handleData);
	}
}
