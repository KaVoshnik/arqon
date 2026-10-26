package dev.arqon.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;

import dev.arqon.body.Gloves;
import dev.arqon.body.SplinterSystem;

public final class ModNetworking {
	private ModNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.playC2S().register(BodyActionPayload.TYPE, BodyActionPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(BodyActionPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			switch (payload.action()) {
				case BodyActionPayload.PULL_SPLINTER -> SplinterSystem.pull(player, payload.arg());
				case BodyActionPayload.TAKE_OFF_GLOVES -> Gloves.takeOff(player);
				default -> {
				}
			}
		});
	}
}
