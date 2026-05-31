package com.net.wenwen.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class WorldState extends SavedData {

    private boolean isUp = false;

    public static final String DATA_NAME = "wenwen_bat_state";

    // 3. 构造函数
    public WorldState() {}

    // 4. 从 NBT 数据加载状态 (读档时调用)
    public static WorldState load(CompoundTag tag) {
        WorldState state = new WorldState();
        // 从 NBT 中读取 "isUp" 的值，如果不存在则默认为 false
        state.isUp = tag.getBoolean("isUp");
        return state;
    }

    // 5. 将当前状态保存到 NBT (存档时调用)
    @Override
    public CompoundTag save(CompoundTag tag) {
        // 将 isUp 的值写入 NBT，键名为 "isUp"
        tag.putBoolean("isUp", this.isUp);
        return tag;
    }

    // --- 以下是方便你调用的公共方法 ---

    public boolean isUp() {
        return this.isUp;
    }

    public void setUp(boolean isUp) {
        // 只有当值真的改变时才更新并标记为脏数据
        if (this.isUp != isUp) {
            this.isUp = isUp;
            // 这是关键！每次修改数据后，必须调用 setDirty()
            // 这样 Minecraft 才知道需要把这个新状态保存到硬盘
            this.setDirty();
        }
    }
}
