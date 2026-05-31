package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

public class BerserkerEnchantment extends Enchantment {
    public BerserkerEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 5;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }
    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        if (!attacker.level().isClientSide() && target instanceof LivingEntity) {
            // 检查是否是暴击（玩家下落攻击）
            boolean isCritical = attacker.fallDistance > 0.0F &&
                    !attacker.onGround() &&
                    !attacker.isInWater() &&
                    !attacker.hasEffect(MobEffects.BLINDNESS) &&
                    !attacker.isPassenger();

            if (isCritical) {
                // 增加暴击伤害，每级增加0.1倍
                float damageMultiplier = 1 + (level * 0.3F);
                // 获取攻击者的基础攻击伤害
                float baseDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                // 计算额外伤害量
                float extraDamage = baseDamage * damageMultiplier;

                // 对目标造成额外伤害
                target.hurt(attacker.damageSources().playerAttack((Player) attacker), extraDamage);
            }
        }
    }
}
