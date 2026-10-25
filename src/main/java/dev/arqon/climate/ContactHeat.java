package dev.arqon.climate;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Предметы в руке греют или студят кисть и пальцы, которые их держат.
 * Число это градусов в секунду для кисти (пальцы чувствуют в 1.5 раза сильнее):
 * ведро лавы согревает руку примерно на 3-4 °C за 10 секунд.
 */
public final class ContactHeat {
	private static final Map<Item, Double> FLUX = new HashMap<>();

	static {
		FLUX.put(Items.LAVA_BUCKET, 0.35);
		FLUX.put(Items.MAGMA_BLOCK, 0.25);
		FLUX.put(Items.BLAZE_ROD, 0.12);
		FLUX.put(Items.FIRE_CHARGE, 0.12);
		FLUX.put(Items.LANTERN, 0.05);
		FLUX.put(Items.TORCH, 0.04);
		FLUX.put(Items.SOUL_LANTERN, 0.04);
		FLUX.put(Items.SOUL_TORCH, 0.03);

		FLUX.put(Items.BLUE_ICE, -0.55);
		FLUX.put(Items.PACKED_ICE, -0.45);
		FLUX.put(Items.ICE, -0.40);
		FLUX.put(Items.POWDER_SNOW_BUCKET, -0.40);
		FLUX.put(Items.SNOW_BLOCK, -0.25);
		FLUX.put(Items.WATER_BUCKET, -0.15);
		FLUX.put(Items.SNOWBALL, -0.10);
	}

	private ContactHeat() {
	}

	/** Градусов в секунду для кисти, которая держит этот предмет (0, если предмет не греет и не студит). */
	public static double flux(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0.0;
		}
		return FLUX.getOrDefault(stack.getItem(), 0.0);
	}
}
