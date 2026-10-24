package dev.arqon.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import dev.arqon.Arqon;
import dev.arqon.body.BodyFrost;
import dev.arqon.body.BodyPart;
import dev.arqon.body.BodyTemperature;
import dev.arqon.body.FrostbiteSystem;

/**
 * Команды для отладки, регистрируются только в dev-окружении.
 * /bodytemp <значение>  установить температуру тела (удобно проверять пороги без многочасового ожидания).
 */
public final class DebugCommands {
	private DebugCommands() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerFrost(dispatcher));
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

	/** /frost <часть|all> <0..100>: задаёт обморожение (части: head, torso, left_arm, right_arm, left_leg, right_leg). */
	private static void registerFrost(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("frost");
		root.then(frostBranch("all", null));
		for (BodyPart part : BodyPart.values()) {
			root.then(frostBranch(part.id, part));
		}
		dispatcher.register(root);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> frostBranch(String name, BodyPart only) {
		return Commands.literal(name)
				.then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 100.0))
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							double value = DoubleArgumentType.getDouble(context, "value");
							double[] frost = BodyFrost.get(player);
							for (BodyPart part : BodyPart.values()) {
								if (only == null || part == only) {
									frost[part.ordinal()] = value;
								}
							}
							BodyFrost.set(player, frost);
							FrostbiteSystem.applyEffects(player, frost);
							context.getSource().sendSuccess(
									() -> Component.literal("Обморожение (" + name + "): " + Math.round(value)),
									false
							);
							return 1;
						}));
	}
}
