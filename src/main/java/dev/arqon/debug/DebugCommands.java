package dev.arqon.debug;

import com.mojang.brigadier.arguments.DoubleArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import dev.arqon.Arqon;
import dev.arqon.body.BodyTemperature;

/**
 * Команды для отладки, регистрируются только в dev-окружении.
 * /bodytemp <значение>  установить температуру тела (удобно проверять пороги без многочасового ожидания).
 */
public final class DebugCommands {
	private DebugCommands() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("bodytemp")
						.then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 60.0))
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									double value = DoubleArgumentType.getDouble(context, "value");
									BodyTemperature.set(player, value);
									context.getSource().sendSuccess(
											() -> Component.literal("Температура тела: " + Arqon.formatTemperature(value) + " °C"),
											false
									);
									return 1;
								}))));
	}
}
