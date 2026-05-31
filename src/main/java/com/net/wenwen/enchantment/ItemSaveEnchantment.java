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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class ItemSaveEnchantment extends Enchantment {
    public ItemSaveEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
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
    public boolean canEnchant(ItemStack stack)
    {
        Item item = stack.getItem();
        return item instanceof TieredItem || item instanceof ArmorItem || item instanceof BowItem;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == ModEnchantments.Unbreak.get()) {
            return false;
        }
        if (other == Enchantments.MENDING) {
            return false;
        }
        return super.checkCompatibility(other);
    }

}
