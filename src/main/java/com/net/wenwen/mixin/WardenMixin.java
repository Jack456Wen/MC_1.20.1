package com.net.wenwen.mixin;

import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Warden.class)
public abstract class WardenMixin {

    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void onDoHurtTarget(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Warden warden = (Warden) (Object) this;
        Level level = warden.level();

        if (target instanceof LivingEntity livingTarget && !level.isClientSide) {
            livingTarget.addEffect(new MobEffectInstance(WenwenModMobEffects.Fear.get(), 600, 0, false, true));
        }
    }

    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        var warder = (Warden) (Object) this;
        if (warder.tickCount % 300 == 0) {
            if(warder.level() instanceof ServerLevel serverLevel){
                var list=serverLevel.players().stream()
                        .filter(player -> warder.distanceToSqr(player) < 400)
                        .collect(Collectors.toList());
                this.performShockwave(list);
            }
        }
    }

    private void performShockwave(List<ServerPlayer> targets) {
        var warder = (Warden) (Object) this;
        if (warder.level().isClientSide()) {
            return; // 只在服务端执行逻辑
        }

        // 播放一个震撼的音效
        warder.level().playSound(null, warder.blockPosition(), WenwenModSounds.SKILL.get(), warder.getSoundSource(), 5.0F, 1.0F);

        // 对每个目标造成伤害和击退
        for (ServerPlayer player : targets) {
            if(warder.getHealth()<0.5*warder.getMaxHealth()){
                player.addEffect(new MobEffectInstance(WenwenModMobEffects.DEHP_EFFECT.get(), 600, 0));
                player.addEffect(new MobEffectInstance(WenwenModMobEffects.NOMOVE_EFFECT.get(), 80, 0));
            }
            player.hurt(warder.damageSources().mobAttack(warder), player.getMaxHealth());
            // 产生强大的击退
            player.knockback(10.0F, warder.getX() - player.getX(), warder.getZ() - player.getZ());
        }

        if (warder.level() instanceof ServerLevel serverLevel) {
            // 1. 定义圆圈的参数
            double radius = 8.0; // 圆圈的半径
            double yOffset = 1.0; // 圆圈离地的高度，可以根据需要调整
            int particleCount = 36; // 组成圆圈的粒子数量，越多越平滑

            // 2. 循环计算并生成粒子
            for (int i = 0; i < particleCount; i++) {
                // 计算当前点的角度 (弧度制)
                // 2 * Math.PI 是一个完整的圆，除以粒子数量得到每个粒子的角度间隔
                double angle = (2 * Math.PI * i) / particleCount;

                // 3. 使用三角函数计算 X 和 Z 轴的偏移量
                double offsetX = radius * Math.cos(angle);
                double offsetZ = radius * Math.sin(angle);

                // 4. 计算粒子的最终生成坐标
                double particleX = warder.getX() + offsetX;
                double particleY = warder.getY() + yOffset;
                double particleZ = warder.getZ() + offsetZ;

                // 5. 在计算好的位置生成粒子
                serverLevel.sendParticles(
                        ParticleTypes.SONIC_BOOM, // 粒子类型
                        particleX, particleY, particleZ, // 生成坐标
                        1, // 一次生成的粒子数量
                        0.0, 0.0, 0.0, // 粒子的随机偏移量，我们这里不需要，所以设为0
                        0.0 // 粒子的速度，设为0
                );
            }
        }

    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var warder = (Warden) (Object) this;
        // 获取最大生命值
        float maxHealth = warder.getMaxHealth();
        // 如果当前生命值低于最大生命值的30%
        if (warder.getHealth() < maxHealth * 0.5) {
            // 检查是否已经有狂暴效果，避免重复添加
            if (!warder.hasEffect(MobEffects.DAMAGE_BOOST)) {
                // 进入狂暴模式：增加攻击力和速度
                warder.addEffect(new MobEffectInstance(WenwenModMobEffects.RANGE.get(), 999999, 0,false,false));
                warder.addEffect(new MobEffectInstance(WenwenModMobEffects.MIANSHANG_EFFECT.get(), 999999, 9,false,false));
                warder.addEffect(new MobEffectInstance(WenwenModMobEffects.HEAL_EFFECT.get(), 999999, 0,false,false));
                warder.addEffect(new MobEffectInstance(WenwenModMobEffects.FANSHANG_EFFECT.get(), 999999, 4,false,false));
                warder.addEffect(new MobEffectInstance(WenwenModMobEffects.BATI_EFFECT.get(), 999999, 0,false,false));
                warder.addEffect(new MobEffectInstance(WenwenModMobEffects.HUJIA_EFFECT.get(), 999999, 4,false,false));
                warder.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 4,false,false)); // 30秒，3级攻击力
                warder.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1,false,false)); // 30秒，2级速度
                warder.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30 * 20, 4,false,false));
                // 播放一个进入狂暴的音效
                warder.level().playSound(null, warder.blockPosition(), WenwenModSounds.LEVELUP_BELL.get(), warder.getSoundSource(), 1.0F, 1F);
            }
        }
    }
}
