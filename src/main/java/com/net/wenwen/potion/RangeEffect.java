package com.net.wenwen.potion;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;


public class RangeEffect extends MobEffect {
    public RangeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFF00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }


}
