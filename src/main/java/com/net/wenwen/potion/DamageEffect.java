package com.net.wenwen.potion;


import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageEffect extends MobEffect {
    public DamageEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {

        LivingEntity entity = event.getEntity();
        var effect= WenwenModMobEffects.DAMAGE_EFFECT.get();

        if (entity.hasEffect(effect)) {
            // 获取效果实例
            MobEffectInstance effectInstance = entity.getEffect(effect);
            if (effectInstance != null) {
                float newDamage = event.getAmount() * 1.5f;
                event.setAmount(newDamage);
            }
        }
    }
}
