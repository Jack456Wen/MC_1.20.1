package com.net.wenwen.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public abstract class BaseEnchantment extends Enchantment {

    protected BaseEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
        super(rarity, category, slots);
    }

    protected static int getCurrentLevelTool(ItemStack stack, Enchantment enchant) {
        if (stack.isEmpty()) return 0;
        return EnchantmentHelper.getTagEnchantmentLevel(enchant, stack);
    }

    @Override
    public int getMinCost(int level) { return 5 + (level - 1) * 8; }

    @Override
    public int getMaxCost(int level) { return getMinCost(level) + 10; }

    @Override
    public int getMaxLevel() { return 5; }

    @Override
    public boolean isTreasureOnly() { return false; }

    // ==========================================
    // 死亡事件分发
    // ==========================================
    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        handlePlayerAction(player, (baseEnchant, weapon, level) ->
                baseEnchant.onEntityDeath(player, weapon, level, event)
        );
    }

    // ==========================================
    // 伤害事件分发
    // ==========================================
    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        handlePlayerAction(player, (baseEnchant, weapon, level) ->
                baseEnchant.onEntityHurt(player, weapon, level, event)
        );
    }

    // ==========================================
    // 经验掉落事件分发
    // ==========================================
    @SubscribeEvent
    public static void handleEntityDropEvent(LivingExperienceDropEvent event)
    {
        if (event.getAttackingPlayer() == null) return;
        handlePlayerAction(event.getAttackingPlayer(), (baseEnchant, weapon, level) ->
                baseEnchant.onEntityDrop(event.getAttackingPlayer(), weapon, level, event)
        );
    }


    // 🌟 3. 核心提取：统一遍历武器附魔并分发，避免重复代码！
    private static void handlePlayerAction(Player player, EnchantmentAction action) {
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty() || !weapon.isEnchanted()) return;

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (entry.getKey() instanceof BaseEnchantment baseEnchant) {
                action.execute(baseEnchant, weapon, entry.getValue());
            }
        }
    }

    // 供子类重写的默认实现
    protected void onEntityDeath(Player killer, ItemStack weapon, int level, LivingDeathEvent event) {}

    // 🌟 供子类重写的伤害处理默认实现
    protected void onEntityHurt(Player attacker, ItemStack weapon, int level, LivingHurtEvent event) {}


    protected void onEntityDrop(Player attacker, ItemStack weapon, int level, LivingExperienceDropEvent event) {}

    // 🌟 内部函数式接口，用于简化分发逻辑
    @FunctionalInterface
    private interface EnchantmentAction {
        void execute(BaseEnchantment enchant, ItemStack weapon, int level);
    }
}
