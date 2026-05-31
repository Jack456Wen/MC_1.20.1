package com.net.wenwen.events;

import com.net.wenwen.WenwenMod;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.network.PlayMusicPacket;
import com.net.wenwen.network.SpelunkerBlockConfigManager;
import com.net.wenwen.network.SpelunkerOrePacket;
import com.net.wenwen.potion.SpelunkerMobEffect;
import com.net.wenwen.spelunker.ChunkOres;
import com.net.wenwen.spelunker.SpelunkerEffectManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpelunkerServerEvents {

    // 探测范围半径（区块），默认2，会扫描 (2*radius+1) x (2*radius+1) x (2*radius+1) 个区块
    private static final int CHUNK_RADIUS = 1;
    private static final int UPDATE_INTERVAL = 20; // 每20tick更新一次

    @SubscribeEvent
    public static void onServerStarted(net.minecraftforge.event.server.ServerStartedEvent event) {
        SpelunkerBlockConfigManager.init();
    }
    // 当效果被移除或结束时触发
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance!=null&&instance.getEffect() instanceof SpelunkerMobEffect) {
            LivingEntity entity = event.getEntity();
            if(entity instanceof ServerPlayer player){
                PlayMusicPacket packet = new PlayMusicPacket(-1);
                WenwenMod.CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
    // 当效果被添加时触发
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        // 获取被添加的效果实例
        MobEffectInstance instance = event.getEffectInstance();
        if (instance!=null&&instance.getEffect() instanceof SpelunkerMobEffect) {
            LivingEntity entity = event.getEntity();
            if(entity instanceof ServerPlayer player){
                PlayMusicPacket packet = new PlayMusicPacket(1);
                // 3. 发送消息给特定的客户端玩家
                WenwenMod.CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }

        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            if (player.level().getGameTime() % UPDATE_INTERVAL == 0) {
                MobEffectInstance effect = player.getEffect(WenwenModMobEffects.SPELUNKER.get());
                if (effect != null) {
                    ArrayList<ChunkOres> chunks = (ArrayList<ChunkOres>) SpelunkerEffectManager.getSurroundingChunks(
                        player.level(),
                        player.blockPosition(),
                        CHUNK_RADIUS
                    );
                    if (!chunks.isEmpty()) {
                        SpelunkerOrePacket packet = new SpelunkerOrePacket(true, new ArrayList<>(), chunks, player.level().getMinSection());
                        WenwenMod.CHANNEL.sendTo(packet, player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            }
        }
    }
}
