package mcr.richi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashSet;
import java.util.Set;

import mcr.murmol.init.MurmolModParticleTypes;
import mcr.richi.MahjongItems;
import mcr.richi.MahjongTileItem;
import mcr.richi.block.FengPanBlock;

/**
 * 麻将内容客户端注册：为麻将牌物品注册 "code" 物品属性，
 * 配合模型 overrides（assets/richi/models/item/mahjong_tile.json）实现按 NBT 牌面切换模型。
 * 注意：无组件时必须返回 -1（不匹配任何 override，显示背面），不能钳到 0（0 是红五万）。
 * 另：手持风盘时用 astral_burst 粒子绕桌心围圈标记桌位（类原版屏障的标记显示）。
 */
@EventBusSubscriber(Dist.CLIENT)
public class MahjongClient {
	/** 客户端 tick 计数（粒子圈节流用） */
	private static int tickCounter;
	/** 粒子圈扫描半径（格，水平） */
	private static final int SCAN_RADIUS = 10;
	/** 每张桌子的粒子圈点数 */
	private static final int CIRCLE_POINTS = 12;
	/** 粒子圈半径（格） */
	private static final double CIRCLE_RADIUS = 0.5;

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ItemProperties.register(
					MahjongItems.MAHJONG_TILE.get(),
					ResourceLocation.withDefaultNamespace("code"),
					(stack, level, entity, seed) -> MahjongTileItem.getCode(stack));
		});
	}

	/** 手持风盘时，扫描附近风盘并在每个桌面中心外围生成 astral_burst 粒子圈 */
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		tickCounter++;
		MahjongTableClient.tick(); // 牌桌发牌/倒牌动画调度（逐 tick 驱动）
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ClientLevel level = minecraft.level;
		if (player == null || level == null)
			return;
		// 每 4 tick 刷新一轮粒子圈（粒子本身有寿命，持续补充保持可见）
		if (tickCounter % 4 != 0)
			return;
		boolean holding = player.getMainHandItem().is(MahjongItems.FENG_PAN.get().asItem())
				|| player.getOffhandItem().is(MahjongItems.FENG_PAN.get().asItem());
		if (!holding)
			return;
		BlockPos base = player.blockPosition();
		Set<BlockPos> seenOrigins = new HashSet<>();
		for (BlockPos pos : BlockPos.betweenClosed(
				base.offset(-SCAN_RADIUS, -2, -SCAN_RADIUS), base.offset(SCAN_RADIUS, 2, SCAN_RADIUS))) {
			if (!(level.getBlockState(pos).getBlock() instanceof FengPanBlock))
				continue;
			// 同一张桌子由 4 个方块拼成，按原点去重，只画一圈
			BlockPos origin = FengPanBlock.getOrigin(level.getBlockState(pos), pos);
			if (!seenOrigins.add(origin))
				continue;
			Vec3 center = FengPanBlock.getTableCenter(level.getBlockState(pos), pos);
			for (int i = 0; i < CIRCLE_POINTS; i++) {
				double angle = Math.PI * 2 * i / CIRCLE_POINTS;
				level.addParticle(MurmolModParticleTypes.ASTRAL_BURST.get(),
						center.x + Math.cos(angle) * CIRCLE_RADIUS,
						center.y,
						center.z + Math.sin(angle) * CIRCLE_RADIUS,
						0, 0, 0);
			}
		}
	}
}
