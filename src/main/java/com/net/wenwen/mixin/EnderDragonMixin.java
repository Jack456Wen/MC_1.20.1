
package com.net.wenwen.mixin;

import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;


import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;


@Mixin(EnderDragon.class)
public class EnderDragonMixin {

    @Shadow
    private EndCrystal nearestCrystal;

    private boolean hp_temp=false;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;

        // 获取世界时间
        long time = dragon.level().getDayTime();
        int currentDay = (int) (time / 24000);
        currentDay = Math.max(1, currentDay);

        // 设置属性
        AttributeInstance maxHealthAttribute = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            double newMaxHealth = Math.max(300, currentDay * 500);
            maxHealthAttribute.setBaseValue(newMaxHealth);
            // 初始化时直接设置为满血
            dragon.setHealth((float) newMaxHealth);
        }
    }

    @Inject(method = "checkCrystals", at = @At("HEAD"), cancellable = true)
    private void onCheckCrystals(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;

        if (dragon.tickCount % 200 == 0) {
            if(dragon.level() instanceof ServerLevel serverLevel){
                var list=serverLevel.players().stream()
                        .filter(player -> dragon.distanceToSqr(player) < 2500)
                        .collect(Collectors.toList());
                this.AddEffectPlayers(list,dragon);
            }
        }

        if (this.nearestCrystal != null) {
            if (this.nearestCrystal.isRemoved()) {
                this.nearestCrystal = null;
            } else if (dragon.tickCount % 10 == 0 && dragon.getHealth() < dragon.getMaxHealth()) {
                // 修改为恢复最大生命值的0.1%
                float healAmount = dragon.getMaxHealth() * 0.001F;
                dragon.setHealth(dragon.getHealth() + healAmount);
            }
        }

        if (dragon.getRandom().nextInt(10) == 0) {
            List<EndCrystal> list = dragon.level().getEntitiesOfClass(EndCrystal.class, dragon.getBoundingBox().inflate(32.0F));
            EndCrystal endcrystal = null;
            double d0 = Double.MAX_VALUE;

            for(EndCrystal endcrystal1 : list) {
                double d1 = endcrystal1.distanceToSqr(dragon);
                if (d1 < d0) {
                    d0 = d1;
                    endcrystal = endcrystal1;
                }
            }

            this.nearestCrystal = endcrystal;
        }

        ci.cancel();
    }
    private void AddEffectPlayers(List<ServerPlayer> targets, EnderDragon dragon)
    {
        for (ServerPlayer player : targets) {
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.DAMAGE_EFFECT.get(), 1200, 0, false, true));
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.SUILIE_EFFECT.get(), 1200, 4, false, true));
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.DEHP_EFFECT.get(), 1200, 4, false, true));
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.BeHead.get(), 1200, 0, false, true));
        }
    }
    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurt(List<Entity> entities, CallbackInfo ci) {
        EnderDragon enderDragon = (EnderDragon) (Object) this;
        Level level = enderDragon.level();
        float maxHealth = enderDragon.getMaxHealth();
        if (enderDragon.getHealth() < maxHealth * 0.5 && !hp_temp) {
            hp_temp = true;
            if (!level.isClientSide()) {
                var target=enderDragon.getTarget();
                if(target!=null){
                    //秒杀一次玩家
                    target.hurt(enderDragon.damageSources().mobAttack(enderDragon), 99999999);
                    target.setDeltaMovement(target.getDeltaMovement().x, 50.0, target.getDeltaMovement().z);
                }
                level.playSound(null, enderDragon.getX(), enderDragon.getY(), enderDragon.getZ(), WenwenModSounds.LEVELUP_BELL.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                if (level instanceof ServerLevel) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, enderDragon.getX(), enderDragon.getY(), enderDragon.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

}
