package mcr.richi;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import mcr.murmol.entity.MurmolNpcEntity;
import mcr.richi.block.FengPanBlock;
import mcr.richi.game.MahjongLobby;
import mcr.richi.game.MahjongTileNotation;
import mcr.richi.game.RichiTableManager;
import mcr.richi.game.RichiTableState;

import net.minecraft.commands.SharedSuggestionProvider;

/**
 * 麻雀指令：
 * /murmol mahjong refresh —— 按服务端牌局状态重建所有桌面渲染（实时渲染的兜底手段）；
 * /murmol mahjong quit    —— 退出最近的牌局（清除状态与桌面渲染）；
 * /murmol npc join <id>   —— Murmol NPC 加入 16 格内最近牌桌等候队列（右键[接受]触发）；
 * /murmol npc decline     —— 右键[拒绝]。
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
							.then(Commands.literal("replace")
									// 替换对局中指定玩家的手牌（4 级权限）：hand=牌谱记法（如 123m123p123456s11z），
									// seat 可选 e/s/w/n（东南西北），留空为自己
									.requires(src -> src.hasPermission(4))
									.then(Commands.argument("hand", StringArgumentType.string())
											.executes(ctx -> replaceHand(ctx, -1))
											.then(Commands.argument("seat", StringArgumentType.word())
													.suggests((c, b) -> SharedSuggestionProvider.suggest(
															new String[] { "e", "s", "w", "n" }, b))
													.executes(ctx -> replaceHand(ctx, seatFromArg(ctx))))))
							.then(Commands.literal("rank")
									// 段位排行榜（天凤 pt）：无参 = 前 10 位；带玩家名 = 查询该玩家 pt 与总排名（离线可查）
									.executes(ctx -> {
										ctx.getSource().sendSuccess(
												() -> Component.translatable("message.richi.rank_header"), false);
										var top = mcr.richi.game.MahjongRankStore.top(ctx.getSource().getLevel(), 10);
										if (top.isEmpty()) {
											ctx.getSource().sendSuccess(
													() -> Component.translatable("message.richi.rank_none"), false);
											return 0;
										}
										int rank = 0;
										for (var e : top) {
											final int r = ++rank;
											ctx.getSource().sendSuccess(() -> Component.translatable(
													"message.richi.rank_line", r, e.name(),
													String.format("%+.1f", e.pt()), e.games()), false);
										}
										return top.size();
									})
									.then(Commands.argument("player", StringArgumentType.word())
											.executes(ctx -> {
												String name = StringArgumentType.getString(ctx, "player");
												var e = mcr.richi.game.MahjongRankStore.find(
														ctx.getSource().getLevel(), name);
												if (e == null) {
													ctx.getSource().sendFailure(
															Component.translatable("message.richi.rank_notfound", name));
													return 0;
												}
												int rank = mcr.richi.game.MahjongRankStore.rankOf(
														ctx.getSource().getLevel(), e);
												ctx.getSource().sendSuccess(() -> Component.translatable(
														"message.richi.rank_query", e.name(),
														String.format("%+.1f", e.pt()), rank, e.games()), false);
												return 1;
											})))
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
							})))
					.then(Commands.literal("npc")
							.then(Commands.literal("join")
									.then(Commands.argument("npcId", IntegerArgumentType.integer(1))
											.executes(ctx -> {
												ServerPlayer player = ctx.getSource().getPlayerOrException();
												int id = IntegerArgumentType.getInteger(ctx, "npcId");
												if (!(player.level().getEntity(id) instanceof MurmolNpcEntity npc)) {
													ctx.getSource().sendFailure(Component.literal("§c找不到该 Murmol"));
													return 0;
												}
												ServerLevel level = player.serverLevel();
												BlockPos nearest = RichiTableManager.nearest(level, npc.position(), 16);
												if (nearest == null
														|| !MahjongLobby.npcJoinTable(level, npc, nearest)) {
													ctx.getSource().sendFailure(
															Component.literal("§c16 格内没有可加入的牌桌等候队列"));
													return 0;
												}
												ctx.getSource().sendSuccess(
														() -> Component.literal("§6<幻星麻雀>§r Murmol 已加入等候队列"), false);
												return 1;
											})))
							.then(Commands.literal("decline")
									.executes(ctx -> {
										ctx.getSource().sendSuccess(
												() -> Component.literal("§6<Murmol>§r 下次再约吧～"), false);
										return 1;
									}))));
	}

	/** replace 指令座位参数解析：e/s/w/n → 0..3，其余抛异常 */
	private static int seatFromArg(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		String s = StringArgumentType.getString(ctx, "seat").toLowerCase();
		int seat = switch (s) {
			case "e" -> 0;
			case "s" -> 1;
			case "w" -> 2;
			case "n" -> 3;
			default -> -1;
		};
		if (seat < 0)
			throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
					Component.literal("§c座位须为 e/s/w/n")).create();
		return seat;
	}

	/** replace 指令执行：seatArg < 0 = 自己座位 */
	private static int replaceHand(CommandContext<CommandSourceStack> ctx, int seatArg)
			throws CommandSyntaxException {
		ServerLevel level = ctx.getSource().getLevel();
		var player = ctx.getSource().getPlayerOrException();
		BlockPos nearest = RichiTableManager.nearest(level, player.position(), 16);
		if (nearest == null) {
			ctx.getSource().sendFailure(Component.literal("§c16 格内没有牌桌"));
			return 0;
		}
		RichiTableState st = RichiTableManager.get(nearest);
		var game = RichiTableManager.getGame(nearest);
		if (st == null || game == null || st.phase != RichiTableState.PHASE_PLAYING) {
			ctx.getSource().sendFailure(Component.literal("§c该牌桌当前没有进行中的对局"));
			return 0;
		}
		int seat = seatArg;
		if (seat < 0) {
			seat = -1;
			for (int i = 0; i < 4; i++)
				if (player.getStringUUID().equals(st.players[i]))
					seat = i;
			if (seat < 0) {
				ctx.getSource().sendFailure(Component.literal("§c你不在这场对局中，请指定座位 e/s/w/n"));
				return 0;
			}
		}
		java.util.List<Integer> codes;
		try {
			codes = MahjongTileNotation.parse(StringArgumentType.getString(ctx, "hand"));
		} catch (IllegalArgumentException e) {
			ctx.getSource().sendFailure(Component.literal("§c牌谱记法无效: " + e.getMessage()));
			return 0;
		}
		if (game.replaceHand(seat, codes)) {
			int replaced = seat;
			ctx.getSource().sendSuccess(
					() -> Component.literal("§6<幻星麻雀>§r 已替换 " + seatName(replaced) + " 家手牌"), true);
			return 1;
		}
		ctx.getSource().sendFailure(Component.literal("§c手牌张数须为 1..14"));
		return 0;
	}

	/** 座位 0..3 → 座位名（東南西北，与 RichiTableState 家序一致） */
	private static String seatName(int seat) {
		return new String[] { "東", "南", "西", "北" }[seat];
	}
}
