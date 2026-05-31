package com.net.wenwen.potion;


import com.net.wenwen.damage.ModDamageSources;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class BeHeadEffect extends MobEffect {
    public BeHeadEffect() {
        super(MobEffectCategory.HARMFUL,0xFFFFFF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    public boolean onHurt(LivingEntity self)
    {
        if(self.getHealth()<self.getMaxHealth()*0.3)
        {
            self.level().playSound(
                    null,
                    self.blockPosition(),
                    WenwenModSounds.ZHANSHA.get(),
                    SoundSource.AMBIENT,
                    1.0F,
                    1.0F
            );
            self.removeEffect(this);
            ModDamageSources damageSources = new ModDamageSources(self.level());
            DamageSource trueDamage = damageSources.beheadDamage();
            self.hurt(trueDamage,999999999);
            return true;
        }
        return false;
    }
}

