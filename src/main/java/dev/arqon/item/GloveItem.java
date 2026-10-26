package dev.arqon.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import dev.arqon.body.Gloves;

/** Перчатки: правая кнопка надевает их на руки (если уже надеты другие, меняет местами). */
public class GloveItem extends Item {
	public GloveItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel) {
			ItemStack held = player.getItemInHand(hand);
			ItemStack worn = Gloves.get(player);
			Gloves.set(player, held.copyWithCount(1));
			player.setItemInHand(hand, worn.isEmpty() ? ItemStack.EMPTY : worn);
		}
		return InteractionResult.SUCCESS;
	}
}
