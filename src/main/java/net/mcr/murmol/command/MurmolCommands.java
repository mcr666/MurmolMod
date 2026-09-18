package net.mcr.murmol.command;

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

import net.mcr.murmol.feral.FeralForm;
import net.mcr.murmol.feral.FeralFormManager;
import net.mcr.murmol.feral.FeralForms;

import java.util.Collection;

/**
 * 模组命令注册。/murmol tf <targets> <form> 切换目标玩家形态，
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
				.then(Commands.argument("targets", EntityArgument.players())
					.then(Commands.argument("form", StringArgumentType.word())
						.suggests(FORM_SUGGESTIONS)
						.executes(MurmolCommands::executeTransform))))
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
							.executes(context -> executeBook(context, false)))))));
	}

	/** /murmol book unlock|lock <targets> <entry|all>：授予/撤销手册形态条目的解锁进度 */
	private static int executeBook(CommandContext<CommandSourceStack> context, boolean unlock) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		String entry = StringArgumentType.getString(context, "entry");
		java.util.List<String> ids = new java.util.ArrayList<>();
		if (entry.equals("all")) {
			for (FeralForm form : FeralForms.all())
				ids.add(form.getId());
		} else {
			boolean known = false;
			for (FeralForm form : FeralForms.all())
				if (form.getId().equals(entry))
					known = true;
			if (!known) {
				context.getSource().sendFailure(Component.literal("unknown book entry: " + entry));
				return 0;
			}
			ids.add(entry);
		}
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
		int changed = 0;
		for (ServerPlayer player : targets) {
			for (String id : ids) {
				var holder = player.server.getAdvancements()
						.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.mcr.murmol.MurmolMod.MODID, "unlock/" + id));
				if (holder.isEmpty())
					continue;
				var progress = player.getAdvancements().getAdvancement(holder.get());
				if (progress == null)
					continue;
				if (unlock) {
					if (!progress.isDone())
						progress.grantProgress("impossible");
				} else {
					if (progress.isDone())
						progress.revokeProgress("impossible");
				}
				changed++;
			}
		}
		context.getSource().sendSuccess(() -> Component.literal((unlock ? "unlocked " : "locked ") + changed
				+ " book entry(ies) for " + targets.size() + " player(s)"), true);
		return changed;
	}

	private static int executeTransform(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		String id = StringArgumentType.getString(context, "form");
		FeralForm form = null;
		for (FeralForm f : FeralForms.all()) {
			if (f.getId().equals(id)) {
				form = f;
				break;
			}
		}
		if (form == null) {
			context.getSource().sendFailure(Component.literal("unknown form: " + id));
			return 0;
		}
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
		final FeralForm target = form;
		for (ServerPlayer player : targets) {
			FeralFormManager.transformFromItem(player.level(), player.getX(), player.getY(), player.getZ(), player, form, ItemStack.EMPTY);
		}
		context.getSource().sendSuccess(() -> Component.literal("transformed " + targets.size() + " player(s) to: " + target.getId()), true);
		return targets.size();
	}
}
