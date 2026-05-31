package com.net.wenwen.enchantment;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class PenetrateEnchantment extends Enchantment {
    public PenetrateEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10; // 最大值比最小值高10级
    }
    @Override
    public boolean isTreasureOnly() {
        return false; // 不是宝藏附魔，可以出现在附魔台
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem;
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem;
    }

    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        if (!attacker.level().isClientSide() && target instanceof LivingEntity targetEntity) {
            boolean isProjectile = attacker.getLastDamageSource() != null &&
                    attacker.getLastDamageSource().getDirectEntity() instanceof Projectile;
            level=Math.max(0,level-1);
            int amplifier = isProjectile ? level * 2 : level;
            targetEntity.addEffect(new MobEffectInstance(WenwenModMobEffects.SUILIE_EFFECT.get(), 100, amplifier, false, false));
        }
    }
}
