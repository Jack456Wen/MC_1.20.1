package com.net.wenwen.common;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class WorldStateManager {

    public static boolean isWorldUp = false;

    /**
     * 从世界中获取 WorldBatState 实例。
     * 如果不存在，则会创建一个新的。
     * @param level 世界实例
     * @return WorldBatState 实例
     */
    public static WorldState getState(Level level) {
        // 确保我们在服务端运行，因为只有服务端有存档数据
        if (!(level instanceof ServerLevel)) {
            return new WorldState();
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // 从服务端世界获取 SavedData。
        // Minecraft 会自动处理：如果数据已存在，就从硬盘加载；如果不存在，就调用 factory 创建一个新的。
        return serverLevel.getDataStorage()
                .computeIfAbsent(
                        WorldState::load, // 如果数据存在，用这个方法加载
                        WorldState::new,  // 如果数据不存在，用这个方法创建一个新的
                        WorldState.DATA_NAME // 数据的唯一标识符
                );
    }
}

