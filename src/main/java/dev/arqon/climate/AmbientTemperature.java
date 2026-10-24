package dev.arqon.climate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Температура воздуха в месте, где стоит игрок (без учёта одежды): связка {@link ClimateModel} с реальным миром.
 * Одежду учитывает {@link ClothingProtection}.
 */
public final class AmbientTemperature {
	private AmbientTemperature() {
	}

	public static double at(ServerLevel level, Player player) {
		BlockPos feet = player.blockPosition();
		BlockPos head = feet.above();

		Biome biome = level.getBiome(feet).value();
		boolean hasSkyLight = level.dimensionType().hasSkyLight();

		// Сколько блоков от головы до открытого неба: 0 под открытым небом, больше 0 под крышей, кроной, землёй.
		int coverDepth = 0;
		Shelter.Room room = Shelter.Room.OPEN;
		if (hasSkyLight) {
			int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, head.getX(), head.getZ());
			coverDepth = Math.max(0, surface - (head.getY() + 1));
			if (coverDepth > 0) {
				room = Shelter.around(level, head);
			}
		}

		return ClimateModel.perceivedTemperature(new ClimateModel.Conditions(
				biome.getBaseTemperature(),
				level.getDayTime(),
				hasSkyLight,
				level.dimensionType().ultraWarm(),
				biome.hasPrecipitation(),
				level.isRaining(),
				level.isThundering(),
				coverDepth,
				player.getY(),
				HeatSources.netAround(level, head),
				room.enclosed(),
				room.volume()
		));
	}
}
