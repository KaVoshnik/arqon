package dev.arqon.climate;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Определяет, находится ли игрок в закрытом помещении, и каков его объём.
 *
 * Заливка идёт от головы игрока через проходимые блоки (воздух, растения, открытые двери и люки).
 * Помещение закрыто, если заливка кончилась сама, не дойдя до открытого неба, незагруженной области
 * или предельного объёма {@link ClimateModel#MAX_ROOM_VOLUME}.
 */
public final class Shelter {
	private static final Direction[] DIRECTIONS = Direction.values();

	private Shelter() {
	}

	/** Закрытое ли помещение и сколько в нём блоков воздуха. */
	public record Room(boolean enclosed, int volume) {
		public static final Room OPEN = new Room(false, 0);
	}

	public static Room around(ServerLevel level, BlockPos head) {
		if (!level.hasChunkAt(head) || !isPassable(level, head, level.getBlockState(head))) {
			return Room.OPEN;
		}

		Set<BlockPos> visited = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		visited.add(head);
		queue.add(head);

		while (!queue.isEmpty()) {
			BlockPos pos = queue.poll();

			// Над этой клеткой нет ничего, что закрывает небо: помещение сообщается с улицей.
			if (isOpenToSky(level, pos)) {
				return Room.OPEN;
			}

			for (Direction direction : DIRECTIONS) {
				BlockPos next = pos.relative(direction);
				if (visited.contains(next)) {
					continue;
				}
				if (!level.hasChunkAt(next)) {
					return Room.OPEN;
				}
				if (!isPassable(level, next, level.getBlockState(next))) {
					continue;
				}
				visited.add(next);
				if (visited.size() > ClimateModel.MAX_ROOM_VOLUME) {
					return Room.OPEN;
				}
				queue.add(next);
			}
		}
		return new Room(true, visited.size());
	}

	private static boolean isOpenToSky(ServerLevel level, BlockPos pos) {
		return level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
	}

	/** Пропускает ли блок воздух: пустая коллизия или открытая дверь, люк, калитка. */
	private static boolean isPassable(ServerLevel level, BlockPos pos, BlockState state) {
		if (state.isAir()) {
			return true;
		}
		if (state.hasProperty(BlockStateProperties.OPEN) && state.getValue(BlockStateProperties.OPEN)) {
			return true;
		}
		return state.getCollisionShape(level, pos).isEmpty();
	}
}
