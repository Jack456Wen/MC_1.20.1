package com.net.wenwen.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class KillsCapability implements IKillsCapability, INBTSerializable<CompoundTag> {

    // 注册 Capability 的令牌
    public static final Capability<IKillsCapability> BOSS_KILLS = CapabilityManager.get(new CapabilityToken<>() {});

    private int kills = 0;
    private final ItemStack stack;

    public KillsCapability(ItemStack stack) {
        this.stack = stack;
        // 初始化时从 NBT 读取一次
        deserializeNBT(stack.getOrCreateTag());
    }

    @Override
    public int getKills() {
        return this.kills;
    }

    @Override
    public void setKills(int kills) {
        this.kills = kills;
        this.stack.getOrCreateTag().putInt("boss_kills", kills);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("boss_kills", this.kills);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.kills = nbt.getInt("boss_kills");
    }
}
