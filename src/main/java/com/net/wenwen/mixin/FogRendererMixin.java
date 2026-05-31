package com.net.wenwen.mixin;

import com.net.wenwen.render.CustomFogRenderer;
import com.net.wenwen.util.BackgroundInfo;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void fogDensity(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float g, CallbackInfo ci) {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.dimension() == Level.END) {
            if (CustomFogRenderer.applyFogDensity(camera, viewDistance)) {
                ci.cancel();
            }
        }
    }
}