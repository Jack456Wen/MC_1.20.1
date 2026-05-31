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
public class AttackUpEffect extends MobEffect {
    public AttackUpEffect() {
        super(MobEffectCategory.BENEFICIAL,0xFFFF00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        // 检查攻击源是否为实体
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            // 检查攻击者是否拥有攻击提升效果
            if (attacker.hasEffect(WenwenModMobEffects.ATTACKUP_EFFECT.get())) {
                // 获取原始伤害值
                float originalDamage = event.getAmount();
                // 计算50%的伤害提升
                float increasedDamage = originalDamage * 1.5f;
                // 设置新的伤害值
                event.setAmount(increasedDamage);
            }
        }
    }

}

