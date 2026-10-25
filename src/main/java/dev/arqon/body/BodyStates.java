package dev.arqon.body;

import net.minecraft.world.entity.player.Player;

/** Чтение и запись состояния частей тела игрока. На клиенте значения приходят по сети. */
public final class BodyStates {
	private BodyStates() {
	}

	/** Копия состояния. Если данных ещё нет или они от старой версии, возвращает здоровое тело. */
	public static BodyState get(Player player) {
		return BodyState.fromList(player.getAttached(ModAttachments.BODY_STATE));
	}

	public static void set(Player player, BodyState state) {
		player.setAttached(ModAttachments.BODY_STATE, state.toList());
	}
}
