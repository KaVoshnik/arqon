package dev.arqon.climate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Ближайшие блоки, которые греют или остужают воздух вокруг игрока.
 * Вклад блока убывает с расстоянием по квадрату, итог ограничен в {@link ClimateModel}.
 */
public final class HeatSources {
	/** Радиус поиска в блоках. */
	private static final int RADIUS = 4;

	private HeatSources() {
	}

	/** Суммарный вклад источников в °C: положительный греет, отрицательный остужает. */
	public static double netAround(ServerLevel level, BlockPos center) {
		double heat = 0.0;
		double cold = 0.0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dy = -RADIUS; dy <= RADIUS; dy++) {
				for (int dz = -RADIUS; dz <= RADIUS; dz++) {
					double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (distance > RADIUS) {
						continue;
					}
					pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) {
						continue;
					}
					double strength = strength(state);
					if (strength == 0.0) {
						continue;
					}
					double falloff = 1.0 - distance / (RADIUS + 1.0);
					double contribution = strength * falloff * falloff;
					if (contribution > 0) {
						heat += contribution;
					} else {
						cold -= contribution;
					}
				}
			}
		}
		return ClimateModel.combineSources(heat, cold);
	}

	/** Вклад блока вплотную (°C): положительный греет, отрицательный остужает, 0 никак не влияет. */
	static double strength(BlockState state) {
		Block block = state.getBlock();

		if (block == Blocks.LAVA) return 30.0;
		if (block == Blocks.FIRE) return 12.0;
		if (block == Blocks.SOUL_FIRE) return 8.0;
		if (block == Blocks.MAGMA_BLOCK) return 6.0;

		if (isLit(state)) {
			if (block == Blocks.CAMPFIRE) return 14.0;
			if (block == Blocks.SOUL_CAMPFIRE) return 8.0;
			if (block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER) return 9.0;
		}

		if (block == Blocks.TORCH || block == Blocks.WALL_TORCH) return 1.5;

		if (block == Blocks.ICE) return -3.0;
		if (block == Blocks.PACKED_ICE) return -4.0;
		if (block == Blocks.BLUE_ICE) return -6.0;
		if (block == Blocks.SNOW_BLOCK) return -2.0;
		if (block == Blocks.POWDER_SNOW) return -3.0;

		return 0.0;
	}

	private static boolean isLit(BlockState state) {
		return state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
	}
}
