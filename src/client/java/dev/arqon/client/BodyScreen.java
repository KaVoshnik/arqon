package dev.arqon.client;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import dev.arqon.body.BodyPart;
import dev.arqon.body.BodyState;
import dev.arqon.body.BodyStates;
import dev.arqon.body.BodyTemperature;
import dev.arqon.body.Frostbite;
import dev.arqon.body.PartThermals;
import dev.arqon.climate.ThermalState;

/**
 * Экран тела: силуэт из частей с суставами и две панели слева (общее самочувствие и выбранная часть).
 * Нажатие на кисть открывает окошко с пальцами. Показывает только то, что человек чувствует: слова, цвет и сегменты, без чисел.
 * Часть под курсором подсвечивается и описывается в панели; когда курсор ушёл, остаётся последняя.
 */
public class BodyScreen extends Screen {
	// Сетка силуэта: 40 x 78 клеток, каждая клетка принадлежит одной части тела или пустая.
	private static final int GW = 40;
	private static final int GH = 78;
	private static final BodyPart[] PARTS = BodyPart.values();
	private static final int[][] OWNER = new int[GH][GW];
	private static final boolean[][] EDGE = new boolean[GH][GW];
	/** Центр и число клеток каждой части силуэта. */
	private static final double[][] CENTER = new double[BodyPart.COUNT][2];
	private static final int[] CELLS = new int[BodyPart.COUNT];
	/** Рамки кистей в клетках: x0, y0, x1, y1 (включительно). */
	private static final int[][] HAND_BOX = new int[BodyPart.COUNT][];

	// Сетка окошка пальцев: 48 x 60 клеток. Ячейка принадлежит пальцу, кисти или пустая; для правой руки.
	private static final int FW = 48;
	private static final int FH = 60;
	private static final int[][] FINGER_OWNER_RIGHT = new int[FH][FW];
	private static final boolean[][] FINGER_EDGE_RIGHT = new boolean[FH][FW];
	private static final BodyPart[] FINGER_ORDER = {BodyPart.RIGHT_THUMB, BodyPart.RIGHT_INDEX, BodyPart.RIGHT_MIDDLE, BodyPart.RIGHT_RING, BodyPart.RIGHT_PINKY};

	// Цвета: контур, подсветка, заливка по стадиям, значки и текст.
	private static final int OUTLINE = 0xFFE8EEF4;
	private static final int ACCENT = 0xFFFFD866;
	private static final int[] FILL_FROST = {0xFF18202A, 0xFF28405A, 0xFF2A4C88, 0xFF47337A, 0xFF211E26};
	private static final int[] FILL_BURN = {0xFF18202A, 0xFF5A2E28, 0xFF8A3A1E, 0xFF5A2A10, 0xFF1E1410};
	private static final int[] TONE_FROST = {0xFFB0E0A0, 0xFFC8D8F0, 0xFF7FB2FF, 0xFFB48CFF, 0xFF9A98A4};
	private static final int[] TONE_BURN = {0xFFB0E0A0, 0xFFFFB090, 0xFFFF8A4A, 0xFFE0602A, 0xFFA09890};
	private static final int FILL_ICY = 0xFF1C2C40;
	private static final int FILL_COLD = 0xFF1A2636;
	private static final int FILL_HOT = 0xFF3A2220;
	private static final int FILL_SCALDING = 0xFF4A241C;

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
	/** Огонёк 7x7: значок ожога. */
	private static final String[] FLAME = {
			"...#...",
			"..##...",
			"..###.#",
			".#####.",
			".#####.",
			".#####.",
			"..###..",
	};

