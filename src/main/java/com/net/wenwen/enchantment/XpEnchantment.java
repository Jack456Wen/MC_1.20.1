package com.net.wenwen.enchantment;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;



public class XpEnchantment extends BaseEnchantment {
    public XpEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    protected void onEntityDrop(Player attacker, ItemStack weapon, int level, LivingExperienceDropEvent event)
    {
        if (level <= 0) {
            return;
        }
        event.setDroppedExperience(event.getDroppedExperience()+getRandomExpAmount(level,event.getDroppedExperience()));
    }
    private int getRandomExpAmount(int level,int xp) {
        return (int)(Math.round((level*0.25)*xp))+5;
    }

}
