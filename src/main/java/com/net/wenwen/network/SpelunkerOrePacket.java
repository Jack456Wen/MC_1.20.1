package com.net.wenwen.network;

import com.net.wenwen.spelunker.ChunkOres;
import com.net.wenwen.spelunker.SpelunkerBlockConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class SpelunkerOrePacket {

    private final boolean overwrite;
    public final Collection<BlockPos> remove;
    public final Collection<ChunkOres> add;
    private final int bottomSectionCord;

    public SpelunkerOrePacket(boolean overwrite, Collection<BlockPos> remove, Collection<ChunkOres> add, int bottomSectionCord) {
        this.overwrite = overwrite;
        this.remove = remove;
        this.add = add;
        this.bottomSectionCord = bottomSectionCord;
    }

    public static SpelunkerOrePacket decode(FriendlyByteBuf buf) {
        boolean overwrite = buf.readBoolean();
        int c = buf.readVarInt();
        ArrayList<BlockPos> remove = new ArrayList<>(c);
        for (int i = 0; i < c; i++) {
            remove.add(new BlockPos(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        }

        c = buf.readVarInt();
        ArrayList<ChunkOres> chunks = new ArrayList<>(c);
        for (int i = 0; i < c; i++) {
            BlockPos pos = new BlockPos(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
            ChunkOres ores = overwrite ? new ChunkOres(pos) : new ChunkOres(pos);
            int cc = buf.readVarInt();
            for (int j = 0; j < cc; j++) {
                BlockPos orePos = new BlockPos(buf.readByte(), buf.readByte(), buf.readByte());
                int blockId = buf.readVarInt();
                if (blockId != -1) {
                    Block block = BuiltInRegistries.BLOCK.byId(blockId);
                    SpelunkerBlockConfig config = SpelunkerBlockConfigManager.getConfig(block);
                    ores.processConfig(orePos, config, true);
                }
            }
            if (overwrite) {
                // 转换本地坐标为世界坐标，使用区块的实际 sectionY
                ores.remapToBlockCoordinates(ores.sectionY);
                chunks.add(ores);
            }
        }

        int bottomSectionCord = overwrite ? buf.readVarInt() : 0;
        return new SpelunkerOrePacket(overwrite, remove, chunks, bottomSectionCord);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(overwrite);
        buf.writeVarInt(remove.size());
        for (BlockPos pos : remove) {
            buf.writeVarInt(pos.getX());
            buf.writeVarInt(pos.getY());
            buf.writeVarInt(pos.getZ());
        }

        buf.writeVarInt(add.size());
        for (ChunkOres ores : add) {
            ChunkPos chunkPos = ores.getPos();
            BlockPos pos = new BlockPos(chunkPos.getMinBlockX(), ores.sectionY * 16, chunkPos.getMinBlockZ());
            buf.writeVarInt(pos.getX());
            buf.writeVarInt(pos.getY());
            buf.writeVarInt(pos.getZ());

            buf.writeVarInt(ores.size());
            for (var ore : ores.entrySet()) {
                BlockPos orePos = ore.getKey();
                buf.writeByte(orePos.getX());
                buf.writeByte(orePos.getY());
                buf.writeByte(orePos.getZ());

                SpelunkerBlockConfig conf = ore.getValue();
                buf.writeVarInt(conf == null ? -1 : BuiltInRegistries.BLOCK.getId(conf.getBlock()));
            }
        }
        if (overwrite)
            buf.writeVarInt(bottomSectionCord);
    }

    public static void handle(SpelunkerOrePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                SpelunkerOrePacketHandler.handle(msg);
            });
        });
        context.setPacketHandled(true);
    }
}
