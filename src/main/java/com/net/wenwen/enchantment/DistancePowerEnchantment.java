package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DistancePowerEnchantment  extends Enchantment {
    public DistancePowerEnchantment () {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5; // 最高等级为5
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 10;
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
        return stack.getItem() instanceof BowItem;
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof BowItem;
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof LivingEntity shooter)) return;

        ItemStack bow = shooter.getMainHandItem();
        if (!(bow.getItem() instanceof BowItem)) return;

        int enchantLevel = bow.getEnchantmentLevel(ModEnchantments.Distance.get());
        if (enchantLevel <= 0) return;

        // 计算距离
        double distance = arrow.position().distanceTo(shooter.position());
        if (distance > 10.0) {
            float extraDamage = (float) ((distance - 10.0) * 0.5 * enchantLevel);
            event.setAmount(event.getAmount() + extraDamage);
        }
    }
}
