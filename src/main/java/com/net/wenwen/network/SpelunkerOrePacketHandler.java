package com.net.wenwen.network;

import com.net.wenwen.spelunker.ChunkOres;
import com.net.wenwen.spelunker.SpelunkerEffectRenderer;
import net.minecraft.client.Minecraft;

public class SpelunkerOrePacketHandler {

    private static SpelunkerEffectRenderer renderer;

    public static void handle(SpelunkerOrePacket msg) {
        if (renderer == null) {
            renderer = new SpelunkerEffectRenderer();
            // 检查玩家是否有效果，如果有则激活渲染器
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                boolean hasEffect = minecraft.player.hasEffect(com.net.wenwen.init.WenwenModMobEffects.SPELUNKER.get());
                renderer.setActive(hasEffect);
            }
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            renderer.updateChunks(minecraft.level, msg.remove, msg.add);
        }
    }

    public static SpelunkerEffectRenderer getRenderer() {
        if (renderer == null) {
            renderer = new SpelunkerEffectRenderer();
        }
        return renderer;
    }
}
