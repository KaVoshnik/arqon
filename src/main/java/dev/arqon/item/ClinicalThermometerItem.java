package dev.arqon.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import dev.arqon.Arqon;
import dev.arqon.body.BodyTemperature;
import dev.arqon.climate.ThermalState;

/** Градусник: по правому клику тратит половинку голода (1 единицу) и показывает температуру тела. */
public class ClinicalThermometerItem extends Item {
	/** Стоимость измерения в единицах голода (1 = половинка «окорочка»). */
	private static final int HUNGER_COST = 1;

	public ClinicalThermometerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.isCreative()) {
				FoodData food = serverPlayer.getFoodData();
				food.setFoodLevel(Math.max(0, food.getFoodLevel() - HUNGER_COST));
			}

			// Показываем то же округлённое значение, по которому считаются пороги состояний.
			double body = ThermalState.roundToTenth(BodyTemperature.get(serverPlayer));
			serverPlayer.displayClientMessage(
					Component.translatable("message.arqon.body_temperature", Arqon.formatTemperature(body)),
					true
			);
			// Если в другой руке термометр, не затираем результат измерения на 3 секунды.
			ThermometerDisplay.suppress(serverPlayer, 60);
		}
		return InteractionResult.SUCCESS;
	}
}
