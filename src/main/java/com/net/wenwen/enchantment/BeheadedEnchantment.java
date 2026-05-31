package com.net.wenwen.enchantment;

import com.net.wenwen.damage.ModDamageSources;
import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeheadedEnchantment extends Enchantment {
    /*public static final TagKey<EntityType<?>> ModTarget = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation("wenwen", "behead")
    );*/

    public BeheadedEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5; // 最高等级为5
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
    public boolean isTreasureOnly() {
        return false; // 不是宝藏附魔，可以出现在附魔台
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof SwordItem; // 只能附魔在剑上
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }


    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if(event.getEntity().level().isClientSide)
            return;
        Player attacker = event.getEntity();
        Entity target = event.getTarget();
        if (target instanceof LivingEntity livingTarget) {
            ItemStack stack = attacker.getMainHandItem();
            if (stack.isEmpty()) return;
            int level = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Beheaded.get(), stack);
            level=Math.min(level,6);
            if (level > 0) {
                if (livingTarget.getHealth() <= livingTarget.getMaxHealth() * (0.04f * level)) {
                    // 对目标造成斩杀伤害
                    attacker.level().playSound(
                            null,
                            attacker.blockPosition(),
                            WenwenModSounds.ZHANSHA.get(),
                            SoundSource.AMBIENT,
                            1.0F,
                            1.0F
                    );
                    ModDamageSources damageSources = new ModDamageSources(attacker.level());
                    DamageSource trueDamage = damageSources.beheadDamage();
                    livingTarget.hurt(trueDamage, Float.MAX_VALUE);
                }
            }
        }
    }
}
