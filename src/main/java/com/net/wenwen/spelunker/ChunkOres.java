package com.net.wenwen.spelunker;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkOres extends ConcurrentHashMap<BlockPos, SpelunkerBlockConfig> {

    public static final ChunkOres EMPTY = new ChunkOres(BlockPos.ZERO);

    private final ChunkPos pos;
    public final int sectionY;
    private boolean remapped = false;
    private int bottomSectionCord;

    public ChunkOres(BlockPos pos) {
        this.pos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        this.sectionY = pos.getY() >> 4;
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public boolean isRemapped() {
        return this.remapped;
    }

    public void processConfig(BlockPos pos, SpelunkerBlockConfig conf, boolean localPos) {
        if(this.remapped && localPos)
            pos = toBlockCoord(pos, this.pos, this.bottomSectionCord);
        else if(!this.remapped && !localPos)
            pos = toLocalCoord(pos);
        if(conf == null)
            remove(pos);
        else put(pos, conf);
    }

    public ChunkOres remapToBlockCoordinates(int bottomSectionCord) {
        this.remapped = true;
        this.bottomSectionCord = bottomSectionCord;
        HashMap<BlockPos, SpelunkerBlockConfig> clone = new HashMap<>(this);
        clear();
        for (Map.Entry<BlockPos, SpelunkerBlockConfig> pair : clone.entrySet())
            put(toBlockCoord(pair.getKey(), this.pos, bottomSectionCord), pair.getValue());
        return this;
    }

    public static BlockPos toLocalCoord(BlockPos blockPos) {
        return new BlockPos(
                blockPos.getX() & 15,
                blockPos.getY() & 15,
                blockPos.getZ() & 15
        );
    }

    public static BlockPos toBlockCoord(BlockPos localPos, ChunkPos sectionPos, int sectionY) {
        return new BlockPos(
                sectionPos.getMinBlockX() + localPos.getX(),
                (sectionY * 16) + localPos.getY() - 64, // 减去 64，因为 Minecraft 世界的底部是 Y=-64
                sectionPos.getMinBlockZ() + localPos.getZ()
        );
    }
}
