package com.net.wenwen.mixin;
import com.net.wenwen.init.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Shadow
    private int age;


    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity itemEntity = (ItemEntity)(Object)this;
        ItemStack stack = itemEntity.getItem();

        if (stack != null) {
            Enchantment enchantment = ModEnchantments.Unbreak.get();
            if (enchantment != null &&
                    EnchantmentHelper.getTagEnchantmentLevel(enchantment, stack) > 0) {
                // 免疫所有伤害，包括虚空伤害
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity)(Object)this;
        ItemStack stack = itemEntity.getItem();

        if (stack != null) {
            Enchantment enchantment = ModEnchantments.Unbreak.get();
            if (enchantment != null &&
                    EnchantmentHelper.getTagEnchantmentLevel(enchantment, stack) > 0) {
                // 保持无敌状态
                itemEntity.setInvulnerable(true);
                if (this.age > 12000) {
                    itemEntity.discard();
                }
            }
        }
    }
}
