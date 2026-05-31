package com.net.wenwen;

import com.net.wenwen.init.ModEnchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


import java.util.Iterator;


import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class itemDropEvent {

    private static final String SOULBOUND_TAG = "wenwen_soulbound_items";

    private itemDropEvent() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handlePlayerDropsEvent(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 如果启用了keepInventory游戏规则，则不处理
        if (player.level().getGameRules().getBoolean(RULE_KEEPINVENTORY)) {
            return;
        }

        // 获取玩家的持久化数据
        CompoundTag persistedTag = player.getPersistentData();
        if (!persistedTag.contains(Player.PERSISTED_NBT_TAG)) {
            persistedTag.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        CompoundTag soulboundTag = persistedTag.getCompound(Player.PERSISTED_NBT_TAG);

        ListTag soulboundItems = new ListTag();

        // 遍历所有掉落物品
        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();
            ItemStack stack = itemEntity.getItem();

            // 检查物品是否有灵魂绑定附魔
            if (EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.ItemSave.get(), stack) > 0) {
                // 将物品从掉落列表中移除
                iterator.remove();

                // 将物品保存到NBT中
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                soulboundItems.add(itemTag);
            }
        }

        // 如果有灵魂绑定物品，保存到玩家的持久化数据中
        if (!soulboundItems.isEmpty()) {
            soulboundTag.put(SOULBOUND_TAG, soulboundItems);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handlePlayerCloneEvent(PlayerEvent.Clone event) {
        // 只处理死亡导致的重生
        if (!event.isWasDeath()) {
            return;
        }

        Player originalPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        // 获取原始玩家的持久化数据
        CompoundTag originalPersistedTag = originalPlayer.getPersistentData();
        if (!originalPersistedTag.contains(Player.PERSISTED_NBT_TAG)) {
            return;
        }

        CompoundTag originalTag = originalPersistedTag.getCompound(Player.PERSISTED_NBT_TAG);
        if (!originalTag.contains(SOULBOUND_TAG)) {
            return;
        }

        // 获取灵魂绑定物品列表
        ListTag soulboundItems = originalTag.getList(SOULBOUND_TAG, TAG_COMPOUND);

        // 将物品添加到新玩家的物品栏中
        for (int i = 0; i < soulboundItems.size(); i++) {
            CompoundTag itemTag = soulboundItems.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);

            // 尝试将物品添加到物品栏
            if (!newPlayer.getInventory().add(stack)) {
                // 如果物品栏满了，则掉落在玩家周围
                newPlayer.spawnAtLocation(stack);
            }
        }

        // 清除已处理的灵魂绑定物品数据
        originalTag.remove(SOULBOUND_TAG);
    }
}
