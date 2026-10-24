package dev.arqon.body;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import dev.arqon.climate.AmbientTemperature;
import dev.arqon.climate.ClimateModel;
import dev.arqon.climate.Clothing;
import dev.arqon.climate.ClothingProtection;
import dev.arqon.climate.ThermalState;
import dev.arqon.damage.ModDamageTypes;
import dev.arqon.effect.ModEffects;

/**
 * Раз в секунду пересчитывает температуру тела каждого игрока, вешает дебаффы и убивает при критических значениях.
 * Креативные и наблюдающие игроки в симуляции не участвуют.
 */
public final class BodyTemperatureSystem {
	/** Как часто пересчитываем (в тиках). Само изменение температуры от этого не зависит. */
	private static final int INTERVAL_TICKS = 20;

	private BodyTemperatureSystem() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(BodyTemperatureSystem::onServerTick);
	}

	private static void onServerTick(MinecraftServer server) {
		if (server.getTickCount() % INTERVAL_TICKS != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		if (!player.isAlive()) {
			return;
		}
		if (player.isCreative() || player.isSpectator()) {
			applyState(player, ThermalState.NORMAL);
			FrostbiteSystem.clearEffects(player);
			BodyTemperature.setAmbient(player, ModAttachments.DEFAULT_AMBIENT);
			return;
		}

		ServerLevel level = (ServerLevel) player.level();
		// Воздух вокруг с учётом одежды: именно эту температуру «чувствует» тело, и именно её видит клиент.
		double air = AmbientTemperature.at(level, player);
		double ambient = Clothing.effectiveAmbient(air, ClothingProtection.of(player));
		BodyTemperature.setAmbient(player, ambient);
		double body = ClimateModel.nextBodyTemperature(BodyTemperature.get(player), ambient, INTERVAL_TICKS);
		BodyTemperature.set(player, body);

		ThermalState state = ThermalState.of(body);
		if (state.isFatal()) {
			kill(player, level, state);
			return;
		}
		applyState(player, state);
		// Обморожение считаем по воздуху без одежды: одежда учитывается отдельно для каждой части тела.
		FrostbiteSystem.update(player, air, body, INTERVAL_TICKS / 20.0);
	}

	private static void kill(ServerPlayer player, ServerLevel level, ThermalState state) {
		ResourceKey<DamageType> type = state == ThermalState.FATAL_HEAT
				? ModDamageTypes.HYPERTHERMIA
				: ModDamageTypes.HYPOTHERMIA;
		player.hurtServer(level, ModDamageTypes.source(level, type), Float.MAX_VALUE);
	}

	/** Оставляет на игроке только эффект, соответствующий состоянию, остальные температурные снимает. */
	private static void applyState(ServerPlayer player, ThermalState state) {
		Holder<MobEffect> wanted = ModEffects.forState(state);
		for (Holder<MobEffect> effect : ModEffects.all()) {
			boolean has = player.hasEffect(effect);
			if (effect.equals(wanted)) {
				if (!has) {
					// Бесконечная длительность: эффект не мигает в HUD и снимается только нами.
					player.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
				}
			} else if (has) {
				player.removeEffect(effect);
			}
		}
	}
}
