package com.net.wenwen;

//import com.github.alexthe666.iceandfire.entity.EntityDragonBase;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.util.*;


public class WardenMusicManager {

    private final Set<UUID> onlinePlayerIdsCache = new HashSet<>();
    private final Set<UUID> onlinePlayerIds = new HashSet<>();
    // 修改 PlayerMusicState 内部类，增加计时功能
    private class PlayerMusicState {
        private int currentMusicId = -1; // -1 表示没有在播放任何Warden相关音乐
        private int timerTicks = 0;     // 计时器，记录当前音乐已经播放了多久

        public int getCurrentMusicId() {
            return currentMusicId;
        }

        public void setCurrentMusicId(int currentMusicId) {
            this.currentMusicId = currentMusicId;
            // 每次设置新音乐时，重置计时器
            this.timerTicks = 0;
        }

        public int getTimerTicks() {
            return timerTicks;
        }

        public void incrementTimer() {
            this.timerTicks++;
        }

        // 根据ID获取音乐总时长（单位：tick）
        public int getDurationForMusic(int musicId) {
            switch (musicId) {
                case 200: return 20; // 20秒 * 20 ticks/秒
                case 201: return 8;  // 8秒
                case 202: return 8;  // 8秒
                case 203: return 8;  // 8秒
                case 204: return 28;  // 近距离
                case 205: return 18;  // 中距离
                case 206: return 17;  // 远距离
                default: return 0;         // 未知音乐或无音乐，时长为0
            }
        }
    }
    private class PlayerMusicState01 {
        private int currentMusicId = -1;
        private int timerTicks = 0;

        public int getCurrentMusicId() {
            return currentMusicId;
        }

        public void setCurrentMusicId(int currentMusicId) {
            this.currentMusicId = currentMusicId;
            // 每次设置新音乐时，重置计时器
            this.timerTicks = 0;
        }

        public int getTimerTicks() {
            return timerTicks;
        }

        public void incrementTimer() {
            this.timerTicks++;
        }

        // 根据ID获取音乐总时长（单位：tick）
        public int getDurationForMusic(int musicId) {
            switch (musicId) {
                case 204: return 27;  // 近距离
                case 205: return 18;  // 中距离
                case 206: return 17;  // 远距离
                default: return 0;         // 未知音乐或无音乐，时长为0
            }
        }
    }

    private class PlayerHeart {
        private int currentMusicId = -1;
        private int timerTicks = 0;

        public int getCurrentMusicId() {
            return currentMusicId;
        }

        public void setCurrentMusicId(int currentMusicId) {
            if(currentMusicId == -1){
                this.currentMusicId = -1;
            }
            else if(currentMusicId == 203){
                this.currentMusicId = 501;
            }
            else if(currentMusicId == 202){
                this.currentMusicId = 502;
            }
            else if(currentMusicId == 201){
                this.currentMusicId = 502;
            }
            else if(currentMusicId == 200){
                this.currentMusicId = 503;
            }
            // 每次设置新音乐时，重置计时器
            this.timerTicks = 0;
        }

        public int getTimerTicks() {
            return timerTicks;
        }

        public void incrementTimer() {
            this.timerTicks++;
        }

        // 根据ID获取音乐总时长（单位：tick）
        public int getDurationForMusic(int musicId) {
            switch (musicId) {
                case 200: return 1;  // 近距离
                case 201: return 2;  // 中距离
                case 202: return 2;
                case 203: return 3;// 远距离
                default: return 0;         // 未知音乐或无音乐，时长为0
            }
        }
    }

    private final Map<UUID, PlayerMusicState> playerMusicStates = new HashMap<>();
    private final Map<UUID, PlayerMusicState01> playerMusicStates01 = new HashMap<>();
    private final Map<UUID, PlayerHeart> playerHeartMap = new HashMap<>();

