package dev.arqon.body;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Перчатки: отдельный слот на теле. Надеваются правой кнопкой по предмету, снимаются в экране тела. */
public final class Gloves {
	private Gloves() {
	}

	public static void init() {
		// Если игрок погиб в перчатках, они выпадают.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
				ItemStack stack = get(player);
				if (!stack.isEmpty()) {
					player.spawnAtLocation(level, stack.copy());
					player.setAttached(ModAttachments.GLOVES, ItemStack.EMPTY);
				}
			}
		});
	}

	public static ItemStack get(Player player) {
		ItemStack stack = player.getAttached(ModAttachments.GLOVES);
		return stack == null ? ItemStack.EMPTY : stack;
	}

	public static boolean has(Player player) {
		return !get(player).isEmpty();
	}

	public static void set(Player player, ItemStack stack) {
		player.setAttached(ModAttachments.GLOVES, stack);
	}

	/** Снимает перчатки: в инвентарь, а если нет места, на землю. */
	public static void takeOff(ServerPlayer player) {
		ItemStack stack = get(player);
		if (stack.isEmpty()) {
			return;
		}
		set(player, ItemStack.EMPTY);
		if (!player.getInventory().add(stack.copy())) {
			player.drop(stack.copy(), false);
		}
	}
}
