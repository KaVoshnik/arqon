package dev.arqon.climate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/** Температура воздуха в точке мира: связка {@link ClimateModel} с реальным миром. */
public final class AmbientTemperature {
	private AmbientTemperature() {
	}

	public static double at(ServerLevel level, BlockPos pos) {
		Biome biome = level.getBiome(pos).value();
		return ClimateModel.ambientTemperature(
				biome.getBaseTemperature(),
				level.getDayTime(),
				level.dimensionType().hasSkyLight(),
				biome.hasPrecipitation(),
				level.isRaining(),
				level.isThundering()
		);
	}
}
