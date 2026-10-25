package dev.arqon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import dev.arqon.body.BodyPart;
import dev.arqon.body.BodyStates;
import dev.arqon.body.Frostbite;

/**
 * Сердцебиение в ушах при тяжёлом обморожении туловища. Тише и реже на пороге, громче и чаще к омертвению.
 * Звук ванильный (сердцебиение стража), без своих файлов.
 */
public final class HeartbeatFeedback {
	private static final int SLOWEST_INTERVAL_TICKS = 40;
	private static final int FASTEST_INTERVAL_TICKS = 20;

	private static int cooldown;

	private HeartbeatFeedback() {
	}

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || !player.isAlive() || player.isCreative() || player.isSpectator()) {
			cooldown = 0;
			return;
		}
		double torso = BodyStates.get(player).frost[BodyPart.TORSO.ordinal()];
		if (torso < Frostbite.HEARTBEAT_FROM) {
			cooldown = 0;
			return;
		}
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		float s = (float) Math.min(1.0, (torso - Frostbite.HEARTBEAT_FROM) / (Frostbite.MAX - Frostbite.HEARTBEAT_FROM));
		client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_HEARTBEAT, 1.0f, 0.6f + 0.4f * s));
		cooldown = Math.round(SLOWEST_INTERVAL_TICKS - (SLOWEST_INTERVAL_TICKS - FASTEST_INTERVAL_TICKS) * s);
	}
}
