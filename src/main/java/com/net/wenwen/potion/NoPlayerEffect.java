package com.net.wenwen.potion;


import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NoPlayerEffect extends MobEffect {
    public NoPlayerEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000);
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

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(WenwenModMobEffects.NOPLAYER_EFFECT.get())) {
                event.setCanceled(true);
            }
        }
    }
}

