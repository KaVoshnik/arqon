package dev.arqon.body;

/**
 * Части тела. Чистая логика без зависимостей от Minecraft.
 *
 * Порядок констант важен: он же индекс в сохранённых данных, поэтому новые части добавляем только в конец.
 * У каждой части свои тепловые свойства: насколько её греет кровь и как быстро она остывает или нагревается.
 */
public enum BodyPart {
	//            id               группа           сторона       кровь в холоде  инерция, с
	HEAD("head", Group.HEAD, Side.NONE, 0.35, 150),
	TORSO("torso", Group.TORSO, Side.NONE, 0.45, 300),

	RIGHT_UPPER_ARM("right_upper_arm", Group.ARMS, Side.RIGHT, 0.45, 200),
	LEFT_UPPER_ARM("left_upper_arm", Group.ARMS, Side.LEFT, 0.45, 200),
	RIGHT_FOREARM("right_forearm", Group.ARMS, Side.RIGHT, 0.35, 150),
	LEFT_FOREARM("left_forearm", Group.ARMS, Side.LEFT, 0.35, 150),
	RIGHT_HAND("right_hand", Group.HANDS, Side.RIGHT, 0.20, 90),
	LEFT_HAND("left_hand", Group.HANDS, Side.LEFT, 0.20, 90),

	RIGHT_THUMB("right_thumb", Group.FINGERS, Side.RIGHT, 0.12, 45),
	RIGHT_INDEX("right_index", Group.FINGERS, Side.RIGHT, 0.12, 45),
	RIGHT_MIDDLE("right_middle", Group.FINGERS, Side.RIGHT, 0.12, 45),
	RIGHT_RING("right_ring", Group.FINGERS, Side.RIGHT, 0.12, 45),
	RIGHT_PINKY("right_pinky", Group.FINGERS, Side.RIGHT, 0.10, 40),
	LEFT_THUMB("left_thumb", Group.FINGERS, Side.LEFT, 0.12, 45),
	LEFT_INDEX("left_index", Group.FINGERS, Side.LEFT, 0.12, 45),
	LEFT_MIDDLE("left_middle", Group.FINGERS, Side.LEFT, 0.12, 45),
	LEFT_RING("left_ring", Group.FINGERS, Side.LEFT, 0.12, 45),
	LEFT_PINKY("left_pinky", Group.FINGERS, Side.LEFT, 0.10, 40),

	RIGHT_THIGH("right_thigh", Group.LEGS, Side.RIGHT, 0.45, 240),
	LEFT_THIGH("left_thigh", Group.LEGS, Side.LEFT, 0.45, 240),
	RIGHT_SHIN("right_shin", Group.LEGS, Side.RIGHT, 0.30, 180),
	LEFT_SHIN("left_shin", Group.LEGS, Side.LEFT, 0.30, 180),
	RIGHT_FOOT("right_foot", Group.FEET, Side.RIGHT, 0.18, 100),
	LEFT_FOOT("left_foot", Group.FEET, Side.LEFT, 0.18, 100);

	/** Части, которые одинаково описываются в сообщениях. */
	public enum Group {
		HEAD("head"),
		TORSO("torso"),
		ARMS("arms"),
		HANDS("hands"),
		FINGERS("fingers"),
		LEGS("legs"),
		FEET("feet");

		public final String id;

		Group(String id) {
			this.id = id;
		}
	}

	public enum Side {
		NONE,
		LEFT,
		RIGHT
	}

	public static final int COUNT = values().length;

	public final String id;
	public final Group group;
	public final Side side;
	/** Доля тепла тела, которая доходит до части на холоде (сосуды сужены). Чем меньше, тем легче замёрзнуть. */
	public final double coldBlood;
	/** Постоянная времени, с: за столько секунд часть проходит около 63 % пути к равновесной температуре. */
	public final double timeConstant;

	BodyPart(String id, Group group, Side side, double coldBlood, double timeConstant) {
		this.id = id;
		this.group = group;
		this.side = side;
		this.coldBlood = coldBlood;
		this.timeConstant = timeConstant;
	}

	/** Часть по идентификатору или null. */
	public static BodyPart byId(String id) {
		for (BodyPart part : values()) {
			if (part.id.equals(id)) {
				return part;
			}
		}
		return null;
	}

	/** Кисть этой стороны. */
	public static BodyPart hand(Side side) {
		return side == Side.LEFT ? LEFT_HAND : RIGHT_HAND;
	}

	/** Пальцы руки этой стороны по порядку: большой, указательный, средний, безымянный, мизинец. */
	public static BodyPart[] fingers(Side side) {
		return side == Side.LEFT
				? new BodyPart[]{LEFT_THUMB, LEFT_INDEX, LEFT_MIDDLE, LEFT_RING, LEFT_PINKY}
				: new BodyPart[]{RIGHT_THUMB, RIGHT_INDEX, RIGHT_MIDDLE, RIGHT_RING, RIGHT_PINKY};
	}
}
