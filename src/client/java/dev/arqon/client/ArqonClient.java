package dev.arqon.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import dev.arqon.Arqon;

public class ArqonClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(ThermalFeedback::tick);

		// addFirst: рисуем под остальным HUD, чтобы хотбар и сердечки оставались читаемыми.
		HudElementRegistry.addFirst(Arqon.id("thermal_overlay"), ThermalOverlay::render);

		Arqon.LOGGER.info("Arqon (клиент) загружен");
	}
}
