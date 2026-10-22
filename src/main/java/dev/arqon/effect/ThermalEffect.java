package dev.arqon.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект температурного состояния. Пока это просто «висящий» дебафф без игрового влияния:
 * логику (замедление, урон, и т.д.) добавим позже.
 */
public class ThermalEffect extends MobEffect {
	public ThermalEffect(int color) {
		super(MobEffectCategory.HARMFUL, color);
	}
}
