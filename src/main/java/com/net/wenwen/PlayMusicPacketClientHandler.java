package com.net.wenwen;

import com.net.wenwen.init.WenwenModItems;
import com.net.wenwen.init.WenwenModSounds;
import com.net.wenwen.network.PlayMusicPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayMusicPacketClientHandler {
    public static void handle(PlayMusicPacket msg) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            // 根据ID选择要播放的音乐
            if (msg.musicId == 0)
            {
                var r=Math.random();
                if(r<0.25)
                {
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.WENWEN1.get()), 10, 140, true)
                    );
                }
                else if (r<0.5 && r>0.25)
                {
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.WENWEN.get()), 10, 140, true)
                    );
                }
                else if (r<0.75 && r>0.5)
                {
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.WENWEN3.get()), 10, 140, true)
                    );
                }
                else
                {
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.WENWEN2.get()), 10, 140, true)
                    );
                }
            }
            //要塞
            else if(msg.musicId==1)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.STRONGHOLD.get()), 10, 140, true)
                );
            }
            //村庄音乐
            else if(msg.musicId==2)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                var r=Math.random();
                if(r<0.35){
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.VILLAGE.get()), 10, 140, true)
                    );
                }
                else if (r<0.75 && r>0.35)
                {
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.VILLAGE1.get()), 10, 140, true)
                    );
                }
                else{
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.VILLAGE2.get()), 10, 140, true)
                    );
                }

            }
            else if(msg.musicId==3)
            {
                var r=Math.random();
                if(r<0.5){
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.WENWEN.get()), 10, 140, true)
                    );
                }
                else if(r<0.75 && r>0.5)
                {
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.HUNT.get()), 10, 140, true)
                    );
                }
                else{
                    Minecraft.getInstance().getMusicManager().stopPlaying();
                    Minecraft.getInstance().getMusicManager().startPlaying(
                            new Music(Holder.direct(WenwenModSounds.WENWEN4.get()), 10, 140, true)
                    );
                }

            }
            else if(msg.musicId==4)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.DESERT.get()), 10, 140, true)
                );
            }
            //坚守者音乐
            else if(msg.musicId==200)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.CATCH.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==201)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.DANGER_3.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==202)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.DANGER_2.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==203)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.DANGER.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==204)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.PURSUE_3.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==205)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.PURSUE_2.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==206)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.PURSUE_1.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==300)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.christmas.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==301)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.srg.get()), 10, 140, true)
                );
            }
            else if(msg.musicId==302)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
                Minecraft.getInstance().getMusicManager().startPlaying(
                        new Music(Holder.direct(WenwenModSounds.Morning.get()), 10, 140, true)
                );
            }
            //停止
            else if (msg.musicId==-1)
            {
                Minecraft.getInstance().getMusicManager().stopPlaying();
            }
            else if (msg.musicId==99) {
                ItemStack item = new ItemStack(WenwenModItems.HEARTCONTAINER.get());
                Minecraft.getInstance().gameRenderer.displayItemActivation(item.copy());
            }
            //心跳音效
            else if (msg.musicId==501){
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(
                                WenwenModSounds.HEART_1.get(),
                                1.0F,
                                1.0F
                        )
                );
            }
            else if (msg.musicId==502){
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(
                                WenwenModSounds.HEART_2.get(),
                                1.0F,
                                1.0F
                        )
                );
            }
            //快速心跳
            else if (msg.musicId==503){
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(
                                WenwenModSounds.HEART_3.get(),
                                1.0F,
                                1.0F
                        )
                );
            }
            else if (msg.musicId==999) {
                ItemStack item = new ItemStack(WenwenModItems.HEART_AMULET.get());
                Minecraft.getInstance().gameRenderer.displayItemActivation(item.copy());
                SoundManager soundManager = Minecraft.getInstance().getSoundManager();

                // 创建一个声音实例
                SoundInstance sound = SimpleSoundInstance.forUI(WenwenModSounds.Mirror.get(), 1.0F,5);
                soundManager.play(sound);
            }
        }
    }
}
