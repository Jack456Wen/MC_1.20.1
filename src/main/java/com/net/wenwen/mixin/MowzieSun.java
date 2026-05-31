package com.net.wenwen.mixin;

import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut;
import com.net.wenwen.damage.ModDamageSources;
import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityUmvuthi.class)
public abstract class MowzieSun extends LivingEntity {

    protected MowzieSun(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var sun = (EntityUmvuthi) (Object) this;
        if(sun.level().isClientSide)
            return;
        var target=source.getEntity();
        if(ModDamageSources.isBeheadDamageSimple(source)){
            super.hurt(source, amount);
            cir.setReturnValue(true);
        }
        if(target instanceof Player player){
            sun.addEffect(new MobEffectInstance(WenwenModMobEffects.FANSHANG_EFFECT.get(), 600, 9, false, true));
        }
    }
}
