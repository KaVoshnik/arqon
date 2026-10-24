package dev.arqon.climate;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import dev.arqon.item.ModItems;

/** Что именно надето на игроке и какую защиту от холода и жары это даёт. */
public final class ClothingProtection {
	/** Предмет -> комплект, которому он принадлежит. Вклад зависит от слота, в который предмет надет. */
	private static final Map<Item, Clothing.SetProfile> SETS = new HashMap<>();

	static {
		register(Clothing.WOOL, ModItems.WOOL_CAP, ModItems.WOOL_COAT, ModItems.WOOL_TROUSERS, ModItems.WOOL_BOOTS);
		register(Clothing.LINEN, ModItems.LINEN_HAT, ModItems.LINEN_SHIRT, ModItems.LINEN_TROUSERS, ModItems.LINEN_SANDALS);

		register(Clothing.LEATHER, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
		register(Clothing.METAL, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
		register(Clothing.METAL, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
		register(Clothing.METAL, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
		register(Clothing.METAL, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
		register(Clothing.METAL, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
	}

	private ClothingProtection() {
	}

	private static void register(Clothing.SetProfile profile, Item... pieces) {
		for (Item piece : pieces) {
			SETS.put(piece, profile);
		}
	}

	public static Clothing.Protection of(Player player) {
		Clothing.Protection total = Clothing.Protection.NONE;
		total = total.plus(piece(player, EquipmentSlot.HEAD, Clothing.Slot.HEAD));
		total = total.plus(piece(player, EquipmentSlot.CHEST, Clothing.Slot.CHEST));
		total = total.plus(piece(player, EquipmentSlot.LEGS, Clothing.Slot.LEGS));
		total = total.plus(piece(player, EquipmentSlot.FEET, Clothing.Slot.FEET));
		return total.clamped();
	}

	private static Clothing.Protection piece(Player player, EquipmentSlot equipmentSlot, Clothing.Slot slot) {
		Clothing.SetProfile profile = SETS.get(player.getItemBySlot(equipmentSlot).getItem());
		return profile == null ? Clothing.Protection.NONE : profile.piece(slot);
	}
}
