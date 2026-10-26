package dev.arqon.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.arqon.Arqon;

/** Действие игрока из экрана тела (клиент → сервер). */
public record BodyActionPayload(int action, int arg) implements CustomPacketPayload {
	/** arg: номер пальца 0..9, заноза вытащена. */
	public static final int PULL_SPLINTER = 0;
	/** Снять перчатки, arg не используется. */
	public static final int TAKE_OFF_GLOVES = 1;

	public static final CustomPacketPayload.Type<BodyActionPayload> TYPE =
			new CustomPacketPayload.Type<>(Arqon.id("body_action"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BodyActionPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BodyActionPayload::action,
			ByteBufCodecs.VAR_INT, BodyActionPayload::arg,
			BodyActionPayload::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
