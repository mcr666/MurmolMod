package mcr.richi;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import mcr.richi.block.FengPanBlock;
import mcr.richi.game.RichiTableManager;

/**
 * 麻将指令：
 * /murmol mahjong refresh —— 按服务端牌局状态重建所有桌面渲染（实时渲染的兜底手段）；
 * /murmol mahjong quit    —— 退出最近的牌局（清除状态与桌面渲染）。
 */
@EventBusSubscriber
public class MahjongCommands {
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("murmol")
				.then(Commands.literal("mahjong")
						.then(Commands.literal("refresh")
								.executes(ctx -> {
									MahjongTicker.clear();
									ServerLevel level = ctx.getSource().getLevel();
									var origins = RichiTableManager.originsIn(level);
									for (BlockPos origin : origins)
										FengPanBlock.rerenderTable(level, origin);
									final int n = origins.size();
									ctx.getSource().sendSuccess(
											() -> Component.translatable("message.richi.refresh_done", n), true);
									return n;
								}))
						.then(Commands.literal("clean")
								.executes(ctx -> {
									// 清理最近的风盘：删对局/对局中不可清 + 清实体 + 通知客户端清 AI 形象
									ServerLevel level = ctx.getSource().getLevel();
									var player = ctx.getSource().getPlayerOrException();
									BlockPos nearest = RichiTableManager.nearest(level, player.position(), 16);
									if (nearest == null) {
										ctx.getSource().sendFailure(Component.translatable("message.richi.quit_none"));
										return 0;
									}
									var st = RichiTableManager.get(nearest);
									if (st != null && st.phase == mcr.richi.game.RichiTableState.PHASE_PLAYING) {
										ctx.getSource().sendFailure(
												Component.translatable("message.richi.fengpan_locked"));
										return 0;
									}
									MahjongTicker.cancel(nearest);
									RichiTableManager.remove(nearest);
									FengPanBlock.clearDisplays(level, nearest);
									FengPanBlock.setGameActive(level, nearest, false);
									mcr.richi.game.RichiTableSync.broadcastGone(level, nearest); // 清客户端 AI 假玩家/盔甲架
									ctx.getSource().sendSuccess(
											() -> Component.translatable("message.richi.clean_done",
													nearest.toShortString()), true);
									return 1;
								}))
							.then(Commands.literal("quit")
								.executes(ctx -> {
									ServerLevel level = ctx.getSource().getLevel();
									var player = ctx.getSource().getPlayerOrException();
									BlockPos nearest = RichiTableManager.nearest(level, player.position(), 16);
									if (nearest == null) {
										ctx.getSource().sendFailure(Component.translatable("message.richi.quit_none"));
										return 0;
									}
									RichiTableManager.remove(nearest);
									FengPanBlock.clearDisplays(level, nearest);
									FengPanBlock.setGameActive(level, nearest, false); // 恢复风盘占位模型
									MahjongTicker.clear(); // 取消未完成的发牌任务
									ctx.getSource().sendSuccess(
											() -> Component.translatable("message.richi.quit_done",
													nearest.toShortString()), true);
									return 1;
								}))));
	}
}