	static {
		for (int y = 0; y < GH; y++) {
			for (int x = 0; x < GW; x++) {
				OWNER[y][x] = ownerAt(x + 0.5, y + 0.5);
			}
		}
		int[][] box = new int[BodyPart.COUNT][];
		for (int y = 0; y < GH; y++) {
			for (int x = 0; x < GW; x++) {
				int o = OWNER[y][x];
				if (o < 0) {
					continue;
				}
				CENTER[o][0] += x + 0.5;
				CENTER[o][1] += y + 0.5;
				CELLS[o]++;
				EDGE[y][x] = differs(OWNER, GW, GH, o, x + 1, y) || differs(OWNER, GW, GH, o, x - 1, y)
						|| differs(OWNER, GW, GH, o, x, y + 1) || differs(OWNER, GW, GH, o, x, y - 1);
				if (box[o] == null) {
					box[o] = new int[]{x, y, x, y};
				} else {
					box[o][0] = Math.min(box[o][0], x);
					box[o][1] = Math.min(box[o][1], y);
					box[o][2] = Math.max(box[o][2], x);
					box[o][3] = Math.max(box[o][3], y);
				}
			}
		}
		for (int i = 0; i < BodyPart.COUNT; i++) {
			if (CELLS[i] > 0) {
				CENTER[i][0] /= CELLS[i];
				CENTER[i][1] /= CELLS[i];
			}
		}
		HAND_BOX[BodyPart.RIGHT_HAND.ordinal()] = box[BodyPart.RIGHT_HAND.ordinal()];
		HAND_BOX[BodyPart.LEFT_HAND.ordinal()] = box[BodyPart.LEFT_HAND.ordinal()];

		for (int y = 0; y < FH; y++) {
			for (int x = 0; x < FW; x++) {
				FINGER_OWNER_RIGHT[y][x] = fingerOwnerAt(x + 0.5, y + 0.5);
			}
		}
		for (int y = 0; y < FH; y++) {
			for (int x = 0; x < FW; x++) {
				int o = FINGER_OWNER_RIGHT[y][x];
				if (o >= -1 && o != -1) {
					FINGER_EDGE_RIGHT[y][x] = differs(FINGER_OWNER_RIGHT, FW, FH, o, x + 1, y) || differs(FINGER_OWNER_RIGHT, FW, FH, o, x - 1, y)
							|| differs(FINGER_OWNER_RIGHT, FW, FH, o, x, y + 1) || differs(FINGER_OWNER_RIGHT, FW, FH, o, x, y - 1);
				}
			}
		}
	}

	private static boolean differs(int[][] grid, int w, int h, int owner, int x, int y) {
		return x < 0 || y < 0 || x >= w || y >= h || grid[y][x] != owner;
	}

	/**
	 * Какой части принадлежит точка силуэта, или -1. Правая рука и нога слева на экране: зритель смотрит на персонажа.
	 * Руки и ноги разделены на звенья по суставам.
	 */
	private static int ownerAt(double x, double y) {
		if (sq((x - 20) / 6) + sq((y - 8) / 7) <= 1) {
			return BodyPart.HEAD.ordinal();
		}
		if (Math.pow(Math.abs((x - 20) / 7.5), 3) + Math.pow(Math.abs((y - 24.5) / 10.5), 3) <= 1) {
			return BodyPart.TORSO.ordinal();
		}
		double mx = GW - x;
		for (int link = 0; link < 6; link++) {
			if (link(link, x, y)) {
				return linkPart(link, BodyPart.Side.RIGHT).ordinal();
			}
			if (link(link, mx, y)) {
				return linkPart(link, BodyPart.Side.LEFT).ordinal();
			}
		}
		return -1;
	}

	/** Звенья конечности по порядку: плечо, предплечье, кисть, бедро, голень, стопа. */
	private static boolean link(int link, double x, double y) {
		return switch (link) {
			case 0 -> capsule(x, y, 11.5, 17, 3.2, 8.2, 29.5, 2.9);
			case 1 -> capsule(x, y, 8.2, 29.8, 2.8, 5.2, 41, 2.3);
			case 2 -> capsule(x, y, 5.0, 41.5, 2.5, 4.0, 46.5, 2.3);
			case 3 -> capsule(x, y, 16, 35.5, 4.0, 15.2, 53, 3.4);
			case 4 -> capsule(x, y, 15.2, 53.3, 3.2, 14.5, 70, 2.6);
			default -> capsule(x, y, 14.5, 70.5, 2.7, 12.3, 74, 2.4);
		};
	}

	private static BodyPart linkPart(int link, BodyPart.Side side) {
		boolean right = side == BodyPart.Side.RIGHT;
		return switch (link) {
			case 0 -> right ? BodyPart.RIGHT_UPPER_ARM : BodyPart.LEFT_UPPER_ARM;
			case 1 -> right ? BodyPart.RIGHT_FOREARM : BodyPart.LEFT_FOREARM;
			case 2 -> right ? BodyPart.RIGHT_HAND : BodyPart.LEFT_HAND;
			case 3 -> right ? BodyPart.RIGHT_THIGH : BodyPart.LEFT_THIGH;
			case 4 -> right ? BodyPart.RIGHT_SHIN : BodyPart.LEFT_SHIN;
			default -> right ? BodyPart.RIGHT_FOOT : BodyPart.LEFT_FOOT;
		};
	}

