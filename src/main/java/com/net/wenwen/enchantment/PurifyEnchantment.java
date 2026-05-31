package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class PurifyEnchantment extends Enchantment {
    public PurifyEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
    @Override
    public boolean isTreasureOnly() {
        return false;
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
        if (other == ModEnchantments.DAMAGE_MULTIPLIER.get()) {
            return false;
        }
        if (other == ModEnchantments.Beheaded.get()) {
            return false;
        }
        if (other == Enchantments.SMITE) {
            return false;
        }
        return super.checkCompatibility(other);
    }
    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        if (target instanceof LivingEntity living) {
            if (level > 0) {
                if (target instanceof ZombieVillager villager) {
                    double successChance = 0.1 * level;
                    if(Math.random()<successChance){
                        ConverToVillage(villager,attacker);
                    }
                }
                if (target instanceof Zombie zombie)
                {
                    float damage = Math.max (40,level*(living.getMaxHealth()*0.05f));
                    zombie.hurt(attacker.damageSources().mobAttack(attacker),damage);
                }
            }
        }
    }
    private void ConverToVillage(ZombieVillager zombieVillager, LivingEntity player)
    {
        CompoundTag tag = new CompoundTag();

        int conversionTime = 20;
        tag.putInt("ConversionTime", conversionTime);

        tag.putUUID("ConversionPlayer", player.getUUID());

        zombieVillager.readAdditionalSaveData(tag);
    }

}
