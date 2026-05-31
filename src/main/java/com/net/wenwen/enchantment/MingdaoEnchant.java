package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MingdaoEnchant extends Enchantment {
    public MingdaoEnchant() {
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
        return 3;
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
        return super.checkCompatibility(other);
    }
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player player)) return;
        if(event.getAmount()>=player.getHealth())
        {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) return;
            int Level = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Mingdao.get(), stack);
            if(Level>0)
            {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("Cooldown")) {
                    long cooldownTime = tag.getLong("Cooldown");
                    if (cooldownTime > player.tickCount) {
                        return;
                    }
                }
                player.level().playSound(
                        null,
                        event.getEntity().blockPosition(),
                        WenwenModSounds.MINGDAO.get(),
                        SoundSource.AMBIENT,
                        1.0F,
                        1.0F
                );
                event.setAmount(0);
                tag.putLong("Cooldown", player.tickCount + Math.min(1200,2400-(Level*200)));
                player.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), 60+(Level*20), 0, false, false));
            }
        }
    }
}