    public void tick(Iterable<ServerPlayer> players) {
        if(EntityTracker.GetEntityListisEmpty()){
            return;
        }
        onlinePlayerIdsCache.clear();

        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            onlinePlayerIdsCache.add(playerId);
            //心跳
            PlayerHeart Heartstate = playerHeartMap.computeIfAbsent(playerId, id -> new PlayerHeart());
            PlayerMusicState state = playerMusicStates.computeIfAbsent(playerId, id -> new PlayerMusicState());

            Warden nearestWarden = null;

            nearestWarden=EntityTracker.findNearestWarden(player,100);

            if (nearestWarden != null) {
                double distanceSq = player.distanceToSqr(nearestWarden);
                int targetMusicId = determineMusicId(distanceSq);

                // --- 核心逻辑修改点 ---

                // 情况1：玩家进入了新的音乐范围，或者刚进入范围
                if (state.getCurrentMusicId() != targetMusicId) {
                    MusicPlayerManager.StopMusicWander(player);
                    MusicPlayerManager.PlayMusicWander(player, targetMusicId);
                    state.setCurrentMusicId(targetMusicId); // 这会同时重置timerTicks
                    //心跳
                    Heartstate.setCurrentMusicId(targetMusicId);
                }
                // 情况2：玩家停留在当前音乐范围内
                else if (state.getCurrentMusicId() != -1) {
                    // 增加计时器
                    state.incrementTimer();
                    Heartstate.incrementTimer();

                    // 检查是否已经播完
                    if (state.getTimerTicks() >= state.getDurationForMusic(state.getCurrentMusicId())) {
                        // 播完了！重新播放当前音乐，形成循环
                        MusicPlayerManager.StopMusicWander(player); // 先停，确保从头播放
                        MusicPlayerManager.PlayMusicWander(player, state.getCurrentMusicId());
                        state.setCurrentMusicId(state.getCurrentMusicId()); // 重置计时器
                    }
                    //心跳
                    if (Heartstate.getTimerTicks() >= Heartstate.getDurationForMusic(state.getCurrentMusicId())){
                        MusicPlayerManager.PlayMusicDragon(player, Heartstate.getCurrentMusicId());
                        Heartstate.setCurrentMusicId(state.getCurrentMusicId()); // 重置计时器
                    }
                }
            } else {
                // 玩家离开了所有Warden的音乐范围
                if (state.getCurrentMusicId() != -1) {
                    MusicPlayerManager.StopMusicWander(player);
                    state.setCurrentMusicId(-1); // 这会同时重置timerTicks
                }
            }
        }

        // 清理离线玩家的状态
        playerMusicStates.keySet().removeIf(playerId -> !onlinePlayerIdsCache.contains(playerId));
        playerHeartMap.keySet().removeIf(id -> !onlinePlayerIdsCache.contains(id));
    }

    public void tick_2(Iterable<ServerPlayer> players) {
        if (!DragonTracker.isLoad){
            return;
        }
        onlinePlayerIds.clear();

        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            onlinePlayerIds.add(playerId);


            PlayerMusicState01 state = playerMusicStates01.computeIfAbsent(playerId, id -> new PlayerMusicState01());

            Entity nearestDragon = null;

            nearestDragon=DragonTracker.findNearestDragon(player,260);
            if (nearestDragon != null) {
                double distanceSq = player.distanceToSqr(nearestDragon);
                int targetMusicId = DragonMusicId(distanceSq);
                if(targetMusicId==205 || targetMusicId==204){
                    player.addEffect(new MobEffectInstance(WenwenModMobEffects.Fear.get(), 400, 0, false, true));
                }
                // 情况1：玩家进入了新的音乐范围，或者刚进入范围
                if (state.getCurrentMusicId() != targetMusicId) {
                    MusicPlayerManager.StopMusicDragon(player);
                    MusicPlayerManager.PlayMusicDragon(player, targetMusicId);
                    state.setCurrentMusicId(targetMusicId); // 这会同时重置timerTicks
                }
                // 情况2：玩家停留在当前音乐范围内
                else if (state.getCurrentMusicId() != -1) {
                    // 增加计时器
                    state.incrementTimer();
                    // 检查是否已经播完
                    if (state.getTimerTicks() >= state.getDurationForMusic(state.getCurrentMusicId())) {
                        // 播完了！重新播放当前音乐，形成循环
                        MusicPlayerManager.StopMusicDragon(player); // 先停，确保从头播放
                        MusicPlayerManager.PlayMusicDragon(player, state.getCurrentMusicId());
                        state.setCurrentMusicId(state.getCurrentMusicId()); // 重置计时器
                    }
                }
            } else {
                if (state.getCurrentMusicId() != -1) {
                    MusicPlayerManager.StopMusicDragon(player);
                    state.setCurrentMusicId(-1); // 这会同时重置timerTicks
                }
            }
        }

        // 清理离线玩家的状态
        playerMusicStates01.keySet().removeIf(playerId -> !onlinePlayerIds.contains(playerId));
    }


    private int determineMusicId(double distance) {
        if (distance < 64) return 200;
        else if (distance < 484) return 201;
        else if (distance < 2916) return 202;
        else if (distance < 6400) return 203;
        return -1;
    }

    private int DragonMusicId(double distance) {
        if (distance < 14400) return 204;
        else if (distance < 34000) return 205;
        else if (distance < 67600) return 206;
        return -1;
    }
}
