package mcr.murmol.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralFormManager;
import mcr.murmol.feral.FeralForms;

import java.util.Collection;

/**
 * 模组命令注册。/murmol tf <form> [targets] 切换目标玩家形态（省略 targets 时变形自己），
 * 走 FeralFormManager 完整变形流程（音效、成就、效果）。
 */
@EventBusSubscriber
public class MurmolCommands {

	private static final SuggestionProvider<CommandSourceStack> FORM_SUGGESTIONS = (context, builder) -> {
		for (FeralForm form : FeralForms.all())
			builder.suggest(form.getId());
		return builder.buildFuture();
	};

	private static final SuggestionProvider<CommandSourceStack> BOOK_ENTRY_SUGGESTIONS = (context, builder) -> {
		builder.suggest("all");
		for (FeralForm form : FeralForms.all())
			builder.suggest(form.getId());
		return builder.buildFuture();
	};

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("murmol")
			.requires(source -> source.hasPermission(2))
			.then(Commands.literal("tf")
				.then(Commands.argument("form", StringArgumentType.word())
					.suggests(FORM_SUGGESTIONS)
					.executes(MurmolCommands::executeTransformSelf)
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(MurmolCommands::executeTransform))))
			.then(Commands.literal("untf")
				.executes(MurmolCommands::executeUntfSelf)
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(MurmolCommands::executeUntf)))
			.then(Commands.literal("book")
				.then(Commands.literal("unlock")
					.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("entry", StringArgumentType.word())
							.suggests(BOOK_ENTRY_SUGGESTIONS)
							.executes(context -> executeBook(context, true)))))
				.then(Commands.literal("lock")
					.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("entry", StringArgumentType.word())
							.suggests(BOOK_ENTRY_SUGGESTIONS)
							.executes(context -> executeBook(context, false))))))
			.then(Commands.literal("soulbind")
				.then(Commands.literal("true")
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(context -> executeSoulbind(context, true))))
				.then(Commands.literal("false")
					.then(Commands.argument("targets", EntityArgument.players())
						.executes(context -> executeSoulbind(context, false))))));
	}

	/** /murmol book unlock|lock <targets> <form|all>：解锁/锁定指定玩家的形态条目（星象仪目录可见性） */
	private static int executeBook(CommandContext<CommandSourceStack> context, boolean unlock) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		String entry = StringArgumentType.getString(context, "entry");
		java.util.List<String> ids = new java.util.ArrayList<>();
		if (entry.equals("all")) {
			for (FeralForm form : FeralForms.all())
				if (form != FeralForms.HUMAN) ids.add(form.getId());
		} else {
			boolean known = false;
			for (FeralForm form : FeralForms.all())
				if (form.getId().equals(entry)) known = true;
			if (!known) {
				context.getSource().sendFailure(Component.literal("未知形态: " + entry));
				return 0;
			}
			ids.add(entry);
		}
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
		int changed = 0;
		for (ServerPlayer player : targets) {
			for (String id : ids) {
				var advHolder = player.server.getAdvancements()
						.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(mcr.murmol.MurmolMod.MODID, "unlock/" + id));
				if (advHolder == null) continue;
				var progress = player.getAdvancements().getOrStartProgress(advHolder);
				if (unlock) {
					if (!progress.isDone()) {
						for (String c : progress.getRemainingCriteria())
							player.getAdvancements().award(advHolder, c);
						changed++;
					}
				} else {
					if (progress.isDone()) {
						for (String c : progress.getCompletedCriteria())
							player.getAdvancements().revoke(advHolder, c);
						changed++;
					}
				}
				FeralFormManager.syncUnlockState(player);
			}
		}
		final int c = changed;
		final int pCount = targets.size();
		context.getSource().sendSuccess(() -> Component.literal(
				(unlock ? "已解锁 " : "已锁定 ") + c + " 个形态条目（" + pCount + " 名玩家）"), true);
		return c;
	}

	/** /murmol tf <form>：对命令输入者自身变形 */
	private static int executeTransformSelf(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer self = context.getSource().getPlayerOrException();
		FeralForm form = resolveForm(context, StringArgumentType.getString(context, "form"));
		if (form == null)
			return 0;
		FeralFormManager.transformFromItem(self.level(), self.getX(), self.getY(), self.getZ(), self, form, ItemStack.EMPTY, true);
		context.getSource().sendSuccess(() -> Component.literal("transformed self to: " + form.getId()), true);
		return 1;
	}

	/** /murmol untf [targets]：等效于 /murmol tf human，变回人类形态 */
	private static int executeUntfSelf(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer self = context.getSource().getPlayerOrException();
		FeralFormManager.transformFromItem(self.level(), self.getX(), self.getY(), self.getZ(), self, FeralForms.HUMAN, ItemStack.EMPTY, true);
		context.getSource().sendSuccess(() -> Component.literal("transformed self to: human"), true);
		return 1;
	}

	private static int executeUntf(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
		for (ServerPlayer player : targets) {
			FeralFormManager.transformFromItem(player.level(), player.getX(), player.getY(), player.getZ(), player, FeralForms.HUMAN, ItemStack.EMPTY, true);
		}
		context.getSource().sendSuccess(() -> Component.literal("transformed " + targets.size() + " player(s) to: human"), true);
		return targets.size();
	}

	private static FeralForm resolveForm(CommandContext<CommandSourceStack> context, String id) {
		for (FeralForm f : FeralForms.all()) {
			if (f.getId().equals(id))
				return f;
		}
		context.getSource().sendFailure(Component.literal("unknown form: " + id));
		return null;
	}

	private static int executeTransform(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		FeralForm form = resolveForm(context, StringArgumentType.getString(context, "form"));
		if (form == null)
			return 0;
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
		final FeralForm target = form;
		for (ServerPlayer player : targets) {
			FeralFormManager.transformFromItem(player.level(), player.getX(), player.getY(), player.getZ(), player, form, ItemStack.EMPTY, true);
		}
		context.getSource().sendSuccess(() -> Component.literal("transformed " + targets.size() + " player(s) to: " + target.getId()), true);
		return targets.size();
	}

	/**
	 * /murmol soulbind true|false <players>：
	 * true = 把目标玩家绑定到其当前形态（须为野性形态、且未绑定；等同使用塑魂之约，但不消耗物品）；
	 * false = 解除目标玩家的塑魂之约绑定。
	 */
	private static int executeSoulbind(CommandContext<CommandSourceStack> context, boolean bind) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
		int changed = 0;
		for (ServerPlayer player : targets) {
			if (bind) {
				FeralForm form = FeralFormManager.getForm(player);
				if (!form.isFeral() || FeralFormManager.isSoulbound(player))
					continue;
				if (FeralFormManager.bindSoul(player, form)) {
					changed++;
					player.displayClientMessage(Component.translatable("message.murmol.soulbind.bound_cmd",
							player.getDisplayName(), form.getDisplayName()), false);
				}
			} else {
				if (!FeralFormManager.isSoulbound(player))
					continue;
				FeralFormManager.unbindSoul(player);
				changed++;
				player.displayClientMessage(Component.translatable("message.murmol.soulbind.unbound_cmd",
						player.getDisplayName()), false);
			}
		}
		final int c = changed;
		context.getSource().sendSuccess(() -> Component.literal((bind ? "soulbound " : "unbound ") + c + " player(s)"), true);
		return changed;
	}
}
