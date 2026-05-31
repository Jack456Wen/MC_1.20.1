package com.net.wenwen.potion;


import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BatiEffect extends MobEffect {
    /*MobEffect[] possibleEffects = {
            WenwenModMobEffects.NOPLAYER_EFFECT.get(),
            WenwenModMobEffects.NOMOVE_EFFECT.get(),
            WenwenModMobEffects.NOATTACK_EFFECT.get(),
            WenwenModMobEffects.SUILIE_EFFECT.get(),
            WenwenModMobEffects.DAMAGE_EFFECT.get(),
            WenwenModMobEffects.DEHP_EFFECT.get()
    };*/
    public BatiEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFF00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }



}

