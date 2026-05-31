package com.net.wenwen.mixin;

import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthana;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut;
import com.bobmowzie.mowziesmobs.server.potion.EffectSunblock;
import com.net.wenwen.damage.ModDamageSources;
import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityWroughtnaut.class)
public abstract class MowzieWroughtnautMixin extends LivingEntity {
    protected MowzieWroughtnautMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var wroughtnaut = (EntityWroughtnaut) (Object) this;
        if(wroughtnaut.level().isClientSide)
            return;
        if(ModDamageSources.isBeheadDamageSimple(source)){
            super.hurt(source, amount);
            cir.setReturnValue(true);
        }
        wroughtnaut.addEffect(new MobEffectInstance(WenwenModMobEffects.ATTACKUP_EFFECT.get(), 9999999, 0, false, true));
        wroughtnaut.addEffect(new MobEffectInstance(WenwenModMobEffects.MIANSHANG_EFFECT.get(), 9999999, 4, false, true));
        wroughtnaut.addEffect(new MobEffectInstance(WenwenModMobEffects.HUJIA_EFFECT.get(), 9999999, 9, false, true));
        var target=source.getEntity();
        if(target instanceof Player player){
            //给玩家增伤
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.SUILIE_EFFECT.get(), 1200, 99, false, true));
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.DAMAGE_EFFECT.get(), 1200, 0, false, true));
        }
    }
}
