package com.net.wenwen.potion;


import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


public class NoMoveEffect extends MobEffect {
    public NoMoveEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B0082);
    }


    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier)
    {
        entity.level().playSound(
                null,
                entity.blockPosition(),
                SoundEvents.ANVIL_PLACE,
                SoundSource.AMBIENT,
                1.0F,
                1.0F
        );
    }



}

