package mcr.murmol.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mcr.richi.client.FengPanMusicClient;

/**
 * 风盘点播唱片期间压制原版 BGM：拦截 MusicManager.tick——有残留原版曲立即停掉，
 * 并把下次调度时间向后推（唱片结束后延迟 5 秒恢复原版音乐调度，避免戛然而止立刻切歌）。
 */
@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {
	@Shadow @Final private Minecraft minecraft;
	@Shadow private SoundInstance currentMusic;
	@Shadow private int nextSongDelay;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void murmol$silenceVanillaMusic(CallbackInfo ci) {
		if (!FengPanMusicClient.isPlaying())
			return;
		if (currentMusic != null) {
			minecraft.getSoundManager().stop(currentMusic);
			currentMusic = null;
		}
		nextSongDelay = 100; // 持续后推调度，播放结束后再等 5 秒恢复原版音乐
		ci.cancel();
	}
}
