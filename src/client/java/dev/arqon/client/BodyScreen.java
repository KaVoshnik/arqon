package dev.arqon.client;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import dev.arqon.body.BodyFrost;
import dev.arqon.body.BodyPart;
import dev.arqon.body.BodyTemperature;
import dev.arqon.body.Frostbite;
import dev.arqon.climate.ThermalState;

/**
 * Экран тела: силуэт из шести контурных частей и две панели слева (общее самочувствие и выбранная часть).
 * Показывает только то, что человек чувствует: слова, цвет и сегменты, без чисел. Пока часть в порядке, она выглядит обычной.
 * Часть под курсором подсвечивается и описывается в панели; когда курсор ушёл, остаётся последняя.
 */
public class BodyScreen extends Screen {
	// Сетка силуэта: 40 x 76 клеток, каждая клетка принадлежит одной части тела или пустая.
	private static final int GW = 40;
	private static final int GH = 76;
	private static final BodyPart[] PARTS = BodyPart.values();
	private static final int[][] OWNER = new int[GH][GW];
	private static final boolean[][] EDGE = new boolean[GH][GW];
	/** Центр каждой части в клетках, для значка. */
	private static final double[][] CENTER = new double[BodyPart.COUNT][2];

	// Цвета: контур, подсветка, заливка по стадиям, значки и текст.
	private static final int OUTLINE = 0xFFE8EEF4;
	private static final int ACCENT = 0xFFFFD866;
	private static final int[] FILL = {0xFF18202A, 0xFF28405A, 0xFF2A4C88, 0xFF47337A, 0xFF211E26};
	private static final int[] TONE = {0xFFB0E0A0, 0xFFC8D8F0, 0xFF7FB2FF, 0xFFB48CFF, 0xFF9A98A4};

	private static final int PANEL_BORDER = 0xFF56626E;
	private static final int PANEL_BODY = 0xF0141A22;
	private static final int PANEL_HEADER = 0xFF222B36;
	private static final int TEXT_DIM = 0xFF9AA6B2;
	private static final int TEXT = 0xFFE8EEF4;
	private static final int MIN_PANEL_W = 156;

	/** Снежинка 7x7: значок холода на пострадавшей части. */
	private static final String[] FLAKE = {
			"...#...",
			".#.#.#.",
			"..###..",
			"#######",
			"..###..",
			".#.#.#.",
			"...#...",
	};

	static {
		for (int y = 0; y < GH; y++) {
			for (int x = 0; x < GW; x++) {
				OWNER[y][x] = ownerAt(x + 0.5, y + 0.5);
			}
		}
		int[] count = new int[BodyPart.COUNT];
		for (int y = 0; y < GH; y++) {
			for (int x = 0; x < GW; x++) {
				int o = OWNER[y][x];
				if (o < 0) {
					continue;
				}
				CENTER[o][0] += x + 0.5;
				CENTER[o][1] += y + 0.5;
				count[o]++;
				EDGE[y][x] = differs(o, x + 1, y) || differs(o, x - 1, y) || differs(o, x, y + 1) || differs(o, x, y - 1);
			}
		}
		for (int i = 0; i < BodyPart.COUNT; i++) {
			if (count[i] > 0) {
				CENTER[i][0] /= count[i];
				CENTER[i][1] /= count[i];
			}
		}
	}

	private static boolean differs(int owner, int x, int y) {
		return x < 0 || y < 0 || x >= GW || y >= GH || OWNER[y][x] != owner;
	}

	/** Какой части принадлежит точка силуэта, или -1. Правая рука и нога слева на экране: зритель смотрит на персонажа. */
	private static int ownerAt(double x, double y) {
		double mx = GW - x;
		if (sq((x - 20) / 6) + sq((y - 8) / 7) <= 1) {
			return BodyPart.HEAD.ordinal();
		}
		if (Math.pow(Math.abs((x - 20) / 7.5), 3) + Math.pow(Math.abs((y - 24.5) / 10.5), 3) <= 1) {
			return BodyPart.TORSO.ordinal();
		}
		if (capsule(x, y, 11.5, 17, 3.2, 3.5, 43, 2.4)) {
			return BodyPart.RIGHT_ARM.ordinal();
		}
		if (capsule(mx, y, 11.5, 17, 3.2, 3.5, 43, 2.4)) {
			return BodyPart.LEFT_ARM.ordinal();
		}
		if (leg(x, y)) {
			return BodyPart.RIGHT_LEG.ordinal();
		}
		if (leg(mx, y)) {
			return BodyPart.LEFT_LEG.ordinal();
		}
		return -1;
	}

	private static boolean leg(double x, double y) {
		return capsule(x, y, 16, 35.5, 4.0, 14.5, 70, 2.8) || capsule(x, y, 14.5, 71, 2.7, 12.5, 73, 2.4);
	}

