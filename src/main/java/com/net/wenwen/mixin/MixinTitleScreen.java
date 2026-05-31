package com.net.wenwen.mixin;


import com.bobmowzie.mowziesmobs.server.potion.EffectSunblock;
import com.net.wenwen.Tool;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

@Mixin(SplashManager.class)
public abstract class MixinTitleScreen {

    @Inject(
            method = "getSplash",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkForNationalDay(CallbackInfoReturnable<SplashRenderer> cir) {

        boolean isSrg = Tool.IsSr();

        if (isSrg) {
            SplashRenderer nationalDaySplash = new SplashRenderer("文文祝你生日快乐！");
            cir.setReturnValue(nationalDaySplash);
        }
        else{
            SplashRenderer nationalDaySplash = new SplashRenderer("~文文~");
            cir.setReturnValue(nationalDaySplash);
        }

    }
}


