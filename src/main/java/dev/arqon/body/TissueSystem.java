package dev.arqon.body;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;

import dev.arqon.climate.Clothing;
import dev.arqon.climate.ClothingProtection;
import dev.arqon.climate.ContactHeat;
import dev.arqon.effect.ModEffects;

/**
 * Температура и повреждения частей тела: обморожение и ожоги. Вызывается из {@link BodyTemperatureSystem} раз в секунду.
 * У каждой части своя температура: на неё влияют воздух (с одеждой, надетой именно на ней), тепло тела и предмет в руке.
 */
public final class TissueSystem {
	/** Пальцы чувствуют предмет в руке сильнее кисти. */
	private static final double FINGER_CONTACT_FACTOR = 1.5;

	private TissueSystem() {
	}

	/**
	 * @param air     температура воздуха вокруг игрока без одежды
	 * @param core    температура тела
	 * @param seconds сколько секунд прошло с прошлого вызова
	 */
	public static void update(ServerPlayer player, double air, double core, double seconds) {
		BodyState before = BodyStates.get(player);
		BodyState after = before.copy();
		double[] flux = contactFlux(player);

		for (BodyPart part : BodyPart.values()) {
			int i = part.ordinal();
			double felt = Clothing.effectiveAmbient(air, ClothingProtection.forPart(player, part));
			double t = PartThermals.step(part, before.temp[i], felt, core, flux[i], seconds);
			after.temp[i] = BodyState.quantize(t);
			after.frost[i] = Frostbite.next(before.frost[i], t, core, seconds);
			after.burn[i] = Burn.next(before.burn[i], t, seconds);
		}

		if (!after.sameAs(before)) {
			BodyStates.set(player, after);
			announce(player, before, after);
		}
		applyEffects(player, after);

		// Обмороженная голова: бег выматывает сильнее.
		int headLevel = Frostbite.Stage.of(after.frost[BodyPart.HEAD.ordinal()]).effectLevel();
		if (headLevel >= 0 && player.isSprinting()) {
			player.causeFoodExhaustion((float) (Frostbite.sprintExhaustionPerSecond(headLevel) * seconds));
		}
	}

	/** Во сколько раз быстрее остывает тело игрока из-за обмороженного туловища (1 без обморожения). */
	public static double coolingFactor(ServerPlayer player) {
		double torso = BodyStates.get(player).frost[BodyPart.TORSO.ordinal()];
		return Frostbite.coolingFactor(Frostbite.Stage.of(torso).effectLevel());
	}

	/** Нагрев и охлаждение кистей и пальцев от предметов в руках, градусов в секунду по частям. */
	private static double[] contactFlux(ServerPlayer player) {
		double[] flux = new double[BodyPart.COUNT];
		BodyPart.Side main = player.getMainArm() == HumanoidArm.RIGHT ? BodyPart.Side.RIGHT : BodyPart.Side.LEFT;
		BodyPart.Side off = main == BodyPart.Side.RIGHT ? BodyPart.Side.LEFT : BodyPart.Side.RIGHT;
		addHand(flux, main, ContactHeat.flux(player.getMainHandItem()));
		addHand(flux, off, ContactHeat.flux(player.getOffhandItem()));
		return flux;
	}

	private static void addHand(double[] flux, BodyPart.Side side, double value) {
		if (value == 0.0) {
			return;
		}
		flux[BodyPart.hand(side).ordinal()] += value;
		for (BodyPart finger : BodyPart.fingers(side)) {
			flux[finger.ordinal()] += value * FINGER_CONTACT_FACTOR;
		}
	}

	/** Сообщает игроку, когда в какой-то группе частей состояние стало хуже. Текст описывает ощущение, без чисел. */
	private static void announce(ServerPlayer player, BodyState before, BodyState after) {
		for (BodyPart.Group group : BodyPart.Group.values()) {
			announceKind(player, "frost." + group.id, worst(before.frost, group), worst(after.frost, group));
			announceKind(player, "burn", worst(before.burn, group), worst(after.burn, group));
		}
	}

	private static void announceKind(ServerPlayer player, String key, Frostbite.Stage was, Frostbite.Stage now) {
		if (now.ordinal() > was.ordinal() && now != Frostbite.Stage.HEALTHY) {
			// Омертвение сообщаем так же, как тяжёлое повреждение.
			Frostbite.Stage shown = now == Frostbite.Stage.NECROTIC ? Frostbite.Stage.SEVERE : now;
			player.displayClientMessage(Component.translatable("message.arqon." + key + "." + shown.id()), true);
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

	/** Выставляет эффекты по состоянию тела. Вызывается и из отладочных команд. */
	public static void applyEffects(ServerPlayer player, BodyState state) {
		applyEffect(player, ModEffects.FROSTBITTEN_LEGS, level(BodyState.legs(state.frost)));
		applyEffect(player, ModEffects.FROSTBITTEN_ARMS, level(BodyState.hands(state.frost)));
		applyEffect(player, ModEffects.FROSTBITTEN_HEAD, level(state.frost[BodyPart.HEAD.ordinal()]));
		applyEffect(player, ModEffects.FROSTBITTEN_TORSO, level(state.frost[BodyPart.TORSO.ordinal()]));
		applyEffect(player, ModEffects.BURNED_LEGS, level(BodyState.legs(state.burn)));
		applyEffect(player, ModEffects.BURNED_ARMS, level(BodyState.hands(state.burn)));
	}

	/** Снимает все эффекты (креатив, наблюдатель). */
	public static void clearEffects(ServerPlayer player) {
		for (Holder<MobEffect> effect : ModEffects.tissue()) {
			if (player.hasEffect(effect)) {
				player.removeEffect(effect);
			}
		}
	}

	private static int level(double value) {
		return Frostbite.Stage.of(value).effectLevel();
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