	private static double sq(double v) {
		return v * v;
	}

	/** Попадает ли точка в «капсулу»: отрезок с плавно меняющимся радиусом. */
	private static boolean capsule(double px, double py, double ax, double ay, double ar, double bx, double by, double br) {
		double dx = bx - ax;
		double dy = by - ay;
		double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
		t = Math.max(0, Math.min(1, t));
		double qx = ax + dx * t;
		double qy = ay + dy * t;
		double r = ar + (br - ar) * t;
		return sq(px - qx) + sq(py - qy) <= r * r;
	}

	/** Последняя часть, на которую наводили курсор. */
	private BodyPart selected;
	/** Ширина панелей: подгоняется под самый длинный текст на текущем языке, чтобы строки не налезали друг на друга. */
	private int panelW = MIN_PANEL_W;

	public BodyScreen() {
		super(Component.translatable("screen.arqon.body"));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		// Затемняем мир, чтобы силуэт и панели читались.
		g.fill(0, 0, this.width, this.height, 0xB0070A10);

		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		panelW = computePanelWidth();
		double[] frost = BodyFrost.get(player);
		Frostbite.Stage[] stages = new Frostbite.Stage[BodyPart.COUNT];
		for (int i = 0; i < stages.length; i++) {
			stages[i] = Frostbite.Stage.of(frost[i]);
		}

		int px = Math.max(2, Math.min(6, (this.height - 24) / GH));
		int originX = (this.width + panelW + 24) / 2 - GW * px / 2;
		int originY = (this.height - GH * px) / 2;

		int cellX = Math.floorDiv(mouseX - originX, px);
		int cellY = Math.floorDiv(mouseY - originY, px);
		BodyPart hovered = null;
		if (cellX >= 0 && cellY >= 0 && cellX < GW && cellY < GH && OWNER[cellY][cellX] >= 0) {
			hovered = PARTS[OWNER[cellY][cellX]];
			selected = hovered;
		}

		drawSilhouette(g, originX, originY, px, stages, hovered);
		drawIcons(g, originX, originY, px, stages);

		int panelX = 12;
		int y = 12;
		y += drawVitals(g, panelX, y, player) + 8;
		drawDetail(g, panelX, y, stages);
	}

	private void drawSilhouette(GuiGraphics g, int originX, int originY, int px, Frostbite.Stage[] stages, BodyPart hovered) {
		for (int y = 0; y < GH; y++) {
			int runStart = -1;
			int runColor = 0;
			for (int x = 0; x <= GW; x++) {
				int color = x < GW ? cellColor(x, y, stages, hovered) : 0;
				if (runStart >= 0 && color != runColor) {
					g.fill(originX + runStart * px, originY + y * px, originX + x * px, originY + (y + 1) * px, runColor);
					runStart = -1;
				}
				if (runStart < 0 && color != 0) {
					runStart = x;
					runColor = color;
				}
			}
		}
	}

	private static int cellColor(int x, int y, Frostbite.Stage[] stages, BodyPart hovered) {
		int o = OWNER[y][x];
		if (o < 0) {
			return 0;
		}
		if (EDGE[y][x]) {
			return PARTS[o] == hovered ? ACCENT : OUTLINE;
		}
		return FILL[stages[o].ordinal()];
	}

	/** Значок снежинки в центре каждой части, которой уже плохо. */
	private void drawIcons(GuiGraphics g, int originX, int originY, int px, Frostbite.Stage[] stages) {
		int ip = Math.max(2, px * 3 / 5);
		for (int i = 0; i < BodyPart.COUNT; i++) {
			if (stages[i] == Frostbite.Stage.HEALTHY) {
				continue;
			}
			int x0 = originX + (int) Math.round(CENTER[i][0] * px) - FLAKE[0].length() * ip / 2;
			int y0 = originY + (int) Math.round(CENTER[i][1] * px) - FLAKE.length * ip / 2;
			int color = TONE[stages[i].ordinal()];
			for (int r = 0; r < FLAKE.length; r++) {
				for (int c = 0; c < FLAKE[r].length(); c++) {
					if (FLAKE[r].charAt(c) == '#') {
						g.fill(x0 + c * ip, y0 + r * ip, x0 + (c + 1) * ip, y0 + (r + 1) * ip, color);
					}
				}
			}
		}
	}

