package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EggEnchantment extends Enchantment {
    public EggEnchantment() {
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
        return 5;
    }
    @Override
    public boolean isTreasureOnly() {
        return false; // 不是宝藏附魔，可以出现在附魔台
    }


    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == ModEnchantments.DROP.get()) {
            return false;
        }
        if (other == Enchantments.MOB_LOOTING) {
            return false;
        }
        return super.checkCompatibility(other);
    }

    @SubscribeEvent
    public static void onLivingDrop(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if(victim instanceof Player){
            return;
        }
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity) {
            ItemStack weapon = ((LivingEntity) attacker).getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.Egg.get());
            if (level > 0 && Math.random() < Math.min(level * 0.02f,0.25f)) {
                var egg=AddEgg(victim);
                if(egg!=null){
                    event.getDrops().add(egg);
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.HEART,
                                victim.getX(), victim.getY() + 1.0, victim.getZ(),
                                15, // 粒子数量
                                0.5, 0.5, 0.5, // 粒子偏移
                                0.1 // 粒子速度
                        );
                        serverLevel.playSound(null, victim.getX(), victim.getY(), victim.getZ(), WenwenModSounds.SKILL_1.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            }
        }
    }
    private static ItemEntity AddEgg(LivingEntity target)
    {
        SpawnEggItem spawnEgg = SpawnEggItem.byId(target.getType());
        // 检查该生物是否有对应的生成蛋
        if (spawnEgg != null) {
            // 创建一个生成蛋的物品堆
            ItemStack spawnEggStack = new ItemStack(spawnEgg, 1); // 掉落1个
            // 创建物品实体
            ItemEntity spawnEggEntity = new ItemEntity(
                    target.level(),
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    spawnEggStack
            );
            return spawnEggEntity;
        }
        return null;
    }

}
