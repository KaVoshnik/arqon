package dev.arqon.body;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.network.codec.ByteBufCodecs;

import dev.arqon.Arqon;
import dev.arqon.climate.ThermalState;

public final class ModAttachments {
	private ModAttachments() {
	}

	/** Комфортная температура воздуха, которую видит клиент, пока значения с сервера ещё нет. */
	public static final double DEFAULT_AMBIENT = 20.0;

	/**
	 * Температура тела игрока (°C). Сохраняется вместе с игроком и синхронизируется с ним самим
	 * (клиенту нужна для визуальных эффектов).
	 * Без copyOnDeath: после смерти температура сбрасывается к 36.6.
	 */
	public static final AttachmentType<Double> BODY_TEMPERATURE = AttachmentRegistry.create(
			Arqon.id("body_temperature"),
			builder -> builder
					.initializer(() -> ThermalState.NORMAL_BODY_TEMPERATURE)
					.persistent(Codec.DOUBLE)
					.syncWith(ByteBufCodecs.DOUBLE, AttachmentSyncPredicate.targetOnly())
	);

	/**
	 * Ощущаемая температура воздуха вокруг игрока (°C), считается сервером раз в секунду.
	 * Не сохраняется, синхронизируется только с самим игроком (для визуальных эффектов).
	 */
	public static final AttachmentType<Double> AMBIENT_TEMPERATURE = AttachmentRegistry.create(
			Arqon.id("ambient_temperature"),
			builder -> builder
					.initializer(() -> DEFAULT_AMBIENT)
					.syncWith(ByteBufCodecs.DOUBLE, AttachmentSyncPredicate.targetOnly())
	);

	/** Вызывается из инициализатора мода, чтобы типы зарегистрировались заранее. */
	public static void init() {
	}
}
