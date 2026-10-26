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
import dev.arqon.body.BodyPart;
import dev.arqon.body.BodyState;
import dev.arqon.body.BodyStates;
import dev.arqon.body.BodyTemperature;
import dev.arqon.body.TissueSystem;

/**
 * Команды для отладки, регистрируются только в dev-окружении.
 * /bodytemp <значение>            температура тела (удобно проверять пороги без многочасового ожидания)
 * /frost <часть|all> <0..100>     обморожение части (все части: см. BodyPart, например right_index, left_foot)
 * /burn <часть|all> <0..100>      ожог части
 * /parttemp <часть|all> <°C>      температура части
 * /splinter <0..10>               занозы в пальцах (0 убирает все)
 */
public final class DebugCommands {
	private DebugCommands() {
	}

	private enum Kind {
		FROST("frost", 0.0, 100.0),
		BURN("burn", 0.0, 100.0),
		TEMP("parttemp", -40.0, 90.0);

		final String command;
		final double min;
		final double max;

		Kind(String command, double min, double max) {
			this.command = command;
			this.min = min;
			this.max = max;
		}
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerBodyTemp(dispatcher);
			for (Kind kind : Kind.values()) {
				registerPartCommand(dispatcher, kind);
			}
		});
	}

	private static void registerBodyTemp(CommandDispatcher<CommandSourceStack> dispatcher) {
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
						})));
	}

	private static void registerSplinter(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("splinter")
				.then(Commands.argument("count", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 10))
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							int count = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "count");
							int mask = dev.arqon.body.Splinters.add(0, player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT,
									Math.min(count, 5), player.getRandom()::nextInt);
							if (count > 5) {
								mask = dev.arqon.body.Splinters.add(mask, player.getMainArm() != net.minecraft.world.entity.HumanoidArm.LEFT,
										count - 5, player.getRandom()::nextInt);
							}
							dev.arqon.body.SplinterSystem.setMask(player, mask);
							context.getSource().sendSuccess(() -> Component.literal("splinter: " + count), false);
							return 1;
						})));
	}

	private static void registerPartCommand(CommandDispatcher<CommandSourceStack> dispatcher, Kind kind) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(kind.command);
		root.then(partBranch(kind, "all", null));
		for (BodyPart part : BodyPart.values()) {
			root.then(partBranch(kind, part.id, part));
		}
		dispatcher.register(root);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> partBranch(Kind kind, String name, BodyPart only) {
		return Commands.literal(name)
				.then(Commands.argument("value", DoubleArgumentType.doubleArg(kind.min, kind.max))
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							double value = DoubleArgumentType.getDouble(context, "value");
							BodyState state = BodyStates.get(player);
							for (BodyPart part : BodyPart.values()) {
								if (only != null && part != only) {
									continue;
								}
								switch (kind) {
									case FROST -> state.frost[part.ordinal()] = value;
									case BURN -> state.burn[part.ordinal()] = value;
									case TEMP -> state.temp[part.ordinal()] = value;
								}
							}
							BodyStates.set(player, state);
							TissueSystem.applyEffects(player, state);
							context.getSource().sendSuccess(
									() -> Component.literal(kind.command + " (" + name + "): " + Math.round(value)),
									false
							);
							return 1;
						}));
	}
}
