package com.net.wenwen.mixin;

import com.net.wenwen.init.ModEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemBreakMixin {

    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private void onHurtAndBreak(int amount, LivingEntity entity, Consumer<LivingEntity> onBroken, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        boolean hasUnbreakable = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Unbreak.get(), itemStack) > 0;
        if (hasUnbreakable) {
            ci.cancel();
        }
    }
}


