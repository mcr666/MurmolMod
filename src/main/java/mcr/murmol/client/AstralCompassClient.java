package mcr.murmol.client;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import mcr.murmol.init.MurmolModItems;
import mcr.murmol.network.AstralCompassPayload;

/**
 * 灵气指南针客户端：为物品注册原版 "angle" 物品属性，复用
 * CompassItemPropertyFunction（角度→指针帧映射；目标维度不匹配/无目标时乱转）。
 * 目标点由服务端按 /locate 同款搜索后通过 AstralCompassPayload 同步，
 * 客户端只读缓存（不扫描区块，避免未加载群系找不到）。
 */
@EventBusSubscriber(Dist.CLIENT)
public class AstralCompassClient {

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> ItemProperties.register(
				MurmolModItems.ASTRAL_COMPASS.get(),
				ResourceLocation.withDefaultNamespace("angle"),
				new CompassItemPropertyFunction(AstralCompassClient::findTarget)));
	}

	@Nullable
	private static GlobalPos findTarget(ClientLevel level, ItemStack itemstack, Entity entity) {
		return AstralCompassPayload.clientTarget;
	}
}
