package dev.arqon.body;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import dev.arqon.climate.Clothing;
import dev.arqon.climate.ClothingProtection;
import dev.arqon.effect.ModEffects;

/**
 * Обморожение частей тела. Вызывается из {@link BodyTemperatureSystem} раз в секунду и использует те же данные:
 * температуру воздуха и температуру тела. У каждой части свой воздух: на неё действует та одежда, что надета именно на ней.
 */
public final class FrostbiteSystem {
	private FrostbiteSystem() {
	}

	/**
	 * @param air     температура воздуха вокруг игрока без одежды
	 * @param body    температура тела
	 * @param seconds сколько секунд прошло с прошлого вызова
	 */
	public static void update(ServerPlayer player, double air, double body, double seconds) {
		double[] before = BodyFrost.get(player);
		double[] after = before.clone();

		for (BodyPart part : BodyPart.values()) {
			int i = part.ordinal();
			double felt = Clothing.effectiveAmbient(air, ClothingProtection.forPart(player, part));
			after[i] = Frostbite.next(before[i], part.susceptibility, felt, body, seconds);
		}

		if (!java.util.Arrays.equals(before, after)) {
			BodyFrost.set(player, after);
			announce(player, before, after);
		}
		applyEffects(player, after);
	}

	/** Сообщает игроку, когда в какой-то группе частей состояние стало хуже. Текст описывает ощущение, без чисел. */
	private static void announce(ServerPlayer player, double[] before, double[] after) {
		for (BodyPart.Group group : BodyPart.Group.values()) {
			Frostbite.Stage was = worst(before, group);
			Frostbite.Stage now = worst(after, group);
			if (now.ordinal() > was.ordinal() && now != Frostbite.Stage.HEALTHY) {
				// Омертвение сообщаем так же, как тяжёлое обморожение.
				Frostbite.Stage shown = now == Frostbite.Stage.NECROTIC ? Frostbite.Stage.SEVERE : now;
				player.displayClientMessage(Component.translatable("message.arqon.frost." + group.id + "." + shown.id()), true);
			}
		}
	}

	private static Frostbite.Stage worst(double[] values, BodyPart.Group group) {
		Frostbite.Stage worst = Frostbite.Stage.HEALTHY;
		for (BodyPart part : BodyPart.values()) {
			if (part.group == group) {
				Frostbite.Stage stage = Frostbite.Stage.of(values[part.ordinal()]);
				if (stage.ordinal() > worst.ordinal()) {
					worst = stage;
				}
			}
		}
		return worst;
	}

	/** Выставляет эффекты ног и рук по среднему состоянию пары. Вызывается и из отладочной команды. */
	public static void applyEffects(ServerPlayer player, double[] frost) {
		applyEffect(player, ModEffects.FROSTBITTEN_LEGS, level(frost, BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG));
		applyEffect(player, ModEffects.FROSTBITTEN_ARMS, level(frost, BodyPart.LEFT_ARM, BodyPart.RIGHT_ARM));
	}

	/** Снимает все эффекты обморожения (креатив, наблюдатель). */
	public static void clearEffects(ServerPlayer player) {
		for (Holder<MobEffect> effect : ModEffects.frostbite()) {
			if (player.hasEffect(effect)) {
				player.removeEffect(effect);
			}
		}
	}

	private static int level(double[] frost, BodyPart a, BodyPart b) {
		return Frostbite.Stage.of(Frostbite.average(frost[a.ordinal()], frost[b.ordinal()])).effectLevel();
	}

	private static void applyEffect(ServerPlayer player, Holder<MobEffect> effect, int level) {
		MobEffectInstance current = player.getEffect(effect);
		if (level < 0) {
			if (current != null) {
				player.removeEffect(effect);
			}
			return;
		}
		if (current != null && current.getAmplifier() == level) {
			return;
		}
		if (current != null) {
			player.removeEffect(effect);
		}
		// Бесконечная длительность: эффект снимаем и меняем только мы.
		player.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, level, false, false, true));
	}
}
