package com.net.wenwen.mixin;
import com.net.wenwen.MusicPlayerManager;
import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;

import com.net.wenwen.potion.BeHeadEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;

import net.minecraft.world.item.enchantment.EnchantmentHelper;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {


    @Inject(
            method = "addEffect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void IsAddEffect(MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(WenwenModMobEffects.BATI_EFFECT.get())) {
            if((effectInstance.getEffect().getCategory() == MobEffectCategory.HARMFUL))
            {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCheckTotemDeathProtection(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(WenwenModMobEffects.Fear.get())) {
            cir.setReturnValue(false); // 强制返回 false，阻止复活
            cir.cancel(); // 取消后续原版逻辑
        }
    }

    @Inject(
            method = "setHealth",
            at = @At("HEAD"),
            cancellable = true
    )
    private void Sethealth(float amount, CallbackInfo info) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(WenwenModMobEffects.BUSI.get()) && amount<=0) {
            info.cancel(); // 取消后续原版逻辑
        }
        if (entity.hasEffect(WenwenModMobEffects.BeHead.get())) {
            MobEffectInstance beheadInstance = entity.getEffect(WenwenModMobEffects.BeHead.get());
            if (beheadInstance != null) {
                BeHeadEffect beheadEffect = (BeHeadEffect) beheadInstance.getEffect();
                if(beheadEffect.onHurt(entity)){
                    info.cancel();
                }
            }
        }
    }

    @Inject(
            method = "hurt",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // 获取当前生物实例
        LivingEntity entity = (LivingEntity) (Object) this;
        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof Projectile){
            if (entity.hasEffect(WenwenModMobEffects.RANGE.get())) {
                // 取消伤害
                cir.setReturnValue(false);
                cir.cancel();
                entity.level().playSound(
                        null, // Player - null 表示广播给所有玩家
                        entity.getX(), entity.getY(), entity.getZ(), // 音效位置
                        net.minecraft.sounds.SoundEvents.SHIELD_BLOCK, // 盾牌格挡音效
                        entity.getSoundSource(), // 音效分类
                        1.0F, // 音量
                        1.0F  // 音调
                );
            }
        }
    }


    @Inject(
            method = "travel", // 目标方法名
            at = @At("HEAD"), // 在方法的最开头注入
            cancellable = true // 允许我们取消原方法的执行
    )
    private void onTravel(Vec3 pTravelVector, CallbackInfo info) {

        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.hasEffect(WenwenModMobEffects.NOMOVE_EFFECT.get()) || entity.hasEffect(WenwenModMobEffects.NOPLAYER_EFFECT.get())) {
            info.cancel();
        }
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealAmount(float amount) {
        LivingEntity entity = (LivingEntity)(Object)this;
        var dehp = WenwenModMobEffects.DEHP_EFFECT.get();
        if (entity.hasEffect(dehp)) {
            MobEffectInstance effect = entity.getEffect(dehp);
            if (effect != null) {
                int effectLevel = effect.getAmplifier();
                float hp_amount=Math.min(0.1f*effectLevel,0.5f);
                return amount * (0.5f-hp_amount); // 将回血量减半
            }
        }
        if (entity.hasEffect(WenwenModMobEffects.HEAL_EFFECT.get())) {
            return amount * 2f;
        }
        return amount;
    }
    @Inject(method = "getArmorValue", at = @At("RETURN"), cancellable = true)
    private void onGetArmorValue(CallbackInfoReturnable<Integer> cir) {
        // 安全检查当前对象是否为LivingEntity
        if (!((Object) this instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) (Object) this;
        int originalArmor = cir.getReturnValue();
        int newArmor = originalArmor;

        try {
            var hujia = WenwenModMobEffects.HUJIA_EFFECT.get();
            var suilie = WenwenModMobEffects.SUILIE_EFFECT.get();

            // 处理护甲效果
            if (entity.hasEffect(hujia)) {
                MobEffectInstance effect = entity.getEffect(hujia);
                if (effect != null) {
                    int effectLevel = effect.getAmplifier() + 1;
                    newArmor += effectLevel * 2;
                }
            }

            // 处理碎裂效果
            if (entity.hasEffect(suilie)) {
                MobEffectInstance effect = entity.getEffect(suilie);
                if (effect != null) {
                    int effectLevel = effect.getAmplifier() + 1;
                    newArmor = Math.max(0, newArmor - (effectLevel * 2));
                }
            }
            //处理重甲附魔
            // 初始护甲值
            int bonusArmor = 0;

            // 检查四个装备槽位的护甲
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack stack = entity.getItemBySlot(slot);
                    int level=EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Armor.get(), stack);
                    // 检查物品是否有耐久附魔
                    if (level > 0) {
                        bonusArmor = Math.round(level*(newArmor*0.2f));
                    }
                }
            }

            // 设置最终的护甲值
            cir.setReturnValue(newArmor + bonusArmor);
        } catch (Exception e) {
            cir.setReturnValue(originalArmor);
        }
    }



    /*@Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void onGetDamageAfterArmorAbsorb(DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        if (source.getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) return;
            int level = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Penetrate.get(), stack);

            if (level > 0) {
                // 获取目标实体
                Entity target = source.getEntity();
                if (target instanceof LivingEntity livingTarget) {
                    // 获取目标护甲值
                    float armorValue = livingTarget.getArmorValue();
                    boolean isRanged = source.is(DamageTypeTags.IS_PROJECTILE);
                    // 计算忽略的护甲值（每级忽略10%，最多50%）
                    float ignorePercent = Math.min(level * 0.1f, 0.5f);
                    if(isRanged){
                        ignorePercent*=2;
                    }
                    float effectiveArmor = armorValue * (1 - ignorePercent);

                    // 使用修改后的护甲值重新计算伤害
                    float damageAfterArmor = damage * (1 - Math.min(20, effectiveArmor) / 25);
                    cir.setReturnValue(damageAfterArmor);
                }
            }
        }
    }*/



}


