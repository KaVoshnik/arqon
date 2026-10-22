package dev.arqon.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import dev.arqon.Arqon;
import dev.arqon.climate.AmbientTemperature;

/**
 * Пока термометр в любой руке, раз в полсекунды показывает температуру воздуха над хотбаром.
 * Работает и в креативе.
 */
public final class ThermometerDisplay {
	private static final int INTERVAL_TICKS = 10;

	/** Игрок -> тик сервера, до которого не перебиваем action bar (например, после измерения градусником). */
	private static final Map<UUID, Integer> SUPPRESSED_UNTIL = new ConcurrentHashMap<>();

	private ThermometerDisplay() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(ThermometerDisplay::onServerTick);
	}

	/** Не перебивать action bar этого игрока ближайшие {@code ticks} тиков. */
	public static void suppress(ServerPlayer player, int ticks) {
		MinecraftServer server = player.level().getServer();
		if (server != null) {
			SUPPRESSED_UNTIL.put(player.getUUID(), server.getTickCount() + ticks);
		}
	}

	private static void onServerTick(MinecraftServer server) {
		int now = server.getTickCount();
		if (now % INTERVAL_TICKS != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!isHoldingThermometer(player)) {
				continue;
			}
			Integer until = SUPPRESSED_UNTIL.get(player.getUUID());
			if (until != null) {
				if (now < until) {
					continue;
				}
				SUPPRESSED_UNTIL.remove(player.getUUID());
			}
			double ambient = AmbientTemperature.at((ServerLevel) player.level(), player.blockPosition());
			player.displayClientMessage(
					Component.translatable("message.arqon.ambient_temperature", Arqon.formatTemperature(ambient)),
					true
			);
		}
	}

	private static boolean isHoldingThermometer(ServerPlayer player) {
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		return main.is(ModItems.THERMOMETER) || off.is(ModItems.THERMOMETER);
	}
}
