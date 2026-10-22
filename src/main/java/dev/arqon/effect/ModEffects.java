package dev.arqon.effect;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import dev.arqon.Arqon;
import dev.arqon.climate.ThermalState;

public final class ModEffects {
	private ModEffects() {
	}

	/** Асфиксия: 38.0 – 39.9 °C */
	public static final Holder<MobEffect> ASPHYXIA = register("asphyxia", 0xE0A040);
	/** Гипертермия: 40.0 – 41.9 °C */
	public static final Holder<MobEffect> HYPERTHERMIA = register("hyperthermia", 0xD83A2E);
	/** Лёгкая гипотермия: 32.1 – 35.0 °C */
	public static final Holder<MobEffect> MILD_HYPOTHERMIA = register("mild_hypothermia", 0x8EC8E8);
	/** Умеренная гипотермия: 28.0 – 32.0 °C */
	public static final Holder<MobEffect> MODERATE_HYPOTHERMIA = register("moderate_hypothermia", 0x3F78C8);

	private static Holder<MobEffect> register(String name, int color) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Arqon.id(name), new ThermalEffect(color));
	}

	/** Эффект, который висит в данном состоянии, или null, если эффекта нет (норма, смерть). */
	public static Holder<MobEffect> forState(ThermalState state) {
		return switch (state) {
			case ASPHYXIA -> ASPHYXIA;
			case HYPERTHERMIA -> HYPERTHERMIA;
			case MILD_HYPOTHERMIA -> MILD_HYPOTHERMIA;
			case MODERATE_HYPOTHERMIA -> MODERATE_HYPOTHERMIA;
			default -> null;
		};
	}

	public static List<Holder<MobEffect>> all() {
		return List.of(ASPHYXIA, HYPERTHERMIA, MILD_HYPOTHERMIA, MODERATE_HYPOTHERMIA);
	}

	/** Вызывается из инициализатора мода, чтобы эффекты зарегистрировались заранее. */
	public static void init() {
	}
}
