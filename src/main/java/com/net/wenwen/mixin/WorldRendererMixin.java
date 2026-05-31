package com.net.wenwen.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.network.SpelunkerOrePacketHandler;
import com.net.wenwen.spelunker.SpelunkerEffectRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.net.wenwen.render.EndSkyRenderer;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    private EndSkyRenderer endSkyRenderer;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void renderEndSky(PoseStack poseStack, org.joml.Matrix4f matrix4f, float partialTick, net.minecraft.client.Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (level != null && level.dimension() == net.minecraft.world.level.Level.END) {
            if (endSkyRenderer == null) {
                endSkyRenderer=new EndSkyRenderer();
            }

            // 如果实例化成功，强转并调用 render 方法
            endSkyRenderer.render(
                    (int) level.getGameTime(),
                    partialTick,
                    poseStack,
                    level,
                    minecraft,
                    isFoggy,
                    1.0F,
                    setupFog
            );

            ci.cancel();
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
    private void renderSpelunkerEffect(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, net.minecraft.client.Camera camera, net.minecraft.client.renderer.GameRenderer gameRenderer, net.minecraft.client.renderer.LightTexture lightTexture, org.joml.Matrix4f projectionMatrix, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.hasEffect(WenwenModMobEffects.SPELUNKER.get())) {
            SpelunkerEffectRenderer renderer = SpelunkerOrePacketHandler.getRenderer();
            if (renderer.isActive()) {
                renderer.render(poseStack, camera);
            }
        }
    }
}