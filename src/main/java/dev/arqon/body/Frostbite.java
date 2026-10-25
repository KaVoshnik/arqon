package dev.arqon.body;

/**
 * Модель обморожения одной части тела: чистая логика без зависимостей от Minecraft.
 *
 * Состояние части это число 0..100. Растёт, пока сама часть холоднее порога, и тем быстрее, чем она холоднее.
 * Отогревается только в тепле.
 * Тяжёлое обморожение (от 75) само не проходит: оно требует лечения, которого пока нет в игре.
 */
public final class Frostbite {
	private Frostbite() {
	}

	/** Ниже этой температуры (°C) части (не воздуха, а самой части) ткани начинают страдать. */
	public static final double FROST_BELOW = 4.0;
	/** Прирост за секунду на каждый градус ниже порога. */
	public static final double GAIN_PER_DEGREE_SECOND = 0.03;

	/** Часть отогревается, только если ей тепло (°C) и тело не переохлаждено. */
	public static final double RECOVER_ABOVE = 12.0;
	public static final double RECOVER_BODY_ABOVE = 35.5;
	public static final double RECOVERY_PER_SECOND = 0.1;

	public static final double NUMB_FROM = 25.0;
	public static final double FROSTBITTEN_FROM = 50.0;
	public static final double SEVERE_FROM = 75.0;
	public static final double MAX = 100.0;

	public enum Stage {
		HEALTHY,
		NUMB,
		FROSTBITTEN,
		SEVERE,
		NECROTIC;

		public static Stage of(double frost) {
			if (frost >= MAX) {
				return NECROTIC;
			}
			if (frost >= SEVERE_FROM) {
				return SEVERE;
			}
			if (frost >= FROSTBITTEN_FROM) {
				return FROSTBITTEN;
			}
			if (frost >= NUMB_FROM) {
				return NUMB;
			}
			return HEALTHY;
		}

		/** Уровень игрового эффекта (0, 1, 2) или -1, если эффекта ещё нет. Эффект начинается с обморожения. */
		public int effectLevel() {
			return ordinal() >= FROSTBITTEN.ordinal() ? ordinal() - FROSTBITTEN.ordinal() : -1;
		}

		public String id() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	/**
	 * Новое состояние части после заданного времени.
	 *
	 * @param partTemp температура самой части
	 * @param body     температура тела
	 */
	public static double next(double frost, double partTemp, double body, double seconds) {
		if (partTemp < FROST_BELOW) {
			return Math.min(MAX, frost + (FROST_BELOW - partTemp) * GAIN_PER_DEGREE_SECOND * seconds);
		}
		if (partTemp >= RECOVER_ABOVE && body >= RECOVER_BODY_ABOVE && frost < SEVERE_FROM) {
			return Math.max(0.0, frost - RECOVERY_PER_SECOND * seconds);
		}
		return frost;
	}

	/** Обмороженная голова: бег тратит больше сил. Дополнительное истощение за секунду бега на уровне 0, 1, 2. */
	public static double sprintExhaustionPerSecond(int level) {
		return level < 0 ? 0.0 : 0.2 * (level + 1);
	}

	/** Обмороженное туловище: во сколько раз быстрее остывает тело (1 без обморожения). */
	public static double coolingFactor(int level) {
		return level < 0 ? 1.0 : 1.0 + 0.25 * (level + 1);
	}

	/** Туловище от этого значения обмороженности (из 100) отдаёт сердцебиение в уши. */
	public static final double HEARTBEAT_FROM = 85.0;

	/** Среднее состояние нескольких частей (для эффектов на обе руки или обе ноги). */
	public static double average(double... values) {
		if (values.length == 0) {
			return 0.0;
		}
		double sum = 0.0;
		for (double v : values) {
			sum += v;
		}
		return sum / values.length;
	}
}
