package com.net.wenwen.mixin;

import com.net.wenwen.Tool;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import org.checkerframework.common.reflection.qual.Invoke;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.LocalDate;
import java.util.Calendar;


@Mixin(MusicManager.class)
public abstract class MixinMusicManager {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getSituationalMusic()Lnet/minecraft/sounds/Music;"
            )
    )
    private Music redirectSituationalMusic(Minecraft instance) {
        boolean isDay = Tool.IsSr();
        if(isDay){
            //instance.screen instanceof TitleScreen || instance.screen instanceof CreateWorldScreen || instance.screen instanceof SelectWorldScreen || instance.screen instanceof OptionsScreen || instance.screen instanceof SoundOptionsScreen || instance.screen instanceof JoinMultiplayerScreen || instance.screen instanceof DirectJoinServerScreen || instance.screen instanceof VideoSettingsScreen || instance.screen instanceof PackSelectionScreen
            if (!Tool.isGame) {

                Holder<SoundEvent> customSoundHolder = WenwenModSounds.srg.getHolder().orElseThrow();

                return new Music(customSoundHolder, 0, 0, true);
            }
        }
        return instance.getSituationalMusic();
    }

}

