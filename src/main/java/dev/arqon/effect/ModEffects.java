package dev.arqon.effect;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

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

	/** Обмороженные ноги: замедляют ход, каждый уровень -15 % к скорости. */
	public static final Holder<MobEffect> FROSTBITTEN_LEGS = registerEffect("frostbitten_legs", new ThermalEffect(0x6F8FD0)
			.addAttributeModifier(Attributes.MOVEMENT_SPEED, Arqon.id("effect.frostbitten_legs"),
					-0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	/** Обмороженные руки: пальцы не слушаются, каждый уровень -15 % к скорости удара и добычи. */
	public static final Holder<MobEffect> FROSTBITTEN_ARMS = registerEffect("frostbitten_arms", new ThermalEffect(0x7C6FD0)
			.addAttributeModifier(Attributes.ATTACK_SPEED, Arqon.id("effect.frostbitten_arms.attack"),
					-0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
			.addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, Arqon.id("effect.frostbitten_arms.mining"),
					-0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

	/** Обожжённые ноги: ходьба медленнее, как от обморожения. */
	public static final Holder<MobEffect> BURNED_LEGS = registerEffect("burned_legs", new ThermalEffect(0xD0804F)
			.addAttributeModifier(Attributes.MOVEMENT_SPEED, Arqon.id("effect.burned_legs"),
					-0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	/** Обожжённые руки: бить и копать медленнее. */
	public static final Holder<MobEffect> BURNED_ARMS = registerEffect("burned_arms", new ThermalEffect(0xD0603F)
			.addAttributeModifier(Attributes.ATTACK_SPEED, Arqon.id("effect.burned_arms.attack"),
					-0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
			.addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, Arqon.id("effect.burned_arms.mining"),
					-0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

	/** Обмороженное лицо: темнеет в глазах, бег выматывает (уровни: -, обморожение, тяжёлое, омертвение). */
	public static final Holder<MobEffect> FROSTBITTEN_HEAD = register("frostbitten_head", 0x8FB0E0);
	/** Обмороженное туловище: тело остывает быстрее. */
	public static final Holder<MobEffect> FROSTBITTEN_TORSO = register("frostbitten_torso", 0x5F7FB8);

	/** Занозы в пальцах: слабее удар и медленнее копание, каждый уровень (заноза) сильнее. */
	public static final Holder<MobEffect> SPLINTERS = registerEffect("splinters", new ThermalEffect(0x9A6B3C)
			.addAttributeModifier(Attributes.ATTACK_DAMAGE, Arqon.id("effect.splinters.weakness"),
					-0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
			.addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, Arqon.id("effect.splinters.mining"),
					-0.12, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

	private static Holder<MobEffect> register(String name, int color) {
		return registerEffect(name, new ThermalEffect(color));
	}

	private static Holder<MobEffect> registerEffect(String name, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Arqon.id(name), effect);
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

	/** Эффекты повреждений частей тела (обморожение и ожоги). */
	public static List<Holder<MobEffect>> tissue() {
		return List.of(FROSTBITTEN_LEGS, FROSTBITTEN_ARMS, FROSTBITTEN_HEAD, FROSTBITTEN_TORSO, BURNED_LEGS, BURNED_ARMS, SPLINTERS);
	}

	/** Вызывается из инициализатора мода, чтобы эффекты зарегистрировались заранее. */
	public static void init() {
	}
}
