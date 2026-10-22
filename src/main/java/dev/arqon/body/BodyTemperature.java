package dev.arqon.body;

import net.minecraft.world.entity.player.Player;

import dev.arqon.climate.ThermalState;

/** Чтение и запись температуры тела игрока. */
public final class BodyTemperature {
	private BodyTemperature() {
	}

	public static double get(Player player) {
		return player.getAttachedOrElse(ModAttachments.BODY_TEMPERATURE, ThermalState.NORMAL_BODY_TEMPERATURE);
	}

	public static void set(Player player, double celsius) {
		player.setAttached(ModAttachments.BODY_TEMPERATURE, celsius);
	}
}
