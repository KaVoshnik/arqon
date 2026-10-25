package dev.arqon.body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Состояние всех частей тела: температура, обморожение и ожог каждой. Чистая логика без зависимостей от Minecraft.
 * Обморожение и ожог 0..100, стадии см. {@link Frostbite.Stage}.
 */
public final class BodyState {
	public final double[] temp = new double[BodyPart.COUNT];
	public final double[] frost = new double[BodyPart.COUNT];
	public final double[] burn = new double[BodyPart.COUNT];

	/** Здоровое тело в покое. */
	public static BodyState resting() {
		BodyState state = new BodyState();
		for (BodyPart part : BodyPart.values()) {
			state.temp[part.ordinal()] = quantize(PartThermals.resting(part));
		}
		return state;
	}

	public BodyState copy() {
		BodyState c = new BodyState();
		System.arraycopy(temp, 0, c.temp, 0, temp.length);
		System.arraycopy(frost, 0, c.frost, 0, frost.length);
		System.arraycopy(burn, 0, c.burn, 0, burn.length);
		return c;
	}

	public boolean sameAs(BodyState other) {
		return Arrays.equals(temp, other.temp) && Arrays.equals(frost, other.frost) && Arrays.equals(burn, other.burn);
	}

	/** Температуры хранятся с точностью до сотой градуса: этого хватает, а числа получаются короткими. */
	public static double quantize(double t) {
		return Math.round(t * 100.0) / 100.0;
	}

	/** Плоский список для сохранения и сети: температуры, потом обморожение, потом ожоги. */
	public List<Double> toList() {
		List<Double> list = new ArrayList<>(BodyPart.COUNT * 3);
		for (double v : temp) {
			list.add(v);
		}
		for (double v : frost) {
			list.add(v);
		}
		for (double v : burn) {
			list.add(v);
		}
		return List.copyOf(list);
	}

	/** Состояние из списка. Если размер не тот (старое сохранение), начинаем со здорового тела. */
	public static BodyState fromList(List<Double> list) {
		if (list == null || list.size() != BodyPart.COUNT * 3) {
			return resting();
		}
		BodyState state = new BodyState();
		int n = BodyPart.COUNT;
		for (int i = 0; i < n; i++) {
			state.temp[i] = finiteOr(list.get(i), PartThermals.resting(BodyPart.values()[i]));
			state.frost[i] = clampDamage(list.get(n + i));
			state.burn[i] = clampDamage(list.get(2 * n + i));
		}
		return state;
	}

	private static double finiteOr(double v, double fallback) {
		return Double.isNaN(v) || Double.isInfinite(v) ? fallback : Math.max(PartThermals.MIN_TEMP, Math.min(PartThermals.MAX_TEMP, v));
	}

	private static double clampDamage(double v) {
		return Double.isNaN(v) ? 0.0 : Math.max(0.0, Math.min(Frostbite.MAX, v));
	}

	/** Худшее из обморожения и ожога у части. */
	public double injury(BodyPart part) {
		return Math.max(frost[part.ordinal()], burn[part.ordinal()]);
	}

	/**
	 * Состояние ног для эффекта: на каждой ноге бедро, голень и стопа (стопа весит вдвое), среднее по двум ногам.
	 *
	 * @param values обморожение или ожоги по частям
	 */
	public static double legs(double[] values) {
		return (leg(values, BodyPart.RIGHT_THIGH, BodyPart.RIGHT_SHIN, BodyPart.RIGHT_FOOT)
				+ leg(values, BodyPart.LEFT_THIGH, BodyPart.LEFT_SHIN, BodyPart.LEFT_FOOT)) / 2.0;
	}

	private static double leg(double[] v, BodyPart thigh, BodyPart shin, BodyPart foot) {
		return (v[thigh.ordinal()] + v[shin.ordinal()] + 2.0 * v[foot.ordinal()]) / 4.0;
	}

	/** Состояние рук для эффекта: на каждой кисть (40 %) и пальцы (60 %), среднее по двум рукам. */
	public static double hands(double[] values) {
		return (hand(values, BodyPart.Side.RIGHT) + hand(values, BodyPart.Side.LEFT)) / 2.0;
	}

	private static double hand(double[] v, BodyPart.Side side) {
		double fingers = 0.0;
		for (BodyPart f : BodyPart.fingers(side)) {
			fingers += v[f.ordinal()];
		}
		return 0.4 * v[BodyPart.hand(side).ordinal()] + 0.6 * (fingers / 5.0);
	}
}
