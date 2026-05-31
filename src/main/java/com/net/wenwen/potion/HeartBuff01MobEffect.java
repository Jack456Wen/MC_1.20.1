
package com.net.wenwen.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class HeartBuff01MobEffect extends MobEffect {
	public HeartBuff01MobEffect() {
		super(MobEffectCategory.BENEFICIAL, -16724737);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return false;
	}

}
