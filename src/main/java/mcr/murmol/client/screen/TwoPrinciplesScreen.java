package mcr.murmol.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import mcr.murmol.feral.FeralForm;
import mcr.murmol.feral.FeralForms;
import mcr.murmol.feral.FeralFormToggles;
import mcr.murmol.network.FormTogglePayloads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 两仪界面：列表形式展示本世界各形态启用/禁用状态。
 * 形态列表每次打开时动态检测（FeralForms 已注册列表）；状态实时读客户端同步缓存，
 * 服务端广播后立即刷新。所有玩家可查看；3 级权限的玩家可编辑：
 * 行点击/全部启用/全部禁用只修改本地待定状态，点「确认」才批量提交到服务端，「取消」放弃并关闭。
 */
public class TwoPrinciplesScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private static final int ROW_WIDTH = 220;
	private static final int VISIBLE_ROWS = 6;

	private List<String> formIds = List.of();
	private int scrollOffset = 0;
	private boolean canEdit = false;
	/** 待定修改：形态 id -> 是否启用（确认前仅本地生效，不发送网络包） */
	private final Map<String, Boolean> pending = new HashMap<>();

	/** 灰色半透明按钮（与列表行同款样式，替代原版 Button） */
	private record UiBtn(int x, int y, int w, int h, String label, Runnable action) {
	}

	private final List<UiBtn> btns = new ArrayList<>();
	/** 右下角「设置」小按钮（所有玩家可见，打开本 mod 配置界面，不受编辑权限限制） */
	private UiBtn settingsBtn;
	/** 右下角「游戏规则」小按钮（所有玩家可见，打开本 mod 游戏规则设置界面） */
	private UiBtn gameRulesBtn;

	public TwoPrinciplesScreen() {
		super(Component.translatable("gui.murmol.two_principles.title"));
	}

	/** 物品/方块右键入口（仅客户端） */
	public static void open() {
		Minecraft.getInstance().setScreen(new TwoPrinciplesScreen());
	}

	@Override
	protected void init() {
		// 动态检测已加载的形态列表（每次打开重新获取）
		this.formIds = FeralFormToggles.formIds();
		this.scrollOffset = 0;
		this.pending.clear();
		this.canEdit = this.minecraft != null && this.minecraft.player != null
				&& this.minecraft.player.hasPermissions(3);

		this.btns.clear();
		// 右下角「设置」小按钮：打开本 mod 配置界面（NeoForge ConfigurationScreen）
		int sw = 50, sh = 16;
		this.settingsBtn = new UiBtn(this.width - sw - 6, this.height - sh - 6, sw, sh,
				Component.translatable("gui.murmol.two_principles.btn_settings").getString(),
				TwoPrinciplesScreen.this::openModConfig);
		// 「设置」左侧的「游戏规则」小按钮：打开本 mod 游戏规则设置界面
		int grw = 60;
		this.gameRulesBtn = new UiBtn(this.settingsBtn.x() - grw - 4, this.height - sh - 6, grw, sh,
				Component.translatable("gui.murmol.two_principles.btn_game_rules").getString(),
				() -> MurmolGameRulesScreen.open(TwoPrinciplesScreen.this));
		if (this.canEdit) {
			int left = this.width / 2 - ROW_WIDTH / 2;
			int top = 34;
			int btnY = top + VISIBLE_ROWS * ROW_HEIGHT + 10;
			// 一行四个按钮：全部启用 / 全部禁用 / 确认 / 取消
			this.btns.add(new UiBtn(left, btnY, 60, 20,
					Component.translatable("gui.murmol.two_principles.btn_enable_all").getString(),
					() -> setAll(true)));
			this.btns.add(new UiBtn(left + 62, btnY, 60, 20,
					Component.translatable("gui.murmol.two_principles.btn_disable_all").getString(),
					() -> setAll(false)));
			this.btns.add(new UiBtn(left + 124, btnY, 46, 20,
					Component.translatable("gui.murmol.two_principles.btn_apply").getString(),
					this::applyChanges));
			this.btns.add(new UiBtn(left + 172, btnY, 48, 20,
					Component.translatable("gui.murmol.two_principles.btn_cancel").getString(), () -> {
						this.pending.clear();
						onClose();
					}));
		}
	}

	/** 打开本 mod 的配置界面（MurmolMod 构造器中注册的 IConfigScreenFactory） */
	private void openModConfig() {
		var mc = Minecraft.getInstance();
		net.neoforged.fml.ModList.get().getModContainerById(mcr.murmol.MurmolMod.MODID).ifPresent(container -> container
				.getCustomExtension(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class)
				.ifPresent(factory -> mc.setScreen(factory.createScreen(container, this))));
	}

	/** 待定状态下的形态是否启用（优先本地待定值，其次服务端同步缓存） */
	private boolean isEnabled(String formId) {
		return this.pending.getOrDefault(formId,
				FormTogglePayloads.CLIENT_ENABLED.getOrDefault(formId, Boolean.TRUE));
	}

	/** 全部启用/禁用（仅修改本地待定状态） */
	private void setAll(boolean enabled) {
		for (String formId : this.formIds)
			this.pending.put(formId, enabled);
	}

	/** 确认：把与当前服务端状态不同的待定项逐个提交（Toggle 为切换语义，只发有变化的项） */
	private void applyChanges() {
		for (Map.Entry<String, Boolean> entry : this.pending.entrySet()) {
			boolean serverState = FormTogglePayloads.CLIENT_ENABLED.getOrDefault(entry.getKey(), Boolean.TRUE);
			if (serverState != entry.getValue())
				net.neoforged.neoforge.network.PacketDistributor.sendToServer(
						new FormTogglePayloads.ToggleMessage(entry.getKey()));
		}
		this.pending.clear();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		var font = this.font;

		graphics.drawCenteredString(font, this.title, this.width / 2, 16, 0xFFFFFF);

		int left = this.width / 2 - ROW_WIDTH / 2;
		int top = 34;
		int listHeight = VISIBLE_ROWS * ROW_HEIGHT;

		// 列表背景
		graphics.fill(left - 4, top - 4, left + ROW_WIDTH + 4, top + listHeight + 4, 0x88000000);

		// 逐行动态渲染（管理员读待定状态，普通玩家读同步缓存）
		for (int visible = 0; visible < VISIBLE_ROWS; visible++) {
			int index = this.scrollOffset + visible;
			if (index >= this.formIds.size())
				break;
			String formId = this.formIds.get(index);
			FeralForm form = FeralForms.byId(formId);
			String label = form == null ? formId : form.getDisplayName().getString();
			boolean enabled = isEnabled(formId);
			// 有未确认的本地修改时行内加亮标记
			boolean pendingHere = this.pending.containsKey(formId);

			int y = top + visible * ROW_HEIGHT;
			boolean hovered = this.canEdit && mouseX >= left && mouseX < left + ROW_WIDTH
					&& mouseY >= y && mouseY < y + ROW_HEIGHT;

			// 行背景：悬停高亮 / 待定修改提示
			int bg = pendingHere ? 0x50C0A050 : hovered ? 0x509090C0 : 0x30A0A0A0;
			graphics.fill(left, y, left + ROW_WIDTH, y + ROW_HEIGHT - 2, bg);

			// 形态名
			graphics.drawString(font, label, left + 6, y + 7, 0xFFFFFF, false);
			// 状态（右侧）
			String stateText = enabled
					? Component.translatable("gui.murmol.two_principles.state_enabled").getString()
					: Component.translatable("gui.murmol.two_principles.state_disabled").getString();
			graphics.drawString(font, stateText, left + ROW_WIDTH - 6 - font.width(stateText), y + 7,
					enabled ? 0x55FF55 : 0xFF5555, false);
		}

		// 滚动条
		int total = this.formIds.size();
		if (total > VISIBLE_ROWS) {
			int barHeight = Math.max(listHeight * VISIBLE_ROWS / total, 10);
			int barY = top + (listHeight - barHeight) * this.scrollOffset / (total - VISIBLE_ROWS);
			graphics.fill(left + ROW_WIDTH + 1, barY, left + ROW_WIDTH + 3, barY + barHeight, 0xFFA0A0A0);
		}

		// 灰色半透明按钮（与列表行同款样式：悬停高亮）
		for (UiBtn b : this.btns) {
			boolean hov = mouseX >= b.x() && mouseX < b.x() + b.w() && mouseY >= b.y() && mouseY < b.y() + b.h();
			graphics.fill(b.x(), b.y(), b.x() + b.w(), b.y() + b.h(), hov ? 0x509090C0 : 0x30A0A0A0);
			graphics.drawString(font, b.label(), b.x() + (b.w() - font.width(b.label())) / 2, b.y() + 6,
					0xFFFFFF, false);
		}

		// 右下角「游戏规则」「设置」小按钮（同款样式）
		UiBtn[] cornerBtns = { this.gameRulesBtn, this.settingsBtn };
		for (UiBtn b0 : cornerBtns) {
			if (b0 == null)
				continue;
			UiBtn b = b0;
			boolean hov = mouseX >= b.x() && mouseX < b.x() + b.w() && mouseY >= b.y() && mouseY < b.y() + b.h();
			graphics.fill(b.x(), b.y(), b.x() + b.w(), b.y() + b.h(), hov ? 0x509090C0 : 0x30A0A0A0);
			graphics.drawString(font, b.label(), b.x() + (b.w() - font.width(b.label())) / 2, b.y() + 3,
					0xFFFFFF, false);
		}

		// 底部提示（按钮在下方，再往下移）
		Component hint = this.canEdit
				? Component.translatable("gui.murmol.two_principles.hint_edit")
				: Component.translatable("gui.murmol.two_principles.readonly");
		graphics.drawCenteredString(font, hint, this.width / 2, top + listHeight + 34, 0xAAAAAA);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// 右下角「游戏规则」「设置」按钮（所有玩家可用，不受编辑权限限制）
		if (button == 0) {
			for (UiBtn b : new UiBtn[] { this.gameRulesBtn, this.settingsBtn }) {
				if (b != null && mouseX >= b.x() && mouseX < b.x() + b.w()
						&& mouseY >= b.y() && mouseY < b.y() + b.h()) {
					b.action().run();
					return true;
				}
			}
		}
		// 灰色半透明按钮（列表样式）命中
		if (this.canEdit && button == 0) {
			for (UiBtn b : this.btns) {
				if (mouseX >= b.x() && mouseX < b.x() + b.w() && mouseY >= b.y() && mouseY < b.y() + b.h()) {
					b.action().run();
					return true;
				}
			}
		}
		if (this.canEdit && button == 0 && !this.formIds.isEmpty()) {
			int left = this.width / 2 - ROW_WIDTH / 2;
			int top = 34;
			if (mouseX >= left && mouseX < left + ROW_WIDTH && mouseY >= top) {
				int index = this.scrollOffset + (int) ((mouseY - top) / ROW_HEIGHT);
				if (index >= 0 && index < this.formIds.size()) {
					String formId = this.formIds.get(index);
					// 仅修改本地待定状态，确认后统一提交
					this.pending.put(formId, !isEnabled(formId));
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		int max = Math.max(this.formIds.size() - VISIBLE_ROWS, 0);
		int newOffset = (int) Math.round(this.scrollOffset - scrollY);
		this.scrollOffset = Math.max(0, Math.min(max, newOffset));
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
