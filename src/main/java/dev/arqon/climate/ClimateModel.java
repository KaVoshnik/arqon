package dev.arqon.climate;

/**
 * Модель температуры: чистая математика без зависимостей от Minecraft.
 * Все «ручки» для настройки баланса собраны здесь в константах.
 *
 * Температура воздуха считается «ощущаемой» и зависит от места: биом, время суток, погода,
 * тень и крыша над головой, глубина под землёй, высота, ближайший огонь и лёд.
 */
public final class ClimateModel {
	private ClimateModel() {
	}

	// ---------- Биом ----------

	/** Ванильная температура биома, которой соответствует {@link #REFERENCE_AIR_TEMPERATURE} (равнины). */
	public static final double REFERENCE_BIOME_TEMPERATURE = 0.8;
	/** Среднесуточная температура воздуха (°C) в «эталонном» биоме. */
	public static final double REFERENCE_AIR_TEMPERATURE = 15.0;
	/** Сколько °C приходится на 1.0 ванильной температуры биома. */
	public static final double DEGREES_PER_BIOME_UNIT = 16.0;
	/** Добавка к среднесуточной температуре в жарких измерениях (Незер). */
	public static final double ULTRAWARM_OFFSET = 6.0;

	// ---------- Время суток и солнце ----------

	/** Суточное колебание воздуха (°C): днём теплее, ночью холоднее на столько. */
	public static final double AIR_SWING = 6.0;
	/** Дополнительный нагрев прямым солнцем (°C) в полдень под открытым небом в жарком биоме. */
	public static final double SUN_LOAD = 6.0;
	/**
	 * Солнце греет тем сильнее, чем теплее биом: доля {@link #SUN_LOAD} линейно растёт от
	 * {@link #SUN_MIN_SCALE} (при среднесуточной температуре {@link #SUN_SCALE_FROM}) до 1
	 * (при {@link #SUN_SCALE_FROM} + {@link #SUN_SCALE_RANGE}).
	 */
	public static final double SUN_SCALE_FROM = 5.0;
	public static final double SUN_SCALE_RANGE = 25.0;
	public static final double SUN_MIN_SCALE = 0.2;
	/** Ветер: под открытым небом в прохладном воздухе на столько холоднее, чем за укрытием. */
	public static final double WIND_CHILL = 3.0;
	/** Ветер выхолаживает, пока воздух холоднее этой температуры (°C), и полностью при на 10 °C холоднее. */
	public static final double WIND_CHILL_BELOW = 15.0;
	/** Во сколько раз под крышей или в тени слабее суточное колебание (в тени ночью теплее, днём прохладнее). */
	public static final double SHADE_SWING_FACTOR = 0.6;
	/** Тик суток с максимальной температурой (8000 ≈ 14:00). */
	public static final long WARMEST_TICK = 8000L;
	public static final long DAY_LENGTH = 24000L;

	// ---------- Погода (только под открытым небом) ----------

	public static final double RAIN_OFFSET = -3.0;
	public static final double THUNDER_OFFSET = -5.0;
	/** Какая доля прямого солнца доходит сквозь тучи. */
	public static final double RAIN_SUN_FACTOR = 0.5;
	public static final double THUNDER_SUN_FACTOR = 0.3;

	// ---------- Высота и глубина ----------

	/** Выше этой высоты (Y) воздух остывает. */
	public static final double LAPSE_START_Y = 70.0;
	/** На сколько °C холоднее на каждый блок выше {@link #LAPSE_START_Y}. */
	public static final double LAPSE_PER_BLOCK = 0.05;
	/** Сколько блоков над головой считается просто крышей или кроной, а не «под землёй». */
	public static final int UNDERGROUND_START = 6;
	/** На протяжении скольких блоков глубины температура плавно переходит к пещерной. */
	public static final int UNDERGROUND_RANGE = 24;
	/** Температура глубокой пещеры (°C) и доля влияния биома на неё. */
	public static final double CAVE_BASE = 12.0;
	public static final double CAVE_BIOME_INFLUENCE = 0.25;

	// ---------- Источники тепла и холода ----------

	public static final double MAX_HEAT_FROM_SOURCES = 30.0;
	public static final double MAX_COLD_FROM_SOURCES = 15.0;

	/**
	 * Условия в точке, где находится игрок.
	 *
	 * @param biomeTemperature ванильная температура биома (Biome#getBaseTemperature)
	 * @param dayTime          время суток в тиках (любое число, берётся по модулю 24000)
	 * @param hasSkyLight      есть ли в измерении небо (в Незере и Крае нет суток, погоды, теней и глубины)
	 * @param ultraWarm        жаркое измерение (Незер)
	 * @param biomeHasPrecip   выпадают ли в биоме осадки (в пустыне дождя нет)
	 * @param coverDepth       сколько блоков от головы до открытого неба (0 = небо открыто)
	 * @param y                высота игрока
	 * @param netSources       суммарный вклад ближайших источников тепла (+) и холода (-), °C
	 */
	public record Conditions(
			double biomeTemperature,
			long dayTime,
			boolean hasSkyLight,
			boolean ultraWarm,
			boolean biomeHasPrecip,
			boolean raining,
			boolean thundering,
			int coverDepth,
			double y,
			double netSources) {
	}

