package com.net.wenwen;

import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.LocalDate;


@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MusicEventHandler {
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if(event.getNewScreen() instanceof ReceivingLevelScreen){
            Tool.isGame=true;
        }
        if (event.getNewScreen() instanceof LevelLoadingScreen) {
            Minecraft mc = Minecraft.getInstance();
            SoundManager soundManager = mc.getSoundManager();
            boolean isChristmas = Tool.IsDay(12,25);
            boolean issr = Tool.IsSr();
            var r=Math.random();
            if(isChristmas){
                soundManager.play(SimpleSoundInstance.forMusic(WenwenModSounds.christmas.get()));
                return;
            }
            if(issr){
                soundManager.play(SimpleSoundInstance.forMusic(WenwenModSounds.srg.get()));
                return;
            }
            if (r < 0.5) {
                soundManager.play(SimpleSoundInstance.forMusic(WenwenModSounds.Loading.get()));
            } else if (r<0.8) {
                soundManager.play(SimpleSoundInstance.forMusic(WenwenModSounds.Loading_3.get()));
            } else{
                soundManager.play(SimpleSoundInstance.forMusic(WenwenModSounds.Loading_2.get()));
            }
        }

    }

}

