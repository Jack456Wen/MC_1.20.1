package com.net.wenwen.potion;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

public class SuilieEffect extends MobEffect {
    public SuilieEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFF00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

}
