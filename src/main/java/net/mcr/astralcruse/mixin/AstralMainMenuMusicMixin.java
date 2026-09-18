package net.mcr.astralcruse.mixin;

import net.mcr.astralcruse.init.AstralCruseModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class AstralMainMenuMusicMixin {
    private static final Music ASTRAL_CRUSE$MAIN_MENU_MUSIC =
        new Music(AstralCruseModSounds.ASTRAL_BOOK_GUI, 40, 40, true);

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void astralCruse$useCustomMainMenuMusic(CallbackInfoReturnable<Music> cir) {
        // 配置关闭时保留原版音乐（与标题界面共用同一选项）
        if (!net.mcr.murmol.MurmolModConfig.MODIFY_TITLE_SCREEN.get()) {
            return;
        }
        Minecraft minecraft = (Minecraft) (Object) this;
        if (minecraft.player == null) {
            cir.setReturnValue(ASTRAL_CRUSE$MAIN_MENU_MUSIC);
        } else {
            minecraft.getMusicManager().stopPlaying(ASTRAL_CRUSE$MAIN_MENU_MUSIC);
        }
    }
}
