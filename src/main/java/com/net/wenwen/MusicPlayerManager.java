package com.net.wenwen;

import com.net.wenwen.WenwenMod;
import com.net.wenwen.network.PlayMusicPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WenwenMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MusicPlayerManager {

    // 音乐循环的间隔 Tick 数 (2600 ticks ≈ 130秒)
    private static final int MUSIC_LOOP_INTERVAL = 2600;

    // 将音乐ID直接绑定到枚举上，省去后续的 if-else 映射
    public enum MCType {
        VILLAGE(2),
        CELL(0),       // CELL 特殊，单独处理
        ANCIENT_CITY(3),
        STRONGHOLD(1),
        DESERT(4),
        DEFAULT(-1);

        public final int musicId;
        MCType(int musicId) {
            this.musicId = musicId;
        }
    }

    // 玩家音乐数据封装类
    private static class PlayerMusicData {
        MCType type;
        int timer;

        PlayerMusicData(MCType type) {
            this.type = type;
            this.timer = 0;
        }
    }

    // 只需要一个 Map 即可，服务端事件是主线程，使用 HashMap 性能最佳
    private static final Map<UUID, PlayerMusicData> playerMusicMap = new HashMap<>();

    // 添加玩家到循环播放列表（精简后的逻辑）
    public static void addPlayer(ServerPlayer player, MCType type) {
        if (player == null || type == MCType.DEFAULT) return;

        UUID uuid = player.getUUID();
        PlayerMusicData data = playerMusicMap.get(uuid);

        // 只有当玩家不在列表中，或者当前播放类型与目标类型不同时，才更新并重置计时
        if (data == null || data.type != type) {
            playerMusicMap.put(uuid, new PlayerMusicData(type));
            playMusicForPlayer(player, type);
        }
    }

    public static void PlayMusicWander(ServerPlayer player, int id) {
        if (player != null) {
            playMusicForPlayer(player, id);
        }
    }

    public static void StopMusicWander(ServerPlayer player) {
        if (player != null) {
            stopPlayMusic(player);
        }
    }

    public static void PlayMusicDragon(ServerPlayer player, int id) {
        if (player != null) {
            playMusicForPlayer(player, id);
        }
    }

    public static void StopMusicDragon(ServerPlayer player) {
        if (player != null) {
            stopPlayMusic(player);
        }
    }

    public static void removePlayer(ServerPlayer player) {
        if (player != null) {
            // 移除并停止音乐，如果返回不为空说明之前在播放
            if (playerMusicMap.remove(player.getUUID()) != null) {
                stopPlayMusic(player);
            }
        }
    }

    // 服务器 Tick 事件
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        // 使用 removeIf 安全清理离线玩家，避免 ConcurrentModificationException
        playerMusicMap.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            PlayerMusicData data = entry.getValue();

            // 获取玩家实例
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) {
                // 玩家已下线但未触发 Logout 事件（异常情况），返回 true 移除该条目
                return true;
            }

            // 计时器增加
            data.timer++;

            // 达到循环时间
            if (data.timer >= MUSIC_LOOP_INTERVAL) {
                playMusicForPlayer(player, data.type);
                data.timer = 0; // 重置计时器
            }

            return false; // 返回 false 保留该条目
        });
    }

    // 玩家离开服务器事件
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            removePlayer(player);
        }
    }

    // --- 内部播放逻辑提取 ---

    private static void playMusicForPlayer(ServerPlayer player, MCType type) {
        if (player == null) return;

        // CELL 类型特殊处理，走无参/0逻辑；其他类型直接从枚举取 ID
        int musicId = (type == MCType.CELL) ? 0 : type.musicId;
        playMusicForPlayer(player, musicId);
    }

    public static void playMusicForPlayer(ServerPlayer player, int id) {
        if (player == null) return;
        PlayMusicPacket packet = new PlayMusicPacket(id);
        WenwenMod.CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static void stopPlayMusic(ServerPlayer player) {
        if (player == null) return;
        PlayMusicPacket packet = new PlayMusicPacket(-1);
        WenwenMod.CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
