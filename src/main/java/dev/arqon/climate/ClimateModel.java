package dev.arqon.climate;

/**
 * Модель температуры: чистая математика без зависимостей от Minecraft.
 * Все «ручки» для настройки баланса собраны здесь в константах.
 */
public final class ClimateModel {
	private ClimateModel() {
	}

	// ---------- Температура воздуха ----------

	/** Ванильная температура биома, которой соответствует {@link #REFERENCE_AIR_TEMPERATURE} (равнины). */
	public static final double REFERENCE_BIOME_TEMPERATURE = 0.8;
	/** Температура воздуха (°C) в «эталонном» биоме в среднем за сутки. */
	public static final double REFERENCE_AIR_TEMPERATURE = 15.0;
	/** Сколько °C приходится на 1.0 ванильной температуры биома. */
	public static final double DEGREES_PER_BIOME_UNIT = 20.0;

	/** Амплитуда суточного колебания (°C): днём теплее на столько, ночью холоднее на столько. */
	public static final double DAY_NIGHT_AMPLITUDE = 7.0;
	/** Тик суток с максимальной температурой (8000 ≈ 14:00). */
	public static final long WARMEST_TICK = 8000L;
	public static final long DAY_LENGTH = 24000L;

	public static final double RAIN_OFFSET = -3.0;
	public static final double THUNDER_OFFSET = -5.0;

	/**
	 * @param biomeTemperature  ванильная температура биома (Biome#getBaseTemperature)
	 * @param dayTime           время суток в тиках (любое число, берётся по модулю 24000)
	 * @param hasSkyLight       есть ли в измерении небо (в Незере и Крае суток и погоды нет)
	 * @param biomeHasPrecip    выпадают ли в биоме осадки (в пустыне дождя нет)
	 */
	public static double ambientTemperature(
			double biomeTemperature,
			long dayTime,
			boolean hasSkyLight,
			boolean biomeHasPrecip,
			boolean raining,
			boolean thundering) {
		double t = REFERENCE_AIR_TEMPERATURE
				+ (biomeTemperature - REFERENCE_BIOME_TEMPERATURE) * DEGREES_PER_BIOME_UNIT;

		if (hasSkyLight) {
			long time = Math.floorMod(dayTime, DAY_LENGTH);
			t += DAY_NIGHT_AMPLITUDE * Math.cos(2.0 * Math.PI * (time - WARMEST_TICK) / DAY_LENGTH);

			if (biomeHasPrecip) {
				if (thundering) {
					t += THUNDER_OFFSET;
				} else if (raining) {
					t += RAIN_OFFSET;
				}
			}
		}
		return t;
	}

	// ---------- Температура тела ----------

	/** Нижняя граница комфортного воздуха (°C): ниже тело остывает. */
	public static final double COMFORT_MIN = 10.0;
	/** Верхняя граница комфортного воздуха (°C): выше тело перегревается. */
	public static final double COMFORT_MAX = 30.0;

	/** Скорость изменения температуры тела: 0.1 °C за 2 минуты (2400 тиков). */
	public static final double BODY_DEGREES_PER_STEP = 0.1;
	public static final double BODY_STEP_TICKS = 2 * 60 * 20;

	/**
	 * Новая температура тела спустя {@code elapsedTicks} тиков при данной температуре воздуха.
	 * Вне комфортного диапазона тело уходит в сторону воздуха, внутри него возвращается к 36.6.
	 */
	public static double nextBodyTemperature(double body, double ambient, double elapsedTicks) {
		double delta = BODY_DEGREES_PER_STEP * elapsedTicks / BODY_STEP_TICKS;
		double normal = ThermalState.NORMAL_BODY_TEMPERATURE;

		if (ambient > COMFORT_MAX) {
			return body + delta;
		}
		if (ambient < COMFORT_MIN) {
			return body - delta;
		}
		if (body > normal) {
			return Math.max(normal, body - delta);
		}
		if (body < normal) {
			return Math.min(normal, body + delta);
		}
		return body;
	}
}
