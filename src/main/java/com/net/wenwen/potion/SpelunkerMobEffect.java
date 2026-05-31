package com.net.wenwen.potion;

import com.net.wenwen.MusicPlayerManager;
import com.net.wenwen.WenwenMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SpelunkerMobEffect extends MobEffect {
	public SpelunkerMobEffect() {
		super(MobEffectCategory.BENEFICIAL, 0x7C4DFF);
	}
}
