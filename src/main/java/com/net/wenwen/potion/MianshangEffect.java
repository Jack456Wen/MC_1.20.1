package com.net.wenwen.potion;

import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.net.wenwen.init.WenwenModItems.MIRROR;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MianshangEffect extends MobEffect {
    public MianshangEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFF00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }


    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {

        LivingEntity entity = event.getEntity();
        var effect=WenwenModMobEffects.MIANSHANG_EFFECT.get();

        if (entity.hasEffect(effect)) {
            // 获取效果实例
            MobEffectInstance effectInstance = entity.getEffect(effect);
            if (effectInstance != null) {
                // 计算伤害减免 (等级 * 5%)
                int amplifier = effectInstance.getAmplifier()+1;
                float damageReduction = amplifier * 0.05f; // 5% per level
                // 应用伤害减免
                float newDamage = event.getAmount() * (1.0f - damageReduction);
                event.setAmount(newDamage);
            }
        }
    }

}
