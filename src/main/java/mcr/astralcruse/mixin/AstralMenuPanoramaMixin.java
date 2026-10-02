package mcr.astralcruse.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 主菜单/标题界面全景图替换。
 *
 * 兼容性关键：不能在 Screen 的静态初始化（&lt;clinit&gt;）中读取客户端配置——
 * Create 等 mod 会在 mod 构造阶段触发 Screen 类初始化，此时客户端配置尚未加载，
 * ModConfigSpec.get() 会抛出 IllegalStateException 导致启动崩溃
 * （表现为 NoClassDefFoundError: Screen，且日志看不出与 murmol 的直接关联）。
 * 这里改为在 CubeMap.render 渲染时动态替换纹理路径，渲染必然发生在配置加载之后。
 */
@Mixin(CubeMap.class)
public abstract class AstralMenuPanoramaMixin {
    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"
        ),
        index = 1
    )
    private ResourceLocation astralCruse$useCustomMenuPanorama(int shaderTexture, ResourceLocation original) {
        String prefix = "textures/gui/title/background/panorama";
        if (original.getNamespace().equals("minecraft")
            && original.getPath().startsWith(prefix)
            && mcr.murmol.MurmolModConfig.MODIFY_TITLE_SCREEN.get()) {
            // 保留 _0.._5 面序号后缀，否则贴图路径不存在会渲染成紫黑格
            return ResourceLocation.fromNamespaceAndPath(
                "astral_cruse",
                prefix + original.getPath().substring(prefix.length())
            );
        }
        return original;
    }
}