	/** Верхняя панель: общее самочувствие словами. Возвращает высоту. */
	private int drawVitals(GuiGraphics g, int x, int y, Player player) {
		ThermalState thermal = ThermalState.of(BodyTemperature.get(player));
		double air = BodyTemperature.getAmbient(player);
		String airKey = air < 0 ? "frost" : air < 10 ? "cold" : air <= 32 ? "comfort" : air <= 38 ? "hot" : "scorching";
		int thermalTone = thermal == ThermalState.NORMAL ? TONE[0] : thermal.name().contains("COLD") || thermal.name().contains("HYPOTHERMIA") ? TONE[2] : 0xFFFF9A5A;
		int airTone = air < 10 ? TONE[2] : air <= 32 ? TONE[0] : 0xFFFF9A5A;

		int h = 20 + 2 * 14 + 6;
		panel(g, x, y, panelW, h, Component.translatable("screen.arqon.body"));
		row(g, x, y + 22, Component.translatable("body.arqon.row.body"),
				Component.translatable("body.arqon.heat." + thermal.name().toLowerCase(Locale.ROOT)), thermalTone);
		row(g, x, y + 36, Component.translatable("body.arqon.row.air"),
				Component.translatable("body.arqon.air." + airKey), airTone);
		return h;
	}

	/** Нижняя панель: выбранная часть тела. */
	private void drawDetail(GuiGraphics g, int x, int y, Frostbite.Stage[] stages) {
		if (selected == null) {
			panel(g, x, y, panelW, 36, Component.translatable("body.arqon.title.none"));
			g.drawString(this.font, Component.translatable("body.arqon.hint"), x + 8, y + 24, TEXT_DIM);
			return;
		}
		Frostbite.Stage stage = stages[selected.ordinal()];
		String title = Component.translatable("body.arqon.part." + selected.id).getString().toUpperCase(Locale.ROOT);
		int h = 20 + 14 + 14 + 14 + 14 + 14 + 4;
		panel(g, x, y, panelW, h, Component.literal(title));

		// Состояние: подпись и значение на отдельных строках, чтобы длинные слова не лезли на подпись.
		g.drawString(this.font, Component.translatable("body.arqon.row.state"), x + 8, y + 22, TEXT_DIM);
		g.drawString(this.font, Component.translatable("body.arqon.stage." + stage.id()), x + 8, y + 33, TONE[stage.ordinal()]);

		g.drawString(this.font, Component.translatable("body.arqon.row.frost"), x + 8, y + 48, TEXT_DIM);
		int barX = x + 8;
		int barY = y + 59;
		int gap = 3;
		int seg = (panelW - 16 - gap * 3) / 4;
		for (int i = 0; i < 4; i++) {
			int color = stage.ordinal() > i ? TONE[stage.ordinal()] : 0xFF2A323C;
			g.fill(barX + i * (seg + gap), barY, barX + i * (seg + gap) + seg, barY + 6, color);
		}

		String effectKey = stage.ordinal() >= Frostbite.Stage.FROSTBITTEN.ordinal()
				? "body.arqon.effect." + selected.group.id
				: "body.arqon.effect.none";
		g.drawString(this.font, Component.translatable(effectKey), x + 8, y + 72, TEXT_DIM);
	}

	/** Самая длинная строка из тех, что могут появиться в панелях, плюс поля. */
	private int computePanelWidth() {
		int widest = MIN_PANEL_W - 16;
		for (Frostbite.Stage stage : Frostbite.Stage.values()) {
			widest = Math.max(widest, textWidth("body.arqon.stage." + stage.id()));
		}
		for (String effect : new String[]{"legs", "arms", "head", "torso", "none"}) {
			widest = Math.max(widest, textWidth("body.arqon.effect." + effect));
		}
		for (BodyPart part : PARTS) {
			widest = Math.max(widest, this.font.width(Component.translatable("body.arqon.part." + part.id).getString().toUpperCase(Locale.ROOT)));
		}
		widest = Math.max(widest, textWidth("body.arqon.hint"));
		int bodyLabel = textWidth("body.arqon.row.body");
		for (ThermalState state : ThermalState.values()) {
			widest = Math.max(widest, bodyLabel + 12 + textWidth("body.arqon.heat." + state.name().toLowerCase(Locale.ROOT)));
		}
		int airLabel = textWidth("body.arqon.row.air");
		for (String air : new String[]{"frost", "cold", "comfort", "hot", "scorching"}) {
			widest = Math.max(widest, airLabel + 12 + textWidth("body.arqon.air." + air));
		}
		return widest + 16;
	}

	private int textWidth(String key) {
		return this.font.width(Component.translatable(key));
	}

	private void panel(GuiGraphics g, int x, int y, int w, int h, Component title) {
		g.fill(x - 2, y - 2, x + w + 2, y + h + 2, PANEL_BORDER);
		g.fill(x, y, x + w, y + h, PANEL_BODY);
		g.fill(x, y, x + w, y + 16, PANEL_HEADER);
		g.drawCenteredString(this.font, title, x + w / 2, y + 4, TEXT);
	}

	private void row(GuiGraphics g, int x, int y, Component label, Component value, int valueColor) {
		g.drawString(this.font, label, x + 8, y, TEXT_DIM);
		g.drawString(this.font, value, x + panelW - 8 - this.font.width(value), y, valueColor);
	}
}
