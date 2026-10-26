package dev.arqon.body;

import java.util.function.IntUnaryOperator;

/**
 * Занозы: чистая логика без зависимостей от Minecraft.
 *
 * Занозы хранятся битовой маской по 10 пальцам: биты 0..4 пальцы правой руки (большой ... мизинец),
 * биты 5..9 пальцы левой. В одном пальце одна заноза.
 */
public final class Splinters {
	private Splinters() {
	}

	/** Шанс получить занозы при рубке дерева без топора. */
	public static final double CHANCE = 0.05;
	/** Во сколько раз перчатки снижают шанс. */
	public static final double GLOVE_FACTOR = 0.25;
	/** Вес количества заноз 1..5: чем больше, тем реже. Сумма 100. */
	private static final int[] WEIGHTS = {50, 25, 13, 8, 4};
	public static final int MAX_PER_EVENT = WEIGHTS.length;
	public static final int FINGER_COUNT = 10;
	private static final int ALL = (1 << FINGER_COUNT) - 1;

	/** Сколько заноз выпало (1..5) по случайному числу r из [0, 1). */
	public static int rollCount(double r) {
		double acc = 0.0;
		for (int i = 0; i < WEIGHTS.length; i++) {
			acc += WEIGHTS[i] / 100.0;
			if (r < acc) {
				return i + 1;
			}
		}
		return WEIGHTS.length;
	}

	/** Номер пальца 0..9 для пальца руки (side 0 правая, 1 левая) и номера пальца 0..4. */
	public static int index(boolean left, int finger) {
		return (left ? 5 : 0) + finger;
	}

	public static boolean has(int mask, int index) {
		return (mask >> index & 1) != 0;
	}

	public static int count(int mask) {
		return Integer.bitCount(mask & ALL);
	}

	public static int remove(int mask, int index) {
		return mask & ~(1 << index) & ALL;
	}

	/**
	 * Добавляет count заноз в свободные пальцы одной руки, случайные пальцы выбирает nextInt (n -> число из [0, n)).
	 * Если свободных пальцев меньше, чем заноз, заполняются все свободные.
	 */
	public static int add(int mask, boolean left, int count, IntUnaryOperator nextInt) {
		int result = mask & ALL;
		for (int n = 0; n < count; n++) {
			int[] free = new int[5];
			int freeCount = 0;
			for (int f = 0; f < 5; f++) {
				if (!has(result, index(left, f))) {
					free[freeCount++] = f;
				}
			}
			if (freeCount == 0) {
				break;
			}
			int pick = free[nextInt.applyAsInt(freeCount)];
			result |= 1 << index(left, pick);
		}
		return result;
	}

	/** Уровень дебаффа (амплитуда эффекта) по маске: 0..4, или -1 если заноз нет. */
	public static int effectLevel(int mask) {
		int c = count(mask);
		return c == 0 ? -1 : Math.min(c, MAX_PER_EVENT) - 1;
	}
}
