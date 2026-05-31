package com.net.wenwen.utils;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class CuriosHelper {

    public static int getTotalCuriosCount(Player player) {
        // 使用局部变量在 lambda 外部接收结果
        int[] num = {0};

        // 1. 你的神仙发现：直接走 getCuriosInventory，没有红线警告！
        CuriosApi.getCuriosInventory(player).ifPresent(curiosHandler -> {

            // 2. 获取所有饰品类型的处理器
            Map<String, ICurioStacksHandler> curioMap = curiosHandler.getCurios();

            // 3. 遍历每一种饰品类型
            for (ICurioStacksHandler stacksHandler : curioMap.values()) {

                // 4. 获取真实的物品栏处理器
                IDynamicStackHandler itemStacks = stacksHandler.getStacks();

                // 5. 获取该类型饰品槽位的数量
                int slotCount = stacksHandler.getSlots();

                // 6. 遍历槽位并统计
                for (int i = 0; i < slotCount; i++) {
                    ItemStack stack = itemStacks.getStackInSlot(i); // 关键：用 getStackInSlot 拿真实物品
                    if (!stack.isEmpty()) {
                        num[0]++;
                    }
                }
            }
        });

        return num[0];
    }
}
