package com.net.wenwen.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;


public class DehpEffect extends MobEffect {
    public DehpEffect() {
        super(MobEffectCategory.HARMFUL, 0x800080);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

}
