package dev.arqon.item;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import dev.arqon.Arqon;

public final class ModItems {
	private ModItems() {
	}

	/** Термометр: температура воздуха. */
	public static final Item THERMOMETER = register("thermometer", ThermometerItem::new);
	/** Градусник: температура тела. */
	public static final Item CLINICAL_THERMOMETER = register("clinical_thermometer", ClinicalThermometerItem::new);

	private static Item register(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Arqon.id(name));
		Item item = factory.apply(new Item.Properties().setId(key).stacksTo(1));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(THERMOMETER);
			entries.accept(CLINICAL_THERMOMETER);
		});
	}
}
