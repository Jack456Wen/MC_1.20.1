
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.warden.Warden;
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


@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {

    @Shadow
    private int[] nextHeadUpdate;


    private boolean hp_temp=false;
    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void onCustomServerAiStep(CallbackInfo ci) {
        WitherBoss wither = (WitherBoss) (Object) this;

        // 检查是否处于第二阶段（生命值低于最大值的一半）
        if (wither.getHealth() < wither.getMaxHealth() / 2.0F) {
            // 遍历三个头颅的冷却计时器
            for (int i = 0; i < this.nextHeadUpdate.length; i++) {
                // 如果冷却时间大于10（一个安全的阈值，避免无限加速），我们就减少它
                // 这里的 "2" 是加速因子，你可以修改它来调整速度。数值越大，攻击越快。
                if (this.nextHeadUpdate[i] > 10) {
                    this.nextHeadUpdate[i] -= 5;
                }
            }
        }

        if (wither.getInvulnerableTicks() > 0) {
            // 修改无敌期间的恢复量，使其与最大生命值成比例
            if (wither.tickCount % 10 == 0) {
                float healAmount = (float) (wither.getMaxHealth() * 0.01); // 恢复最大生命值的1%
                wither.heal(healAmount);
            }
        } else {
            // 修改正常状态下的恢复量
            if (wither.tickCount % 20 == 0) {
                float healAmount = (float) (wither.getMaxHealth() * 0.001); // 恢复最大生命值的0.1%
                wither.heal(healAmount);
            }
        }

        // 攻击玩家时施加效果
        if (wither.tickCount % 100 == 0) {
            if(wither.level() instanceof ServerLevel serverLevel){
                var list=serverLevel.players().stream()
                        .filter(player -> wither.distanceToSqr(player) < 400)
                        .collect(Collectors.toList());
                this.AddEffectPlayers(list,wither);
            }
        }
    }

    private void AddEffectPlayers(List<ServerPlayer> targets, WitherBoss wither)
    {
        for (ServerPlayer player : targets) {
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.DAMAGE_EFFECT.get(), 100, 0, false, true));
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.SUILIE_EFFECT.get(), 100, 4, false, true));
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.DEHP_EFFECT.get(), 100, 0, false, true));
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.BeHead.get(), 100, 0, false, true));
        }
    }
    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        WitherBoss witherBoss = (WitherBoss) (Object) this;
        Level level = witherBoss.level();
        // 获取最大生命值
        float maxHealth = witherBoss.getMaxHealth();
        if (witherBoss.getHealth() < maxHealth * 0.5 && !hp_temp) {
            hp_temp=true;
            if (!level.isClientSide()) {

                // 1. 生成3个凋零骷髅
                if (level.getDifficulty() != Difficulty.PEACEFUL) {
                    for (int i = 0; i < 3; i++) {
                        // 在凋零周围随机位置生成
                        WitherSkeleton witherSkeleton = EntityType.WITHER_SKELETON.create(level);
                        if (witherSkeleton != null) {
                            witherSkeleton.moveTo(
                                    witherBoss.getX() + (level.random.nextDouble() - 0.5D) * 4.0D,
                                    witherBoss.getY() + 0.5D,
                                    witherBoss.getZ() + (level.random.nextDouble() - 0.5D) * 4.0D,
                                    level.random.nextFloat() * 360.0F,
                                    0.0F
                            );
                            // 确保生成的骷髅是敌对的
                            witherSkeleton.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(witherSkeleton.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                            level.addFreshEntity(witherSkeleton);
                        }
                    }
                }

                // 2. 播放第二阶段开始的音效和特效
                level.playSound(null, witherBoss.getX(), witherBoss.getY(), witherBoss.getZ(), WenwenModSounds.LEVELUP_BELL.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                if (level instanceof ServerLevel) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, witherBoss.getX(), witherBoss.getY(), witherBoss.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }


}
