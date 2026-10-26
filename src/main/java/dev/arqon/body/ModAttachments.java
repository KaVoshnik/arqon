package dev.arqon.body;

import java.util.List;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

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

	/**
	 * Состояние частей тела: температура, обморожение и ожог каждой, плоским списком (см. {@link BodyState}).
	 * Сохраняется вместе с игроком и синхронизируется с ним самим (экран тела читает значения на клиенте).
	 * Без copyOnDeath: после смерти тело здоровое.
	 */
	public static final AttachmentType<List<Double>> BODY_STATE = AttachmentRegistry.create(
			Arqon.id("body_state"),
			builder -> builder
					.initializer(() -> BodyState.resting().toList())
					.persistent(Codec.DOUBLE.listOf())
					.syncWith(ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.targetOnly())
	);

	/**
	 * Заноза в пальцах: битовая маска по 10 пальцам (см. {@link Splinters}).
	 * Сохраняется, синхронизируется с игроком. Без copyOnDeath: после смерти занозы пропадают.
	 */
	public static final AttachmentType<Integer> SPLINTERS = AttachmentRegistry.create(
			Arqon.id("splinters"),
			builder -> builder
					.initializer(() -> 0)
					.persistent(Codec.INT)
					.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
	);

	/** Надетые перчатки (один предмет). При смерти выпадают на землю. */
	public static final AttachmentType<ItemStack> GLOVES = AttachmentRegistry.create(
			Arqon.id("gloves"),
			builder -> builder
					.initializer(() -> ItemStack.EMPTY)
					.persistent(ItemStack.OPTIONAL_CODEC)
					.syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	/** Вызывается из инициализатора мода, чтобы типы зарегистрировались заранее. */
	public static void init() {
	}
}
