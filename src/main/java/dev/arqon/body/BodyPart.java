package dev.arqon.body;

/**
 * Части тела. Чистая логика без зависимостей от Minecraft.
 *
 * Порядок констант важен: он же индекс в сохранённых данных, поэтому новые части добавляем только в конец.
 */
public enum BodyPart {
	HEAD("head", Group.HEAD, 0.5),
	TORSO("torso", Group.TORSO, 0.15),
	LEFT_ARM("left_arm", Group.ARMS, 1.0),
	RIGHT_ARM("right_arm", Group.ARMS, 1.0),
	LEFT_LEG("left_leg", Group.LEGS, 1.0),
	RIGHT_LEG("right_leg", Group.LEGS, 1.0);

	/** Части, которые одинаково влияют на игровую механику и одинаково описываются в сообщениях. */
	public enum Group {
		HEAD("head"),
		TORSO("torso"),
		ARMS("arms"),
		LEGS("legs");

		public final String id;

		Group(String id) {
			this.id = id;
		}
	}

	public static final int COUNT = values().length;

	public final String id;
	public final Group group;
	/** Насколько быстро часть вымерзает: кисти и стопы первыми, туловище с его теплом последним. */
	public final double susceptibility;

	BodyPart(String id, Group group, double susceptibility) {
		this.id = id;
		this.group = group;
		this.susceptibility = susceptibility;
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
}
