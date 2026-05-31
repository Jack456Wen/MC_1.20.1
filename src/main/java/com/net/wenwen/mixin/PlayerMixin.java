package com.net.wenwen.mixin;

import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Inventory;

import org.spongepowered.asm.mixin.Final;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;


@Mixin(Player.class)
public class PlayerMixin {
    @Inject(
            method = "getDigSpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void onGetDigSpeed(BlockState state, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        float originalSpeed = cir.getReturnValueF();
        ItemStack mainHandItem = ((Player) (Object) this).getMainHandItem();

        // 检查主手物品是否有Purge附魔
        int purgeLevel = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Purge.get(), mainHandItem);

        if (purgeLevel > 0) {
            boolean inWater = ((Player) (Object) this).isEyeInFluid(FluidTags.WATER) &&
                    !EnchantmentHelper.hasAquaAffinity((Player) (Object) this);
            boolean notOnGround = !((Player) (Object) this).onGround();

            // 计算基础速度（不受惩罚的速度）
            float baseSpeed = originalSpeed;
            if (inWater) baseSpeed *= 5.0F;  // 恢复到基础速度
            if (notOnGround) baseSpeed *= 5.0F;  // 恢复到基础速度

            // 计算惩罚减少比例
            float penaltyReduction = 0.2F * Math.min(purgeLevel, 5);  // 每级减少20%的惩罚，最多5级

            // 应用新的惩罚
            float newSpeed = baseSpeed;
            if (inWater) {
                // 水中惩罚是基础速度的1/5，我们按比例减少这个惩罚
                float waterPenalty = 5.0F - (penaltyReduction * 4.0F);  // 惩罚从5.0F减少到1.0F
                newSpeed /= waterPenalty;
            }
            if (notOnGround) {
                // 空中惩罚是基础速度的1/5，我们按比例减少这个惩罚
                float airPenalty = 5.0F - (penaltyReduction * 4.0F);  // 惩罚从5.0F减少到1.0F
                newSpeed /= airPenalty;
            }

            // 设置新的挖掘速度
            cir.setReturnValue(newSpeed);
        }
    }


    @Inject(
            method = "remove",
            at = @At("HEAD"),
            cancellable = true
    )
    private void RemoveMixin(Entity.RemovalReason p_150097_, CallbackInfo info) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(WenwenModMobEffects.BUSI.get())) {
            info.cancel(); // 取消后续原版逻辑
        }
    }


}
