package dev.arqon.body;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import dev.arqon.effect.ModEffects;

/** Занозы: появляются при рубке дерева без топора, снимаются мини-игрой в экране тела. */
public final class SplinterSystem {
	private SplinterSystem() {
	}

	public static void init() {
		PlayerBlockBreakEvents.AFTER.register(SplinterSystem::onBreak);
		// Раз в секунду приводим эффект в соответствие с занозами (на случай креатива, смерти, молока).
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 == 0) {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					refresh(player);
				}
			}
		});
	}

	public static int mask(Player player) {
		Integer mask = player.getAttached(ModAttachments.SPLINTERS);
		return mask == null ? 0 : mask;
	}

	public static void setMask(ServerPlayer player, int mask) {
		player.setAttached(ModAttachments.SPLINTERS, mask);
		refresh(player);
	}

	private static void onBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (!(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel level)) {
			return;
		}
		if (sp.isCreative() || sp.isSpectator() || !state.is(BlockTags.LOGS)) {
			return;
		}
		// Топор спасает от заноз.
		if (sp.getMainHandItem().is(ItemTags.AXES)) {
			return;
		}
		double chance = Splinters.CHANCE * (Gloves.has(sp) ? Splinters.GLOVE_FACTOR : 1.0);
		RandomSource random = sp.getRandom();
		if (random.nextDouble() >= chance) {
			return;
		}
		int count = Splinters.rollCount(random.nextDouble());
		boolean left = sp.getMainArm() == HumanoidArm.LEFT;
		int before = mask(sp);
		int after = Splinters.add(before, left, count, random::nextInt);
		if (after == before) {
			return;
		}
		// Полсердца урона.
		sp.hurtServer(level, level.damageSources().generic(), 1.0f);
		setMask(sp, after);
		sp.displayClientMessage(Component.translatable("message.arqon.splinter"), true);
	}

	/** Игрок вытащил занозу из пальца index (проверяем, что она там правда есть). */
	public static void pull(ServerPlayer player, int index) {
		if (index < 0 || index >= Splinters.FINGER_COUNT) {
			return;
		}
		int mask = mask(player);
		if (!Splinters.has(mask, index)) {
			return;
		}
		setMask(player, Splinters.remove(mask, index));
		player.displayClientMessage(Component.translatable("message.arqon.splinter.pulled"), true);
	}

	public static void refresh(ServerPlayer player) {
		int level = (player.isCreative() || player.isSpectator()) ? -1 : Splinters.effectLevel(mask(player));
		MobEffectInstance current = player.getEffect(ModEffects.SPLINTERS);
		if (level < 0) {
			if (current != null) {
				player.removeEffect(ModEffects.SPLINTERS);
			}
			return;
		}
		if (current != null && current.getAmplifier() == level && current.isInfiniteDuration()) {
			return;
		}
		if (current != null) {
			player.removeEffect(ModEffects.SPLINTERS);
		}
		player.addEffect(new MobEffectInstance(ModEffects.SPLINTERS, MobEffectInstance.INFINITE_DURATION, level, false, false, true));
	}
}
