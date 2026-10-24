package dev.arqon.item;

import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import dev.arqon.Arqon;

public final class ModItems {
	private ModItems() {
	}

	// ---------- Приборы ----------

	/** Термометр: температура воздуха. */
	public static final Item THERMOMETER = register("thermometer", ThermometerItem::new, new Item.Properties().stacksTo(1));
	/** Градусник: температура тела. */
	public static final Item CLINICAL_THERMOMETER = register("clinical_thermometer", ClinicalThermometerItem::new, new Item.Properties().stacksTo(1));

	// ---------- Одежда: шерстяной комплект (тепло) ----------

	public static final Item WOOL_CAP = register("wool_cap", Item::new, clothing(ClothingMaterials.WOOL, ArmorType.HELMET, "warm"));
	public static final Item WOOL_COAT = register("wool_coat", Item::new, clothing(ClothingMaterials.WOOL, ArmorType.CHESTPLATE, "warm"));
	public static final Item WOOL_TROUSERS = register("wool_trousers", Item::new, clothing(ClothingMaterials.WOOL, ArmorType.LEGGINGS, "warm"));
	public static final Item WOOL_BOOTS = register("wool_boots", Item::new, clothing(ClothingMaterials.WOOL, ArmorType.BOOTS, "warm"));

	// ---------- Одежда: льняной комплект (прохлада) ----------

	public static final Item LINEN_HAT = register("linen_hat", Item::new, clothing(ClothingMaterials.LINEN, ArmorType.HELMET, "light"));
	public static final Item LINEN_SHIRT = register("linen_shirt", Item::new, clothing(ClothingMaterials.LINEN, ArmorType.CHESTPLATE, "light"));
	public static final Item LINEN_TROUSERS = register("linen_trousers", Item::new, clothing(ClothingMaterials.LINEN, ArmorType.LEGGINGS, "light"));
	public static final Item LINEN_SANDALS = register("linen_sandals", Item::new, clothing(ClothingMaterials.LINEN, ArmorType.BOOTS, "light"));

	/** Одежда: надевается в слоты брони, защиты и прочности нет, в подсказке написано, от чего она спасает. */
	private static Item.Properties clothing(ArmorMaterial material, ArmorType type, String loreKind) {
		return new Item.Properties()
				.humanoidArmor(material, type)
				.stacksTo(1)
				.component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
				.component(DataComponents.LORE, new ItemLore(List.of(
						Component.translatable("item.arqon.lore." + loreKind).withStyle(ChatFormatting.GRAY)
				)));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Arqon.id(name));
		Item item = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(THERMOMETER);
			entries.accept(CLINICAL_THERMOMETER);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
			entries.accept(WOOL_CAP);
			entries.accept(WOOL_COAT);
			entries.accept(WOOL_TROUSERS);
			entries.accept(WOOL_BOOTS);
			entries.accept(LINEN_HAT);
			entries.accept(LINEN_SHIRT);
			entries.accept(LINEN_TROUSERS);
			entries.accept(LINEN_SANDALS);
		});
	}
}
