package dev.arqon.climate;

/**
 * Модель одежды: чистая логика без зависимостей от Minecraft.
 *
 * Каждый предмет принадлежит комплекту с двумя числами: защита от холода и защита от жары.
 * Положительное значение смягчает отклонение воздуха от комфортного диапазона, отрицательное усиливает.
 * Вклад предмета зависит от слота: пальто важнее ботинок.
 */
public final class Clothing {
	private Clothing() {
	}

	public enum Slot {
		HEAD(0.20),
		CHEST(0.40),
		LEGS(0.30),
		FEET(0.10);

		public final double weight;

		Slot(double weight) {
			this.weight = weight;
		}
	}

	/** Суммарная защита надетого: от холода и от жары. */
	public record Protection(double cold, double heat) {
		public static final Protection NONE = new Protection(0.0, 0.0);

		public Protection plus(Protection other) {
			return new Protection(cold + other.cold, heat + other.heat);
		}

		/** Ограничивает защиту разумными пределами, чтобы комбинации не ломали баланс. */
		public Protection clamped() {
			return new Protection(clamp(cold), clamp(heat));
		}

		private static double clamp(double v) {
			return Math.max(MIN_PROTECTION, Math.min(MAX_PROTECTION, v));
		}
	}

	/** Полный комплект в целом: что даёт, если надеть все 4 предмета. */
	public record SetProfile(double cold, double heat) {
		public Protection piece(Slot slot) {
			return new Protection(cold * slot.weight, heat * slot.weight);
		}
	}

	public static final double MIN_PROTECTION = -0.5;
	public static final double MAX_PROTECTION = 0.75;

	/** Шерсть: сильно греет, но в жару хуже голого тела. */
	public static final SetProfile WOOL = new SetProfile(0.55, -0.35);
	/** Лён: спасает от жары, от холода не защищает. */
	public static final SetProfile LINEN = new SetProfile(0.0, 0.40);
	/** Кожаная броня: слегка утепляет и слегка перегревает. */
	public static final SetProfile LEATHER = new SetProfile(0.30, -0.10);
	/** Металлическая броня: проводит холод и нагревается на солнце. */
	public static final SetProfile METAL = new SetProfile(-0.10, -0.15);

	/**
	 * Защита конкретной части тела: что надето именно на ней. Любой из комплектов может быть null (ничего не надето).
	 * Здесь берётся полное значение предмета, а не вклад слота: шапка целиком греет голову, пальто туловище,
	 * рукава руки (кисти и пальцы только краем рукава), штаны ноги, ботинки стопы.
	 */
	public static Protection forPart(dev.arqon.body.BodyPart part, SetProfile head, SetProfile chest, SetProfile legs, SetProfile feet) {
		Protection p = switch (part.group) {
			case HEAD -> cover(head, 1.0);
			case TORSO -> cover(chest, 1.0);
			case ARMS -> cover(chest, 0.92);
			case HANDS -> cover(chest, 0.50);
			case FINGERS -> cover(chest, 0.45);
			case LEGS -> part == dev.arqon.body.BodyPart.RIGHT_THIGH || part == dev.arqon.body.BodyPart.LEFT_THIGH
					? cover(legs, 1.0)
					: cover(legs, 0.8).plus(cover(feet, 0.2));
			case FEET -> cover(legs, 0.25).plus(cover(feet, 0.75));
		};
		return p.clamped();
	}

	private static Protection cover(SetProfile set, double weight) {
		return set == null ? Protection.NONE : new Protection(set.cold() * weight, set.heat() * weight);
	}

	/**
	 * Температура воздуха, которую тело ощущает на самом деле, с учётом одежды.
	 * В комфортном диапазоне одежда ничего не меняет; за его пределами смягчает или усиливает отклонение.
	 */
	public static double effectiveAmbient(double ambient, Protection protection) {
		Protection p = protection.clamped();
		if (ambient > ClimateModel.COMFORT_MAX) {
			return ClimateModel.COMFORT_MAX + (ambient - ClimateModel.COMFORT_MAX) * (1.0 - p.heat());
		}
		if (ambient < ClimateModel.COMFORT_MIN) {
			return ClimateModel.COMFORT_MIN - (ClimateModel.COMFORT_MIN - ambient) * (1.0 - p.cold());
		}
		return ambient;
	}
}
