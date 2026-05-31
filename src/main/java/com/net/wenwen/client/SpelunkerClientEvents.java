package com.net.wenwen.client;

import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.network.SpelunkerBlockConfigManager;
import com.net.wenwen.network.SpelunkerOrePacketHandler;
import com.net.wenwen.spelunker.ChunkOres;
import com.net.wenwen.spelunker.SpelunkerEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SpelunkerClientEvents {

    private static Level lastLevel = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                // 检查世界是否切换
                Level currentLevel = minecraft.level;
                if (currentLevel != lastLevel) {
                    // 世界切换，清理数据
                    SpelunkerOrePacketHandler.getRenderer().clear();
                    lastLevel = currentLevel;
                }

                boolean hasEffect = minecraft.player.hasEffect(WenwenModMobEffects.SPELUNKER.get());
                boolean wasActive = SpelunkerOrePacketHandler.getRenderer().isActive();

                if (hasEffect != wasActive) {
                    SpelunkerOrePacketHandler.getRenderer().setActive(hasEffect);
                    if (hasEffect) {
                        SpelunkerOrePacketHandler.getRenderer().clear();
                    }
                }
            }
        }
    }

    // 如果你之前注册在 ForgeEvents 或者别的总线里，确保这个方法是在客户端执行的
    @SubscribeEvent
    public static void onClientBlockBreak(BlockEvent.BreakEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        // 直接判断客户端玩家是否有效果
        if (minecraft.player != null && minecraft.player.hasEffect(WenwenModMobEffects.SPELUNKER.get())) {
            // 注意：客户端事件中，直接使用 minecraft.level (它是 ClientLevel)
            // 千万不要用 event.getLevel()，因为那个可能是服务端的 Level 引用，会导致数据串流
            net.minecraft.client.multiplayer.ClientLevel world = minecraft.level;

            if (world == null) return;

            BlockPos pos = event.getPos();

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            int sectionY = world.getSectionIndex(pos.getY());

            // 重新扫描（此时世界里的方块已经被挖掉了，扫出来的结果是对的）
            ChunkOres ores = SpelunkerEffectManager.findOresInChunk(world, chunkX, chunkZ, sectionY);

            List<BlockPos> remove = new ArrayList<>();
            remove.add(new BlockPos(chunkX << 4, sectionY, chunkZ << 4));
            List<ChunkOres> add = new ArrayList<>();
            add.add(ores);

            SpelunkerOrePacketHandler.getRenderer().updateChunks(world, remove, add);
        }
    }


}
