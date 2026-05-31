package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HealthBoostEnchantment extends Enchantment {
    private static final UUID HEALTH_BOOST_UUID = UUID.fromString("c0d7f1b0-8f9a-11ed-a1eb-0242ac120002");
    private static final String HEALTH_BOOST_NAME = "healthBoostEnchantment";

    public HealthBoostEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
        MinecraftForge.EVENT_BUS.register(this);
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
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.ALL_DAMAGE_PROTECTION) {
            return false;
        }
        return super.checkCompatibility(other);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = event.getSlot();
        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        // 只处理胸甲槽位的变化
        if (slot != EquipmentSlot.CHEST) {
            return;
        }

        // 如果脱下胸甲
        if (from.getItem() instanceof ArmorItem &&
                ((ArmorItem) from.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST) {
            removeHealthBoost(entity);
        }

        // 如果穿上胸甲
        if (to.getItem() instanceof ArmorItem &&
                ((ArmorItem) to.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST) {
            int level=to.getEnchantmentLevel(ModEnchantments.Health.get());
            if (to.getEnchantmentLevel(ModEnchantments.Health.get()) > 0) {
                applyHealthBoost(entity,level);
            }
        }
    }

    private static void applyHealthBoost(LivingEntity entity,int level) {
        AttributeInstance attributeInstance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attributeInstance != null) {
            // 移除旧的修饰符（如果存在）
            attributeInstance.removeModifier(HEALTH_BOOST_UUID);

            // 获取基础生命值
            double baseHealth = entity.getAttributeBaseValue(Attributes.MAX_HEALTH);

            // 计算非药水效果的其他加成
            double otherBoosts = 0;
            for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                if (!isPotionModifier(modifier) && !modifier.getId().equals(HEALTH_BOOST_UUID)) {
                    if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                        otherBoosts += modifier.getAmount();
                    } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                        otherBoosts += baseHealth * modifier.getAmount();
                    } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        otherBoosts *= (1 + modifier.getAmount());
                    }
                }
            }

            // 计算增加10%生命值（基础值 + 其他非药水加成）
            double healthBoost = (baseHealth + otherBoosts) * (0.05+(level*0.04));

            // 添加新的修饰符（使用瞬态修饰符）
            attributeInstance.addTransientModifier(new AttributeModifier(
                    HEALTH_BOOST_UUID,
                    HEALTH_BOOST_NAME,
                    healthBoost,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    private static void removeHealthBoost(LivingEntity entity) {
        AttributeInstance attributeInstance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attributeInstance != null) {
            if (attributeInstance.getModifier(HEALTH_BOOST_UUID) != null) {
                attributeInstance.removeModifier(HEALTH_BOOST_UUID);
                // 确保当前生命值不超过新的最大值
                entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
            }
        }
    }

    // 判断是否为药水效果的修饰符
    private static boolean isPotionModifier(AttributeModifier modifier) {
        // 药水效果的UUID通常在特定范围内
        UUID id = modifier.getId();
        return (id.getMostSignificantBits() == 0L &&
                (id.getLeastSignificantBits() & 0xFFFF000000000000L) != 0L) ||
                modifier.getName().toLowerCase().contains("potion") ||
                modifier.getName().toLowerCase().contains("effect");
    }



    @Override
    public boolean isCurse() {
        return false;
    }
}

