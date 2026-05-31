package com.net.wenwen.block;

import com.net.wenwen.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityChest extends ChestBlockEntity {

    private boolean isLocked = true;
    public TileEntityChest(BlockPos pos, BlockState state) {
        super(Registration.CHEST_TILE_TYPE.get(), pos, state);
    }
    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
        // 状态改变后，必须通知客户端更新，否则渲染或同步会出问题
        this.setChanged();
    }

    // 重写保存方法，将 isLocked 存入 NBT
    @Override
    public void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putBoolean("IsLocked", this.isLocked);
    }

    // 重写读取方法，从 NBT 读取 isLocked
    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.isLocked = pTag.contains("IsLocked") ? pTag.getBoolean("IsLocked") : true;
    }


}
