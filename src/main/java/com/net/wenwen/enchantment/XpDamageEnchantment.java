package com.net.wenwen.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class XpDamageEnchantment extends BaseEnchantment {

    public XpDamageEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
    protected void onEntityHurt(Player attacker, ItemStack weapon, int level, LivingHurtEvent event) {

        int xpLevel = attacker.experienceLevel;
        float multiplier = 1.0f + (float) xpLevel / Math.max(1, 100 - (level * 5));
        event.setAmount(event.getAmount() * multiplier);
    }
}
