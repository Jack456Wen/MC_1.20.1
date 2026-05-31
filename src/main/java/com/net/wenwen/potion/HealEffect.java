package com.net.wenwen.potion;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;


public class HealEffect extends MobEffect {
    public HealEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00FF00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }


}
