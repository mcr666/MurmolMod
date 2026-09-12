package net.mcr.astralcruse.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Screen.class)
public abstract class AstralMenuPanoramaMixin {
    @ModifyArg(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/CubeMap;<init>(Lnet/minecraft/resources/ResourceLocation;)V"
        ),
        index = 0
    )
    private static ResourceLocation astralCruse$useCustomMenuPanorama(ResourceLocation original) {
        return ResourceLocation.fromNamespaceAndPath(
            "astral_cruse",
            "textures/gui/title/background/panorama"
        );
    }
}
