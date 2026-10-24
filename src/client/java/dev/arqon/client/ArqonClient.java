package dev.arqon.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.client.KeyMapping;

import dev.arqon.Arqon;

public class ArqonClient implements ClientModInitializer {
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Arqon.id("main"));
	private static KeyMapping openBodyKey;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(ThermalFeedback::tick);
		ClientTickEvents.END_CLIENT_TICK.register(HeartbeatFeedback::tick);

		// addFirst: рисуем под остальным HUD, чтобы хотбар и сердечки оставались читаемыми.
		HudElementRegistry.addFirst(Arqon.id("thermal_overlay"), ThermalOverlay::render);

		openBodyKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.arqon.open_body",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_B,
				KEY_CATEGORY
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openBodyKey.consumeClick()) {
				if (client.player != null && client.screen == null) {
					client.setScreen(new BodyScreen());
				}
			}
		});

		Arqon.LOGGER.info("Arqon (клиент) загружен");
	}
}