	public static double perceivedTemperature(Conditions c) {
		double mean = REFERENCE_AIR_TEMPERATURE
				+ (c.biomeTemperature() - REFERENCE_BIOME_TEMPERATURE) * DEGREES_PER_BIOME_UNIT;
		if (c.ultraWarm()) {
			mean += ULTRAWARM_OFFSET;
		}

		// В Незере и Крае нет ни суток, ни погоды, ни теней: только биом и источники.
		if (!c.hasSkyLight()) {
			return mean + c.netSources();
		}

		boolean exposed = c.coverDepth() <= 0;
		double phase = Math.cos(2.0 * Math.PI * (Math.floorMod(c.dayTime(), DAY_LENGTH) - WARMEST_TICK) / DAY_LENGTH);

		boolean precipitating = c.biomeHasPrecip() && (c.raining() || c.thundering());
		double sky = 1.0;
		if (precipitating) {
			sky = c.thundering() ? THUNDER_SUN_FACTOR : RAIN_SUN_FACTOR;
		}

		double surface = mean + AIR_SWING * phase * (exposed ? 1.0 : SHADE_SWING_FACTOR);
		if (exposed) {
			double sunScale = Math.max(SUN_MIN_SCALE, clamp01((mean - SUN_SCALE_FROM) / SUN_SCALE_RANGE));
			surface += SUN_LOAD * sunScale * Math.max(0.0, phase) * sky;
			if (precipitating) {
				surface += c.thundering() ? THUNDER_OFFSET : RAIN_OFFSET;
			}
			surface -= WIND_CHILL * clamp01((WIND_CHILL_BELOW - surface) / 10.0);
		}
		surface -= LAPSE_PER_BLOCK * Math.max(0.0, c.y() - LAPSE_START_Y);

		double underground = clamp01((c.coverDepth() - UNDERGROUND_START) / (double) UNDERGROUND_RANGE);
		double cave = CAVE_BASE + (mean - CAVE_BASE) * CAVE_BIOME_INFLUENCE;
		double base = surface + (cave - surface) * underground;

		return base + c.netSources();
	}

	/** Складывает тепло и холод от источников с ограничением по максимуму для каждого. */
	public static double combineSources(double heat, double cold) {
		return Math.min(heat, MAX_HEAT_FROM_SOURCES) - Math.min(cold, MAX_COLD_FROM_SOURCES);
	}

	// ---------- Температура тела ----------

	/** Нижняя граница комфортного воздуха (°C): ниже тело остывает. */
	public static final double COMFORT_MIN = 10.0;
	/** Верхняя граница комфортного воздуха (°C): выше тело перегревается. */
	public static final double COMFORT_MAX = 32.0;

	/** Базовая скорость изменения температуры тела: 0.1 °C за 2 минуты (2400 тиков). */
	public static final double BODY_DEGREES_PER_STEP = 0.1;
	public static final double BODY_STEP_TICKS = 2 * 60 * 20;

	/** При каком отклонении от комфорта скорость равна базовой. Меньше отклонение: медленнее, больше: быстрее. */
	public static final double DRIFT_REFERENCE_DEVIATION = 10.0;
	public static final double DRIFT_MIN_FACTOR = 0.25;
	public static final double DRIFT_MAX_FACTOR = 2.0;

	/** Множитель скорости дрейфа для данного отклонения от комфортного диапазона (°C). */
	public static double driftFactor(double deviation) {
		return Math.max(DRIFT_MIN_FACTOR, Math.min(DRIFT_MAX_FACTOR, deviation / DRIFT_REFERENCE_DEVIATION));
	}

	/**
	 * Новая температура тела спустя {@code elapsedTicks} тиков при данной температуре воздуха.
	 * Вне комфортного диапазона тело уходит в сторону воздуха (тем быстрее, чем дальше от комфорта),
	 * внутри него возвращается к 36.6 с базовой скоростью.
	 */
	public static double nextBodyTemperature(double body, double ambient, double elapsedTicks) {
		double delta = BODY_DEGREES_PER_STEP * elapsedTicks / BODY_STEP_TICKS;
		double normal = ThermalState.NORMAL_BODY_TEMPERATURE;

		if (ambient > COMFORT_MAX) {
			return body + delta * driftFactor(ambient - COMFORT_MAX);
		}
		if (ambient < COMFORT_MIN) {
			return body - delta * driftFactor(COMFORT_MIN - ambient);
		}
		if (body > normal) {
			return Math.max(normal, body - delta);
		}
		if (body < normal) {
			return Math.min(normal, body + delta);
		}
		return body;
	}

	private static double clamp01(double v) {
		return Math.max(0.0, Math.min(1.0, v));
	}
}
