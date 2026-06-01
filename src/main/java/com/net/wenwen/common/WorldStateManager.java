package com.net.wenwen.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;


public class WorldStateManager {

    // 内存缓存
    private static WorldState CACHED_STATE = null;

    public static WorldState getState(Level level) {
        // 客户端直接返回空壳，不走缓存逻辑（避免客户端污染服务器缓存）
        if (level.isClientSide) {
            return new WorldState();
        }

        // 1. 如果缓存存在，直接返回 (性能核心)
        if (CACHED_STATE != null) {
            return CACHED_STATE;
        }

        // 2. 缓存未命中，加载数据
        if (!(level instanceof ServerLevel serverLevel)) {
            return new WorldState();
        }

        MinecraftServer server = serverLevel.getServer();
        // 强制获取主世界
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        if (overworld == null) {
            return new WorldState();
        }

        // 3. 加载并存入缓存
        CACHED_STATE = overworld.getDataStorage()
                .computeIfAbsent(
                        WorldState::load,
                        WorldState::new,
                        WorldState.DATA_NAME
                );

        return CACHED_STATE;
    }

    public static boolean isWorldUp(Level level) {
        return getState(level).isUp();
    }

    public static void setWorldUp(Level level, boolean value) {
        WorldState state = getState(level);
        state.setUp(value);
    }

    public static void resetCache() {
        CACHED_STATE = null;
    }

}
