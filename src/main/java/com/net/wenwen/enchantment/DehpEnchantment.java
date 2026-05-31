package com.net.wenwen.enchantment;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class DehpEnchantment extends Enchantment {
    public DehpEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10;
    }
    @Override
    public boolean isTreasureOnly() {
        return false;
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }
    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        if (!attacker.level().isClientSide() && target instanceof LivingEntity targetEntity) {
            targetEntity.addEffect(new MobEffectInstance(WenwenModMobEffects.DEHP_EFFECT.get(), level*60, 0, false, false));
        }
    }
}
