package com.net.wenwen.mixin;

import com.net.wenwen.Tool;
import net.minecraft.client.Minecraft;

import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.LocalDate;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
    private void onCreateTitle(CallbackInfoReturnable<String> cir) {
        String customTitle="文文整合包(挑战版)";
        boolean issr = Tool.IsSr();
        if(issr){
            customTitle = "文文整合包-祝文文生日快乐(●'◡'●)";
        }
        else{
            customTitle = "文文整合包(挑战版)";
        }
        cir.setReturnValue(customTitle);

    }
}
