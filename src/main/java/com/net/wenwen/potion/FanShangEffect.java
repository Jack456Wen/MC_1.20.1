package com.net.wenwen.potion;


import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FanShangEffect extends MobEffect {
    public FanShangEffect() {
        super(MobEffectCategory.BENEFICIAL,0xFFFF00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        Entity target=event.getSource().getEntity();
        var effect= WenwenModMobEffects.FANSHANG_EFFECT.get();
        if (entity.hasEffect(effect)) {
            // 获取效果实例
            MobEffectInstance effectInstance = entity.getEffect(effect);
            if (effectInstance != null) {
                int amplifier = effectInstance.getAmplifier()+1;
                float damageReduction = amplifier * 0.1f;
                float newDamage = event.getAmount() * damageReduction;
                if (target != null) {
                    target.hurt(entity.damageSources().magic(), newDamage);
                }
            }
        }
    }

}