	/** Кисть правой руки в окошке пальцев: индекс в {@link #FINGER_ORDER}, 5 это сама кисть, -1 пусто. */
	private static int fingerOwnerAt(double x, double y) {
		double[][] fingers = {
				{39.0, 42.0, 3.8, 45.0, 27.0, 3.2},   // большой
				{32.6, 30.0, 3.2, 34.6, 13.0, 2.8},   // указательный
				{25.6, 29.0, 3.4, 25.6, 8.0, 3.0},    // средний
				{18.8, 30.0, 3.2, 16.8, 12.0, 2.8},   // безымянный
				{12.4, 33.0, 2.8, 8.6, 19.0, 2.4},    // мизинец
		};
		for (int i = 0; i < fingers.length; i++) {
			double[] f = fingers[i];
			if (capsule(x, y, f[0], f[1], f[2], f[3], f[4], f[5])) {
				return i;
			}
		}
		if (Math.pow(Math.abs((x - 25.0) / 15.0), 2.6) + Math.pow(Math.abs((y - 43.0) / 12.4), 2.6) <= 1
				|| capsule(x, y, 25.0, 52.0, 6.4, 25.0, 59.0, 6.4)) {
			return 5;
		}
		return -1;
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
	/** Чья кисть открыта в окошке пальцев, или null. */
	private BodyPart.Side popupSide;
	/** Ширина панелей: подгоняется под самый длинный текст на текущем языке, чтобы строки не налезали друг на друга. */
	private int panelW = MIN_PANEL_W;

	// Раскладка, считается в init().
	private int px;
	private int originX;
	private int originY;
	private int pxF;
	private int popupX;
	private int popupY;
	private int popupW;
	private int popupH;

	private Button rightHandButton;
	private Button leftHandButton;
	private Button closeButton;

	public BodyScreen() {
		super(Component.translatable("screen.arqon.body"));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		panelW = computePanelWidth();
		px = Math.max(2, Math.min(6, (this.height - 24) / GH));
		originX = (this.width + panelW + 24) / 2 - GW * px / 2 - (GW * px) / 2;
		originX = Math.max(panelW + 40, originX);
		originY = (this.height - GH * px) / 2;

		pxF = Math.max(2, Math.min(5, (this.height - 80) / FH));
		popupW = FW * pxF + 24;
		popupH = FH * pxF + 40;
		popupX = Math.min(originX + GW * px + 28, this.width - popupW - 8);
		popupY = (this.height - popupH) / 2;

		// Невидимые кнопки поверх нарисованного: нажатие на кисть и на крестик окошка. Рисуем их сами.
		rightHandButton = handButton(BodyPart.Side.RIGHT);
		leftHandButton = handButton(BodyPart.Side.LEFT);
		closeButton = Button.builder(Component.empty(), b -> popupSide = null)
				.bounds(popupX + popupW - 16, popupY + 2, 14, 12).build();
		closeButton.setAlpha(0.0f);
		addRenderableWidget(rightHandButton);
		addRenderableWidget(leftHandButton);
		addRenderableWidget(closeButton);
	}

	private Button handButton(BodyPart.Side side) {
		int[] box = HAND_BOX[BodyPart.hand(side).ordinal()];
		int x0 = originX + box[0] * px - 2;
		int y0 = originY + box[1] * px - 2;
		int w = (box[2] - box[0] + 1) * px + 4;
		int h = (box[3] - box[1] + 1) * px + 4;
		Button button = Button.builder(Component.empty(), b -> popupSide = popupSide == side ? null : side)
				.bounds(x0, y0, w, h).build();
		button.setAlpha(0.0f);
		return button;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		closeButton.visible = popupSide != null;
		closeButton.active = popupSide != null;
		super.render(g, mouseX, mouseY, partialTick);
		// Затемняем мир, чтобы силуэт и панели читались.
		g.fill(0, 0, this.width, this.height, 0xB0070A10);

		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		BodyState state = BodyStates.get(player);

		BodyPart hovered = null;
		BodyPart hoveredMain = null;
		int cellX = Math.floorDiv(mouseX - originX, px);
		int cellY = Math.floorDiv(mouseY - originY, px);
		if (cellX >= 0 && cellY >= 0 && cellX < GW && cellY < GH && OWNER[cellY][cellX] >= 0) {
			hoveredMain = PARTS[OWNER[cellY][cellX]];
			hovered = hoveredMain;
		}
		BodyPart hoveredFinger = null;
		if (popupSide != null) {
			hoveredFinger = fingerAt(mouseX, mouseY);
			if (hoveredFinger != null) {
				hovered = hoveredFinger;
			}
		}
		if (hovered != null) {
			selected = hovered;
		}

		drawSilhouette(g, state, hoveredMain);
		drawIcons(g, state);

		if (popupSide != null) {
			drawPopup(g, state, hoveredFinger);
		}

		int panelX = 12;
		int y = 12;
		y += drawVitals(g, panelX, y, player) + 8;
		drawDetail(g, panelX, y, state);
	}

	// ---------------------------- силуэт ----------------------------

	private void drawSilhouette(GuiGraphics g, BodyState state, BodyPart hovered) {
		for (int y = 0; y < GH; y++) {
			int runStart = -1;
			int runColor = 0;
			for (int x = 0; x <= GW; x++) {
				int color = x < GW ? cellColor(x, y, state, hovered) : 0;
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

	private int cellColor(int x, int y, BodyState state, BodyPart hovered) {
		int o = OWNER[y][x];
		if (o < 0) {
			return 0;
		}
		if (EDGE[y][x]) {
			return PARTS[o] == hovered || PARTS[o] == popupHand() ? ACCENT : OUTLINE;
		}
		return fill(state, PARTS[o]);
	}

	/** Выбранная в окошке кисть подсвечивается на силуэте. */
	private BodyPart popupHand() {
		return popupSide == null ? null : BodyPart.hand(popupSide);
	}

	/** Цвет заливки части: по тяжести повреждения, а если всё в порядке, по температуре. */
	private static int fill(BodyState state, BodyPart part) {
		int i = part.ordinal();
		Frostbite.Stage frost = Frostbite.Stage.of(state.frost[i]);
		Frostbite.Stage burn = Frostbite.Stage.of(state.burn[i]);
		if (frost != Frostbite.Stage.HEALTHY || burn != Frostbite.Stage.HEALTHY) {
			return burn.ordinal() > frost.ordinal() ? FILL_BURN[burn.ordinal()] : FILL_FROST[frost.ordinal()];
		}
		double t = state.temp[i];
		if (t < 8) {
			return FILL_ICY;
		}
		if (t < 18) {
			return FILL_COLD;
		}
		if (t >= 44) {
			return FILL_SCALDING;
		}
		if (t >= 40) {
			return FILL_HOT;
		}
		return FILL_FROST[0];
	}

	/** Значок в центре каждой достаточно крупной части, которой уже плохо: снежинка или огонёк. */
	private void drawIcons(GuiGraphics g, BodyState state) {
		int ip = Math.max(2, px * 3 / 5);
		for (int i = 0; i < BodyPart.COUNT; i++) {
			if (CELLS[i] < 60) {
				continue;
			}
			Frostbite.Stage frost = Frostbite.Stage.of(state.frost[i]);
			Frostbite.Stage burn = Frostbite.Stage.of(state.burn[i]);
			if (frost == Frostbite.Stage.HEALTHY && burn == Frostbite.Stage.HEALTHY) {
				continue;
			}
			boolean isBurn = burn.ordinal() > frost.ordinal();
			Frostbite.Stage stage = isBurn ? burn : frost;
			drawIcon(g, isBurn ? FLAME : FLAKE, originX + (int) Math.round(CENTER[i][0] * px), originY + (int) Math.round(CENTER[i][1] * px), ip,
					isBurn ? TONE_BURN[stage.ordinal()] : TONE_FROST[stage.ordinal()]);
		}
	}

	private static void drawIcon(GuiGraphics g, String[] icon, int centerX, int centerY, int ip, int color) {
		int x0 = centerX - icon[0].length() * ip / 2;
		int y0 = centerY - icon.length * ip / 2;
		for (int r = 0; r < icon.length; r++) {
			for (int c = 0; c < icon[r].length(); c++) {
				if (icon[r].charAt(c) == '#') {
					g.fill(x0 + c * ip, y0 + r * ip, x0 + (c + 1) * ip, y0 + (r + 1) * ip, color);
				}
			}
		}
	}

	// ---------------------------- окошко пальцев ----------------------------

	/** Часть, соответствующая клетке окошка пальцев для данной стороны, или -1; пальцы левой руки отражены. */
	private int fingerCell(int cellX, int cellY) {
		if (cellX < 0 || cellY < 0 || cellX >= FW || cellY >= FH) {
			return -1;
		}
		int sx = popupSide == BodyPart.Side.LEFT ? FW - 1 - cellX : cellX;
		return FINGER_OWNER_RIGHT[cellY][sx];
	}

	private BodyPart fingerPart(int owner) {
		if (owner < 0) {
			return null;
		}
		return owner == 5 ? BodyPart.hand(popupSide) : BodyPart.fingers(popupSide)[owner];
	}

	private BodyPart fingerAt(int mouseX, int mouseY) {
		int cellX = Math.floorDiv(mouseX - (popupX + 12), pxF);
		int cellY = Math.floorDiv(mouseY - (popupY + 28), pxF);
		return fingerPart(fingerCell(cellX, cellY));
	}

	private void drawPopup(GuiGraphics g, BodyState state, BodyPart hoveredFinger) {
		BodyPart hand = BodyPart.hand(popupSide);
		panel(g, popupX, popupY, popupW, popupH, Component.translatable("body.arqon.part." + hand.id));
		g.drawString(this.font, "x", popupX + popupW - 12, popupY + 4, TEXT_DIM);

		int ox = popupX + 12;
		int oy = popupY + 28;
		for (int y = 0; y < FH; y++) {
			int runStart = -1;
			int runColor = 0;
			for (int x = 0; x <= FW; x++) {
				int color = 0;
				if (x < FW) {
					int owner = fingerCell(x, y);
					if (owner >= 0) {
						BodyPart part = fingerPart(owner);
						int sx = popupSide == BodyPart.Side.LEFT ? FW - 1 - x : x;
						color = FINGER_EDGE_RIGHT[y][sx] ? (part == hoveredFinger ? ACCENT : OUTLINE) : fill(state, part);
					}
				}
				if (runStart >= 0 && color != runColor) {
					g.fill(ox + runStart * pxF, oy + y * pxF, ox + x * pxF, oy + (y + 1) * pxF, runColor);
					runStart = -1;
				}
				if (runStart < 0 && color != 0) {
					runStart = x;
					runColor = color;
				}
			}
		}
	}

	// ---------------------------- панели ----------------------------

	/** Верхняя панель: общее самочувствие словами. Возвращает высоту. */
	private int drawVitals(GuiGraphics g, int x, int y, Player player) {
		ThermalState thermal = ThermalState.of(BodyTemperature.get(player));
		double air = BodyTemperature.getAmbient(player);
		String airKey = air < 0 ? "frost" : air < 10 ? "cold" : air <= 32 ? "comfort" : air <= 38 ? "hot" : "scorching";
		int thermalTone = thermal == ThermalState.NORMAL ? TONE_FROST[0] : thermal.name().contains("COLD") || thermal.name().contains("HYPOTHERMIA") ? TONE_FROST[2] : 0xFFFF9A5A;
		int airTone = air < 10 ? TONE_FROST[2] : air <= 32 ? TONE_FROST[0] : 0xFFFF9A5A;

		int h = 20 + 2 * 14 + 6;
		panel(g, x, y, panelW, h, Component.translatable("screen.arqon.body"));
		row(g, x, y + 22, Component.translatable("body.arqon.row.body"),
				Component.translatable("body.arqon.heat." + thermal.name().toLowerCase(Locale.ROOT)), thermalTone);
		row(g, x, y + 36, Component.translatable("body.arqon.row.air"),
				Component.translatable("body.arqon.air." + airKey), airTone);
		return h;
	}

	/** Нижняя панель: выбранная часть тела. */
	private void drawDetail(GuiGraphics g, int x, int y, BodyState state) {
		if (selected == null) {
			panel(g, x, y, panelW, 36, Component.translatable("body.arqon.title.none"));
			g.drawString(this.font, Component.translatable("body.arqon.hint"), x + 8, y + 24, TEXT_DIM);
			return;
		}
		int i = selected.ordinal();
		double frost = state.frost[i];
		double burn = state.burn[i];
		boolean isBurn = burn > frost;
		Frostbite.Stage stage = Frostbite.Stage.of(isBurn ? burn : frost);
		String kind = isBurn ? "burn" : "frost";
		int tone = (isBurn ? TONE_BURN : TONE_FROST)[stage.ordinal()];
		PartThermals.Feel feel = PartThermals.Feel.of(state.temp[i]);

		String title = Component.translatable("body.arqon.part." + selected.id).getString().toUpperCase(Locale.ROOT);
		boolean isHand = selected.group == BodyPart.Group.HANDS;
		int h = 20 + 14 + 14 + 14 + 14 + 14 + 14 + 4 + (isHand ? 14 : 0);
		panel(g, x, y, panelW, h, Component.literal(title));

		row(g, x, y + 22, Component.translatable("body.arqon.row.feel"),
				Component.translatable("body.arqon.feel." + feel.id()), feelTone(feel));

		// Состояние: подпись и значение на отдельных строках, чтобы длинные слова не лезли на подпись.
		g.drawString(this.font, Component.translatable("body.arqon.row.state"), x + 8, y + 38, TEXT_DIM);
		Component stageText = stage == Frostbite.Stage.HEALTHY
				? Component.translatable("body.arqon.stage.healthy")
				: Component.translatable("body.arqon.stage." + kind + "." + stage.id());
		g.drawString(this.font, stageText, x + 8, y + 49, tone);

		g.drawString(this.font, Component.translatable("body.arqon.row." + kind), x + 8, y + 64, TEXT_DIM);
		int barX = x + 8;
		int barY = y + 75;
		int gap = 3;
		int seg = (panelW - 16 - gap * 3) / 4;
		for (int s = 0; s < 4; s++) {
			int color = stage.ordinal() > s ? tone : 0xFF2A323C;
			g.fill(barX + s * (seg + gap), barY, barX + s * (seg + gap) + seg, barY + 6, color);
		}

		g.drawString(this.font, Component.translatable(effectKey(selected, isBurn, stage)), x + 8, y + 88, TEXT_DIM);
		if (isHand) {
			g.drawString(this.font, Component.translatable("body.arqon.hint.fingers"), x + 8, y + 102, TONE_FROST[2]);
		}
	}

	/** Что часть меняет в игре: пока повреждение не дошло до обморожения или ожога, ничего. */
	private static String effectKey(BodyPart part, boolean isBurn, Frostbite.Stage stage) {
		if (stage.ordinal() < Frostbite.Stage.FROSTBITTEN.ordinal()) {
			return "body.arqon.effect.none";
		}
		return switch (part.group) {
			case HEAD -> isBurn ? "body.arqon.effect.none" : "body.arqon.effect.head";
			case TORSO -> isBurn ? "body.arqon.effect.none" : "body.arqon.effect.torso";
			case LEGS, FEET -> "body.arqon.effect.legs";
			case HANDS, FINGERS -> "body.arqon.effect.hands";
			case ARMS -> "body.arqon.effect.none";
		};
	}

	private static int feelTone(PartThermals.Feel feel) {
		return switch (feel) {
			case FROZEN, ICY -> 0xFF7FB2FF;
			case COLD -> 0xFFA8C8F0;
			case COOL, NORMAL -> 0xFFB0E0A0;
			case WARM -> 0xFFFFD090;
			case HOT -> 0xFFFF9A5A;
			case SCALDING -> 0xFFFF6A3A;
		};
	}

	/** Самая длинная строка из тех, что могут появиться в панелях, плюс поля. */
	private int computePanelWidth() {
		int widest = MIN_PANEL_W - 16;
		for (Frostbite.Stage stage : Frostbite.Stage.values()) {
			if (stage != Frostbite.Stage.HEALTHY) {
				widest = Math.max(widest, textWidth("body.arqon.stage.frost." + stage.id()));
				widest = Math.max(widest, textWidth("body.arqon.stage.burn." + stage.id()));
			}
		}
		widest = Math.max(widest, textWidth("body.arqon.stage.healthy"));
		for (String effect : new String[]{"head", "torso", "legs", "hands", "none"}) {
			widest = Math.max(widest, textWidth("body.arqon.effect." + effect));
		}
		for (BodyPart part : PARTS) {
			widest = Math.max(widest, this.font.width(Component.translatable("body.arqon.part." + part.id).getString().toUpperCase(Locale.ROOT)));
		}
		widest = Math.max(widest, textWidth("body.arqon.hint"));
		widest = Math.max(widest, textWidth("body.arqon.hint.fingers"));
		int feelLabel = textWidth("body.arqon.row.feel");
		for (PartThermals.Feel feel : PartThermals.Feel.values()) {
			widest = Math.max(widest, feelLabel + 12 + textWidth("body.arqon.feel." + feel.id()));
		}
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
