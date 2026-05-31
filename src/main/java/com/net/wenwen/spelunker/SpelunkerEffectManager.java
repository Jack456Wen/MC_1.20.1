package com.net.wenwen.spelunker;

import com.net.wenwen.network.SpelunkerBlockConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.Collection;

public class SpelunkerEffectManager {

    public static ChunkOres findOresInChunk(Level world, int chunkX, int chunkZ, int sectionY) {
        LevelChunk chunk = world.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return ChunkOres.EMPTY;
        }

        if (sectionY < 0 || sectionY >= chunk.getSectionsCount()) {
            return ChunkOres.EMPTY;
        }

        LevelChunkSection section = chunk.getSection(sectionY);
        if (section == null || section.hasOnlyAir()) {
            return ChunkOres.EMPTY;
        }

        ChunkOres ores = new ChunkOres(new BlockPos(chunkX << 4, sectionY << 4, chunkZ << 4));

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState blockState = section.getBlockState(x, y, z);
                    Block block = blockState.getBlock();
                    // 快速检查是否是矿石
                    if (SpelunkerBlockConfigManager.isOreBlock(block)) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        ores.put(blockPos, SpelunkerBlockConfigManager.getConfig(block));
                    }
                }
            }
        }

        return ores;
    }

    public static Collection<ChunkOres> getSurroundingChunks(Level world, BlockPos playerPos, int radius) {
        ArrayList<ChunkOres> chunks = new ArrayList<>();
        int centerX = playerPos.getX() >> 4;
        int playerSectionY = world.getSectionIndex(playerPos.getY());
        int centerZ = playerPos.getZ() >> 4;

        // 限制垂直扫描范围，只扫描玩家周围一定范围内的区块，而不是从世界底部到玩家高度
        int minSectionY = Math.max(world.getMinSection(), playerSectionY - 1);
        int maxSectionY = Math.min(world.getMaxSection(), playerSectionY + 1);

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int y = minSectionY; y <= maxSectionY; y++) {
                    ChunkOres ores = findOresInChunk(world, x, z, y);
                    if (!ores.isEmpty()) {
                        chunks.add(ores);
                    }
                }
            }
        }

        return chunks;
    }
}
