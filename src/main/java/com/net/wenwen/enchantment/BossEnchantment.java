package com.net.wenwen.enchantment;

import com.net.wenwen.capabilities.KillsCapability;
import com.net.wenwen.entity.EntityOctorok;
import com.net.wenwen.entity.HostileBat;
import com.net.wenwen.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BossEnchantment extends BaseEnchantment {
    public BossEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean checkCompatibility(@NotNull Enchantment other) {
        return other != Enchantments.SHARPNESS &&other != ModEnchantments.DAMAGE_MULTIPLIER.get() && super.checkCompatibility(other);
    }

    @Override
    public int getMaxLevel() { return 1; }


    @Override
    protected void onEntityDeath(Player killer, ItemStack weapon, int enchantLevel, LivingDeathEvent event) {

        if (enchantLevel <= 0) return;
        if (killer.level().isClientSide) return;
        if (!(event.getEntity() instanceof WitherBoss) && !(event.getEntity() instanceof EnderDragon) && !(event.getEntity() instanceof EntityOctorok) && !(event.getEntity() instanceof HostileBat)) {
            return;
        }
        weapon.getCapability(KillsCapability.BOSS_KILLS).ifPresent(cap -> {
            int temp_Level=1;
            if ((event.getEntity() instanceof WitherBoss) || (event.getEntity() instanceof EnderDragon)) {
                temp_Level=5;
            }
            int newKills = cap.getKills() + (enchantLevel*temp_Level);
            cap.setKills(newKills);

            // ⭐⭐⭐ 核心：精准打击，只操作我们自己的修饰符 ⭐⭐⭐

            // 1. 构建我们旧的修饰符（用来精准移除）
            AttributeModifier oldModifier = new AttributeModifier(
                    UUID.fromString("d3b7e8c9-10a1-4b2f-8c9d-4e5f6a7b8c9d"),
                    "boss_kills",
                    newKills - 1, // 旧的击杀数
                    AttributeModifier.Operation.ADDITION
            );

            // 2. 构建我们新的修饰符（用来精准添加）
            AttributeModifier newModifier = new AttributeModifier(
                    UUID.fromString("d3b7e8c9-10a1-4b2f-8c9d-4e5f6a7b8c9d"),
                    "boss_kills",
                    newKills, // 新的击杀数
                    AttributeModifier.Operation.ADDITION
            );
            killer.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(oldModifier);

            killer.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(newModifier);

            killer.getInventory().setChanged();
        });
    }

}
