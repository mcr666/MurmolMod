package net.mcr.astralcruse.mixin;

import net.mcr.murmol.init.MurmolModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class AstralMainMenuMusicMixin {
    // 延迟初始化：不能在静态字段中引用 MurmulModSounds，
    // 否则会在 Minecraft 类初始化期间提前加载注册器导致闪退
    private static Music astralCruse$mainMenuMusic() {
        return new Music(MurmolModSounds.ASTRAL_AFFECTION, 40, 40, true);
    }

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void astralCruse$useCustomMainMenuMusic(CallbackInfoReturnable<Music> cir) {
        // 配置关闭时保留原版音乐（与标题界面共用同一选项）
        if (!net.mcr.murmol.MurmolModConfig.MODIFY_TITLE_SCREEN.get()) {
            return;
        }
        Minecraft minecraft = (Minecraft) (Object) this;
        if (minecraft.player == null) {
            cir.setReturnValue(astralCruse$mainMenuMusic());
        } else {
            minecraft.getMusicManager().stopPlaying(astralCruse$mainMenuMusic());
        }
    }
}
