package dev.arqon.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import dev.arqon.Arqon;

/**
 * Типы урона для смерти от температуры. Сами типы описаны датапаком:
 * data/arqon/damage_type/*.json, а обход брони/тотема в data/minecraft/tags/damage_type/*.json.
 */
public final class ModDamageTypes {
	private ModDamageTypes() {
	}

	public static final ResourceKey<DamageType> HYPERTHERMIA =
			ResourceKey.create(Registries.DAMAGE_TYPE, Arqon.id("hyperthermia"));
	public static final ResourceKey<DamageType> HYPOTHERMIA =
			ResourceKey.create(Registries.DAMAGE_TYPE, Arqon.id("hypothermia"));

	public static DamageSource source(ServerLevel level, ResourceKey<DamageType> key) {
		return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
	}
}
