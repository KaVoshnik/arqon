package dev.arqon.climate;

/**
 * Состояние тела игрока в зависимости от температуры тела.
 * Чистая логика без зависимостей от Minecraft, чтобы её было легко проверять.
 *
 * Пороги сравниваются по значению, округлённому до 0.1 °C, то есть ровно с тем,
 * что игрок видит на градуснике.
 */
public enum ThermalState {
	NORMAL,
	/** 38.0 – 39.9 °C */
	ASPHYXIA,
	/** 40.0 – 41.9 °C */
	HYPERTHERMIA,
	/** 42.0 °C и выше: мгновенная смерть */
	FATAL_HEAT,
	/** 32.1 – 35.0 °C */
	MILD_HYPOTHERMIA,
	/** 28.0 – 32.0 °C */
	MODERATE_HYPOTHERMIA,
	/** ниже 28.0 °C: мгновенная смерть */
	FATAL_COLD;

	public static final double NORMAL_BODY_TEMPERATURE = 36.6;

	public static ThermalState of(double bodyTemperature) {
		double t = roundToTenth(bodyTemperature);
		if (t >= 42.0) return FATAL_HEAT;
		if (t >= 40.0) return HYPERTHERMIA;
		if (t >= 38.0) return ASPHYXIA;
		if (t < 28.0) return FATAL_COLD;
		if (t <= 32.0) return MODERATE_HYPOTHERMIA;
		if (t <= 35.0) return MILD_HYPOTHERMIA;
		return NORMAL;
	}

	public boolean isFatal() {
		return this == FATAL_HEAT || this == FATAL_COLD;
	}

	public static double roundToTenth(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
