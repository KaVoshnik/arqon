package dev.arqon.body;

/**
 * Ожог от перегрева части тела: чистая логика без зависимостей от Minecraft.
 * Растёт, пока часть горячее порога, проходит медленно и только до тяжёлой стадии. Стадии те же, что у обморожения.
 */
public final class Burn {
	private Burn() {
	}

	/** Выше этой температуры (°C) ткани начинают страдать. */
	public static final double BURN_ABOVE = 44.0;
	/** Прирост за секунду на каждый градус выше порога. */
	public static final double GAIN_PER_DEGREE_SECOND = 0.14;
	/** Ожог заживает, только если часть остыла ниже этой температуры. */
	public static final double HEAL_BELOW = 40.0;
	public static final double HEAL_PER_SECOND = 0.05;

	public static double next(double burn, double partTemp, double seconds) {
		if (partTemp > BURN_ABOVE) {
			return Math.min(Frostbite.MAX, burn + (partTemp - BURN_ABOVE) * GAIN_PER_DEGREE_SECOND * seconds);
		}
		if (partTemp < HEAL_BELOW && burn < Frostbite.SEVERE_FROM) {
			return Math.max(0.0, burn - HEAL_PER_SECOND * seconds);
		}
		return burn;
	}
}
