package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class ArmorEnchantment extends Enchantment {
    public ArmorEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 4;
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
        return stack.getItem() instanceof ArmorItem;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.PROJECTILE_PROTECTION) {
            return false;
        }
        if (other == Enchantments.BLAST_PROTECTION) {
            return false;
        }
        if (other == Enchantments.ALL_DAMAGE_PROTECTION) {
            return false;
        }
        if (other == Enchantments.FALL_PROTECTION) {
            return false;
        }
        if (other == Enchantments.FIRE_PROTECTION) {
            return false;
        }
        return super.checkCompatibility(other);
    }

}
