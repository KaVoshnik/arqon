package dev.arqon.body;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import dev.arqon.Arqon;
import dev.arqon.climate.ThermalState;

public final class ModAttachments {
	private ModAttachments() {
	}

	/**
	 * Температура тела игрока (°C). Сохраняется вместе с игроком.
	 * Без copyOnDeath: после смерти температура сбрасывается к 36.6.
	 */
	public static final AttachmentType<Double> BODY_TEMPERATURE = AttachmentRegistry.create(
			Arqon.id("body_temperature"),
			builder -> builder
					.initializer(() -> ThermalState.NORMAL_BODY_TEMPERATURE)
					.persistent(Codec.DOUBLE)
	);

	/** Вызывается из инициализатора мода, чтобы тип зарегистрировался заранее. */
	public static void init() {
	}
}
