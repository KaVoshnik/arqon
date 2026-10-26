package dev.arqon;

import java.util.Locale;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.arqon.body.BodyTemperatureSystem;
import dev.arqon.body.Gloves;
import dev.arqon.body.SplinterSystem;
import dev.arqon.net.ModNetworking;
import dev.arqon.body.ModAttachments;
import dev.arqon.debug.DebugCommands;
import dev.arqon.effect.ModEffects;
import dev.arqon.item.ModItems;
import dev.arqon.item.ThermometerDisplay;

public class Arqon implements ModInitializer {
	public static final String MOD_ID = "arqon";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModAttachments.init();
		ModEffects.init();
		ModItems.init();
		ThermometerDisplay.init();
		BodyTemperatureSystem.init();
		ModNetworking.init();
		SplinterSystem.init();
		Gloves.init();

		// Отладочные команды есть только в dev-окружении (runClient / runServer из IDE),
		// в собранном моде для игроков их нет.
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			DebugCommands.init();
		}

		LOGGER.info("Arqon загружен");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	/** Температура с одним знаком после запятой, всегда через точку. */
	public static String formatTemperature(double celsius) {
		return String.format(Locale.ROOT, "%.1f", celsius);
	}
}
