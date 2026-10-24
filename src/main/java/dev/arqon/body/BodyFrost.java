package dev.arqon.body;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

/** Чтение и запись обморожения частей тела игрока. На клиенте значения приходят по сети. */
public final class BodyFrost {
	private BodyFrost() {
	}

	/** Копия состояния всех частей по порядку {@link BodyPart}. Недостающие значения считаются нулём. */
	public static double[] get(Player player) {
		List<Double> stored = player.getAttachedOrElse(ModAttachments.BODY_FROST, List.of());
		double[] result = new double[BodyPart.COUNT];
		for (int i = 0; i < result.length && i < stored.size(); i++) {
			result[i] = clamp(stored.get(i));
		}
		return result;
	}

	public static double get(Player player, BodyPart part) {
		return get(player)[part.ordinal()];
	}

	public static void set(Player player, double[] values) {
		List<Double> list = new ArrayList<>(BodyPart.COUNT);
		for (int i = 0; i < BodyPart.COUNT; i++) {
			list.add(clamp(i < values.length ? values[i] : 0.0));
		}
		player.setAttached(ModAttachments.BODY_FROST, List.copyOf(list));
	}

	private static double clamp(double v) {
		if (Double.isNaN(v)) {
			return 0.0;
		}
		return Math.max(0.0, Math.min(Frostbite.MAX, v));
	}
}
