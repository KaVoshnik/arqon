package dev.arqon.item;

import net.minecraft.world.item.Item;

/**
 * Термометр. Сам предмет ничего не делает по клику: пока он лежит в любой руке,
 * температуру воздуха показывает {@link ThermometerDisplay}.
 */
public class ThermometerItem extends Item {
	public ThermometerItem(Properties properties) {
		super(properties);
	}
}
