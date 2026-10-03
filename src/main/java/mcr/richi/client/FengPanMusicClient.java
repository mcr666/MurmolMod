package mcr.richi.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

/**
 * 风盘点播唱片（客户端）：在风盘位置播放唱片音效（RECORDS 声道、位置衰减），
 * 收包时立即截断原版 BGM；播放期间由 MusicManagerMixin 持续压制原版音乐（含菜单曲），
 * 到达唱片时长后自动恢复原版音乐调度。唱片不消耗，仅提示一次曲名（绿色）。
 */
public class FengPanMusicClient {
	/** 当前点播的截止时间（System.currentTimeMillis()，早于该时间 = 正在播放） */
	private static volatile long endAtMillis;

	/** 播放期间返回 true：MusicManager.tick 被拦截，不再调度原版 BGM */
	public static boolean isPlaying() {
		return System.currentTimeMillis() < endAtMillis;
	}

	public static void play(mcr.richi.network.FengPanMusicPayload.PlayRecord msg) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null)
			return;
		// 立即截断正在播放的原版 BGM
		mc.getMusicManager().stopPlaying();
		// 唱片内容：RECORDS 声道、线性衰减，定位在风盘中心
		mc.getSoundManager().play(new SimpleSoundInstance(msg.sound(), SoundSource.RECORDS, 1.0f, 1.0f,
				net.minecraft.util.RandomSource.create(), false, 0, SoundInstance.Attenuation.LINEAR,
				msg.x(), msg.y(), msg.z(), false));
		endAtMillis = System.currentTimeMillis() + (long) (msg.durationSec() * 1000.0f);
		// 周围玩家显示一次"正在播放xxx"（曲名绿色）
		LocalPlayer player = mc.player;
		if (player != null)
			player.displayClientMessage(
					Component.translatable("message.richi.playing_disc",
							msg.name().copy().withStyle(ChatFormatting.GREEN)),
					true);
	}
}
