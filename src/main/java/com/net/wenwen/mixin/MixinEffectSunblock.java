package com.net.wenwen.mixin;

import com.bobmowzie.mowziesmobs.server.potion.EffectSunblock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectSunblock.class)
public class MixinEffectSunblock {

    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    public void applyEffectTick(LivingEntity entity, int amplifier, CallbackInfo ci) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            float healAmount = entity.getMaxHealth() * 0.0025F;
            entity.heal(healAmount);
            ci.cancel();
        }
    }
}

