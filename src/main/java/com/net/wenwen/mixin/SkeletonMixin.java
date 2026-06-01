package com.net.wenwen.mixin;

import com.net.wenwen.arrrows.ExplossiveArrow;
import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSkeleton.class)
public abstract class SkeletonMixin {

    @Inject(
            method = "getArrow",
            at = @At("RETURN"),
            cancellable = true
    )
    private void injectExplosiveArrow(ItemStack itemStack, float distanceFactor, CallbackInfoReturnable<AbstractArrow> cir) {
        AbstractSkeleton skeleton=(AbstractSkeleton)(Object)this;
        Level level = skeleton.level();
        if (!level.isClientSide) {
            AbstractArrow originalArrow = cir.getReturnValue();
            if (skeleton.getRandom().nextDouble() < 0.025)
            {
                AbstractArrow explosiveArrow = new ExplossiveArrow(level, originalArrow.getX(), originalArrow.getY(), originalArrow.getZ());
                explosiveArrow.setBaseDamage(4);
                explosiveArrow.setKnockback(10);
                explosiveArrow.setOwner(skeleton);
                cir.setReturnValue(explosiveArrow);
            }

        }
    }

    @Inject(method = "performRangedAttack", at = @At("HEAD"))
    private void performRangedAttack(LivingEntity living, float p_32142_, CallbackInfo ci) {
        AbstractSkeleton skeleton=(AbstractSkeleton)(Object)this;
        if(!skeleton.level().isClientSide){
            var r=skeleton.getRandom().nextDouble();
            if(r<0.12f){
                living.setRemainingFireTicks(200);
            }
            if (r < 0.05) {
                // 随机选择三个效果之一
                int effectType = skeleton.getRandom().nextInt(3);
                switch (effectType) {
                    case 0 -> living.addEffect(new MobEffectInstance(
                            WenwenModMobEffects.DAMAGE_EFFECT.get(), 600, 0, false, true));
                    case 1 -> living.addEffect(new MobEffectInstance(
                            WenwenModMobEffects.SUILIE_EFFECT.get(), 600, 4, false, true));
                    case 2 -> living.addEffect(new MobEffectInstance(
                            WenwenModMobEffects.DEHP_EFFECT.get(), 600, 0, false, true));
                }
            }
        }
    }



}
