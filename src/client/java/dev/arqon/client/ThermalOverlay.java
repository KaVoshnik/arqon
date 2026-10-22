package dev.arqon.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Оверлей жары и холода без текстур: цветная виньетка по краям экрана и лёгкий «пульс» тона.
 * Жара: тёплая оранжево-красная дымка с мерцанием. Холод: голубовато-белая изморозь по краям.
 */
public final class ThermalOverlay {
	/** Сколько слоёв виньетки. Больше: плавнее, но больше прямоугольников. */
	private static final int BANDS = 16;
	/** Глубина виньетки как доля меньшей стороны экрана при полной силе. */
	private static final float HEAT_DEPTH = 0.20f;
	private static final float COLD_DEPTH = 0.28f;

	private static final int HEAT_RGB = 0xE8601C;
	private static final int HEAT_HAZE_RGB = 0xFFB060;
	private static final int COLD_RGB = 0xBFE4FF;
	private static final int COLD_TINT_RGB = 0x5A8CD2;

	private ThermalOverlay() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
		float heat = ThermalFeedback.heat();
		float cold = ThermalFeedback.cold();
		if (heat < 0.02f && cold < 0.02f) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		float time = (client.level != null ? client.level.getGameTime() : 0L) + tickCounter.getGameTimeDeltaPartialTick(false);

		if (heat >= 0.02f) {
			// Медленный пульс, как марево: сила гуляет в пределах ±15 %.
			float pulse = 0.85f + 0.15f * (float) Math.sin(time * 0.12);
			float s = heat * pulse;
			// Лёгкая тёплая дымка на весь экран.
			graphics.fill(0, 0, width, height, argb(s * 0.10f, HEAT_HAZE_RGB));
			vignette(graphics, width, height, HEAT_RGB, s * 0.55f, HEAT_DEPTH);
		}

		if (cold >= 0.02f) {
			// Холодный оттенок на весь экран и изморозь по краям, растущая вглубь с силой.
			graphics.fill(0, 0, width, height, argb(cold * 0.10f, COLD_TINT_RGB));
			vignette(graphics, width, height, COLD_RGB, cold * 0.80f, COLD_DEPTH * (0.5f + 0.5f * cold));
		}
	}

	/**
	 * Рамка из вложенных прямоугольных колец: у края самый плотный цвет, к центру прозрачнее.
	 *
	 * @param strength максимальная непрозрачность у самого края, 0..1
	 * @param depth    глубина виньетки как доля меньшей стороны экрана
	 */
	private static void vignette(GuiGraphics g, int w, int h, int rgb, float strength, float depth) {
		int total = Math.max(BANDS, Math.round(Math.min(w, h) * depth));
		int thickness = Math.max(1, total / BANDS);

		for (int i = 0; i < BANDS; i++) {
			float falloff = 1.0f - i / (float) BANDS;
			int color = argb(strength * falloff * falloff, rgb);
			if ((color >>> 24) == 0) {
				continue;
			}
			int x1 = i * thickness;
			int y1 = i * thickness;
			int x2 = w - i * thickness;
			int y2 = h - i * thickness;
			if (x1 >= x2 || y1 >= y2) {
				break;
			}
			g.fill(x1, y1, x2, y1 + thickness, color);                    // верх
			g.fill(x1, y2 - thickness, x2, y2, color);                    // низ
			g.fill(x1, y1 + thickness, x1 + thickness, y2 - thickness, color); // лево
			g.fill(x2 - thickness, y1 + thickness, x2, y2 - thickness, color); // право
		}
	}

	private static int argb(float alpha, int rgb) {
		int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0f)));
		return (a << 24) | (rgb & 0xFFFFFF);
	}
}
