package dev.arqon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

import dev.arqon.body.BodyTemperature;

/**
 * Клиентская обратная связь температуры: считает «силу ощущения» жары и холода (для оверлея),
 * трясёт прицел при гипотермии, пускает пар изо рта на морозе и капли пота в жару.
 * Всё берётся из синхронизированных с сервера значений, своей климатической логики на клиенте нет.
 */
public final class ThermalFeedback {
	/** Ощущение жары и холода, 0..1, плавно меняется. Читает оверлей. */
	private static float heat;
	private static float cold;

	/** Ощущение с учётом температуры воздуха: жарко от этого воздуха и холодно от этого воздуха. */
	private static final double HEAT_FEEL_START = 32.0;
	private static final double HEAT_FEEL_FULL = 46.0;
	private static final double COLD_FEEL_START = 8.0;
	private static final double COLD_FEEL_FULL = -8.0;

	/** Температура тела, с которой оверлей начинает реагировать на само состояние тела. */
	private static final double BODY_HEAT_START = 37.2;
	private static final double BODY_HEAT_FULL = 40.5;
	private static final double BODY_COLD_START = 36.0;
	private static final double BODY_COLD_FULL = 30.0;

	/** Скорость сглаживания: доля пути к цели за тик. */
	private static final float SMOOTHING = 0.04f;

	private ThermalFeedback() {
	}

	public static float heat() {
		return heat;
	}

	public static float cold() {
		return cold;
	}

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.level == null || !player.isAlive() || player.isCreative() || player.isSpectator()) {
			heat = approach(heat, 0f);
			cold = approach(cold, 0f);
			return;
		}

		double ambient = BodyTemperature.getAmbient(player);
		double body = BodyTemperature.get(player);

		// Сама жара вокруг даёт до 60 % ощущения, перегретое тело добивает до 100 %.
		float heatTarget = Math.max(
				0.6f * ramp(ambient, HEAT_FEEL_START, HEAT_FEEL_FULL),
				ramp(body, BODY_HEAT_START, BODY_HEAT_FULL));
		float coldTarget = Math.max(
				0.6f * ramp(ambient, COLD_FEEL_START, COLD_FEEL_FULL),
				ramp(body, BODY_COLD_START, BODY_COLD_FULL));

		heat = approach(heat, heatTarget);
		cold = approach(cold, coldTarget);

		if (client.isPaused()) {
			return;
		}
		shiver(player, body);
		particles(client, player, ambient, body);
	}

	/** Дрожь: лёгкая и умеренная гипотермия слегка трясут прицел. Креатив и наблюдатели сюда не доходят. */
	private static void shiver(LocalPlayer player, double body) {
		if (body > 35.0) {
			return;
		}
		// 35 °C: еле заметно, 28 °C: ощутимо.
		double strength = 0.15 + 0.5 * Math.min(1.0, (35.0 - body) / 7.0);
		RandomSource random = player.getRandom();
		if (random.nextInt(3) != 0) {
			return;
		}
		// Случайный толчок в обе стороны, в среднем прицел остаётся на месте.
		player.turn((random.nextDouble() - 0.5) * 2.0 * strength, (random.nextDouble() - 0.5) * 2.0 * strength);
	}

	private static void particles(Minecraft client, LocalPlayer player, double ambient, double body) {
		RandomSource random = player.getRandom();

		// Пар изо рта на морозе.
		if (ambient < 2.0 && player.tickCount % 40 == 0) {
			double look = Math.toRadians(player.getYRot());
			double x = player.getX() - Math.sin(look) * 0.35;
			double z = player.getZ() + Math.cos(look) * 0.35;
			double y = player.getEyeY() - 0.15;
			client.level.addParticle(ParticleTypes.CLOUD, x, y, z,
					-Math.sin(look) * 0.03, 0.01, Math.cos(look) * 0.03);
		}

		// Капли пота в сильную жару.
		if ((ambient > 38.0 || body > 38.0) && random.nextInt(25) == 0) {
			double x = player.getX() + (random.nextDouble() - 0.5) * 0.5;
			double y = player.getEyeY() + 0.1;
			double z = player.getZ() + (random.nextDouble() - 0.5) * 0.5;
			client.level.addParticle(ParticleTypes.FALLING_WATER, x, y, z, 0.0, 0.0, 0.0);
		}
	}

	/** 0 при значении {@code from} и 1 при значении {@code to}; работает и для убывающей шкалы (to < from). */
	private static float ramp(double value, double from, double to) {
		double t = (value - from) / (to - from);
		return (float) Math.max(0.0, Math.min(1.0, t));
	}

	private static float approach(float current, float target) {
		return current + (target - current) * SMOOTHING;
	}
}
