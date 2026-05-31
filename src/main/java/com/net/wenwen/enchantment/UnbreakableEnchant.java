package com.net.wenwen.enchantment;

import com.net.wenwen.init.ModEnchantments; // 确保这个路径是正确的
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnbreakableEnchant extends Enchantment {
    public UnbreakableEnchant() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 30;
    }

    @Override
    public int getMaxLevel() {
        return 1; // 这是一个只有一级的附魔
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof TieredItem || item instanceof ArmorItem || item instanceof BowItem;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.UNBREAKING) {
            return false;
        }
        return super.checkCompatibility(other);
    }
    /*@SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        ItemStack stack = event.getTo();
        if (stack.isEmpty()) return;

        boolean hasUnbreakable = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Unbreak.get(), stack) > 0;

        CompoundTag tag = stack.getTag();

        if (hasUnbreakable && (tag == null || !tag.getBoolean("Unbreakable"))) {
            if (tag == null) {
                tag = new CompoundTag();
                stack.setTag(tag);
            }
            tag.putBoolean("Unbreakable", true);
            stack.setDamageValue(0);
            return;
        }
        if (tag != null && tag.contains("Unbreakable") &&!hasUnbreakable) {

            tag.remove("Unbreakable");
        }
    }*/

    /*@SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {

        if (event.getEntity().getType() != EntityType.ITEM) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (!stack.hasTag()) return;

        if (EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Unbreak.get(), stack) > 0) {
            // 使物品实体免疫火焰和岩浆伤害
            itemEntity.setInvulnerable(true);
            // 确保物品不会消失
            //itemEntity.setExtendedLifetime();
        }
    }*/



}
