package dev.arqon.item;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import dev.arqon.Arqon;

/**
 * Материалы одежды. Технически это «броня без защиты и прочности»: вся польза одежды в температуре.
 * Внешний вид на теле берётся из assets/arqon/equipment/*.json.
 */
public final class ClothingMaterials {
	private ClothingMaterials() {
	}

	public static final ResourceKey<EquipmentAsset> WOOL_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, Arqon.id("wool"));
	public static final ResourceKey<EquipmentAsset> LINEN_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, Arqon.id("linen"));

	private static final TagKey<Item> REPAIRS_WOOL = TagKey.create(Registries.ITEM, Arqon.id("repairs_wool_clothing"));
	private static final TagKey<Item> REPAIRS_LINEN = TagKey.create(Registries.ITEM, Arqon.id("repairs_linen_clothing"));

	public static final ArmorMaterial WOOL = material(SoundEvents.ARMOR_EQUIP_LEATHER, REPAIRS_WOOL, WOOL_ASSET);
	public static final ArmorMaterial LINEN = material(SoundEvents.ARMOR_EQUIP_LEATHER, REPAIRS_LINEN, LINEN_ASSET);

	private static ArmorMaterial material(
			Holder<SoundEvent> equipSound,
			TagKey<Item> repairIngredient,
			ResourceKey<EquipmentAsset> asset) {
		return new ArmorMaterial(
				0,
				Map.of(
						ArmorType.HELMET, 0,
						ArmorType.CHESTPLATE, 0,
						ArmorType.LEGGINGS, 0,
						ArmorType.BOOTS, 0
				),
				5,
				equipSound,
				0.0F,
				0.0F,
				repairIngredient,
				asset
		);
	}
}
