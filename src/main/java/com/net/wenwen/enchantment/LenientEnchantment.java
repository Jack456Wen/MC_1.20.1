package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class LenientEnchantment extends Enchantment {
    public LenientEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8; // 1级需要5级附魔，2级需要13级，3级需要21级
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10; // 最大值比最小值高10级
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
    @Override
    public boolean isTreasureOnly() {
        return false; // 不是宝藏附魔，可以出现在附魔台
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == ModEnchantments.Beheaded.get()) {
            return false;
        }
        return super.checkCompatibility(other);
    }
}
