package dev.arqon.body;

/**
 * Температура частей тела: чистая логика без зависимостей от Minecraft.
 *
 * Каждая часть тянется к равновесной температуре между воздухом, который она чувствует, и теплом тела.
 * Сколько тепла доходит, зависит от притока крови: на холоде сосуды сужаются и конечности остаются наедине с морозом,
 * в тепле расширяются, в сильную жару кровь уже не справляется. Предмет в руке добавляет или отнимает градусы в секунду.
 */
public final class PartThermals {
	private PartThermals() {
	}

	/** Температура воздуха (°C), ниже которой сосуды сужены, и выше которой полностью расширены. */
	public static final double VASO_FROM = 10.0;
	public static final double VASO_TO = 25.0;
	/** Приток крови в тепле, торс, остальные. */
	public static final double WARM_BLOOD = 0.85;
	public static final double WARM_BLOOD_TORSO = 0.95;
	/** Выше этого воздуха кровь перестаёт справляться: приток падает до (1 - OVERHEAT_LOSS) на OVERHEAT_SPAN градусах. */
	public static final double OVERHEAT_FROM = 35.0;
	public static final double OVERHEAT_SPAN = 10.0;
	public static final double OVERHEAT_LOSS = 0.4;

	public static final double MIN_TEMP = -40.0;
	public static final double MAX_TEMP = 90.0;

	/** Доля тепла тела, доходящая до части при данной температуре воздуха. */
	public static double bloodFlow(BodyPart part, double ambient) {
		double warm = part == BodyPart.TORSO ? WARM_BLOOD_TORSO : WARM_BLOOD;
		double t = smooth((ambient - VASO_FROM) / (VASO_TO - VASO_FROM));
		double flow = part.coldBlood + (warm - part.coldBlood) * t;
		double over = clamp01((ambient - OVERHEAT_FROM) / OVERHEAT_SPAN);
		return flow * (1.0 - OVERHEAT_LOSS * over);
	}

	/** Температура, к которой тянется часть, если ничего не мешает. */
	public static double equilibrium(BodyPart part, double ambient, double core) {
		return ambient + bloodFlow(part, ambient) * (core - ambient);
	}

	/** Температура, с которой начинается жизнь и после смерти: воздух 22 °C, тело 36.6 °C. */
	public static double resting(BodyPart part) {
		return equilibrium(part, 22.0, 36.6);
	}

	/**
	 * Новая температура части.
	 *
	 * @param ambient воздух, который чувствует именно эта часть (с учётом одежды на ней)
	 * @param core    температура тела
	 * @param flux    нагрев (+) или охлаждение (-) от предмета в руке, градусов в секунду; 0 если ничего нет
	 */
	public static double step(BodyPart part, double temp, double ambient, double core, double flux, double seconds) {
		double target = equilibrium(part, ambient, core);
		double next = temp + (target - temp) * (1.0 - Math.exp(-seconds / part.timeConstant)) + flux * seconds;
		return Math.max(MIN_TEMP, Math.min(MAX_TEMP, next));
	}

	/** Как человек ощущает температуру части. */
	public enum Feel {
		FROZEN, ICY, COLD, COOL, NORMAL, WARM, HOT, SCALDING;

		public static Feel of(double temp) {
			if (temp < 0) {
				return FROZEN;
			}
			if (temp < 8) {
				return ICY;
			}
			if (temp < 18) {
				return COLD;
			}
			if (temp < 26) {
				return COOL;
			}
			if (temp < 35) {
				return NORMAL;
			}
			if (temp < 40) {
				return WARM;
			}
			if (temp < 44) {
				return HOT;
			}
			return SCALDING;
		}

		public String id() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	private static double smooth(double x) {
		double t = clamp01(x);
		return t * t * (3 - 2 * t);
	}

	private static double clamp01(double v) {
		return Math.max(0.0, Math.min(1.0, v));
	}
}
