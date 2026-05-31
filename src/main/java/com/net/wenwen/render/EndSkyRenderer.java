package com.net.wenwen.render;

import com.net.wenwen.Config;
import com.net.wenwen.util.BackgroundInfo;
import com.net.wenwen.util.MHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EndSkyRenderer extends DimensionSpecialEffects {

    private static final ResourceLocation NEBULA_1 = new ResourceLocation("wenwen", "textures/sky/nebula_2.png");
    private static final ResourceLocation NEBULA_2 = new ResourceLocation("wenwen", "textures/sky/nebula_3.png");
    private static final ResourceLocation HORIZON = new ResourceLocation("wenwen", "textures/sky/nebula_1.png");
    private static final ResourceLocation STARS = new ResourceLocation("wenwen", "textures/sky/stars.png");
    private static final ResourceLocation FOG = new ResourceLocation("wenwen", "textures/sky/fog.png");

    private Vector3f axis1;
    private Vector3f axis2;
    private Vector3f axis3;
    private Vector3f axis4;

    private boolean initialised;

    public EndSkyRenderer() {
        super(Float.NaN, false, SkyType.END, true, false);
    }

    private void initialise() {
        if (!initialised) {
            RandomSource random = new LegacyRandomSource(131);
            axis1 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            axis2 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            axis3 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            axis4 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            axis1.normalize();
            axis2.normalize();
            axis3.normalize();
            axis4.normalize();
            initialised = true;
        }
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.scale((double)0.15F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    public void render(int ticks, float partialTick, PoseStack poseStack, ClientLevel level, Minecraft minecraft, boolean isFoggy, float skyDarken, Runnable setupFog) {
        if(!Config.enderSky){
            return;
        }
        initialise();

        float time = ((level.getDayTime() + partialTick) % 360000) * 0.000017453292F;
        float time2 = time * 2;
        float time3 = time * 3;

        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        float blindA = 1F - BackgroundInfo.blindness;
        float blind02 = blindA * 0.2F;
        float blind06 = blindA * 0.6F;

        if (blindA > 0) {
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotationXYZ(0, time, 0));
            RenderSystem.setShaderTexture(0, HORIZON);
            renderHorizon(poseStack, 0.77F, 0.31F, 0.73F, 0.7F * blindA);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotationXYZ(0, -time, 0));
            RenderSystem.setShaderTexture(0, NEBULA_1);
            renderNebula(poseStack, 30, 0.77F, 0.31F, 0.73F, blind02);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotationXYZ(0, time2, 0));
            RenderSystem.setShaderTexture(0, NEBULA_2);
            renderNebula(poseStack, 10, 0.77F, 0.31F, 0.73F, blind02);
            poseStack.popPose();

            RenderSystem.setShaderTexture(0, STARS);

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().setAngleAxis(time, axis3.x, axis3.y, axis3.z));
            renderStars(poseStack, 1000, 0.4, 1.2, 0.77F, 0.31F, 0.73F, blind06, true);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().setAngleAxis(time2, axis4.x, axis4.y, axis4.z));
            renderStars(poseStack, 1000, 0.4, 1.2, 1F, 1F, 1F, blind06, true);
            poseStack.popPose();
        }

        float a = (BackgroundInfo.fogDensity - 1F);
        if (a > 0) {
            if (a > 1) a = 1;
            RenderSystem.setShaderTexture(0, FOG);
            renderFog(poseStack, BackgroundInfo.fogColorRed, BackgroundInfo.fogColorGreen, BackgroundInfo.fogColorBlue, a);
        }

        if (blindA > 0) {
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().setAngleAxis(time3, axis1.x, axis1.y, axis1.z));
            renderStars(poseStack, 3500, 0.1, 0.30, 1, 1, 1, blind06, false);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().setAngleAxis(time2, axis2.x, axis2.y, axis2.z));
            renderStars(poseStack, 2000, 0.1, 0.35, 0.95F, 0.64F, 0.93F, blind06, false);
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderHorizon(PoseStack matrices, float r, float g, float b, float a) {
        RenderSystem.setShaderColor(r, g, b, a);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        makeCylinder(buffer, matrices, 16, 50, 100);
        Tesselator.getInstance().end();
    }

    private void renderFog(PoseStack matrices, float r, float g, float b, float a) {
        RenderSystem.setShaderColor(r, g, b, a);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        makeCylinder(buffer, matrices, 16, 50, 70);
        Tesselator.getInstance().end();
    }

    private void renderNebula(PoseStack matrices, int count, float r, float g, float b, float a) {
        RenderSystem.setShaderColor(r, g, b, a);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        makeFarFog(buffer, matrices, 40, 60, count, 11515);
        Tesselator.getInstance().end();
    }

    private void renderStars(PoseStack matrices, int count, double minSize, double maxSize, float r, float g, float b, float a, boolean useTexture) {
        RenderSystem.setShaderColor(r, g, b, a);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        if (useTexture) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            makeUVStars(buffer, matrices, minSize, maxSize, count, 61354);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionShader);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            makeStars(buffer, matrices, minSize, maxSize, count, 41315);
        }
        Tesselator.getInstance().end();
    }

    private void makeStars(BufferBuilder buffer, PoseStack poseStack, double minSize, double maxSize, int count, long seed) {
        RandomSource random = new LegacyRandomSource(seed);
        org.joml.Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < count; ++i) {
            double posX = random.nextDouble() * 2.0 - 1.0;
            double posY = random.nextDouble() * 2.0 - 1.0;
            double posZ = random.nextDouble() * 2.0 - 1.0;
            double size = MHelper.randRange(minSize, maxSize, random);
            double length = posX * posX + posY * posY + posZ * posZ;

            if (length < 1.0 && length > 0.001) {
                length = 1.0 / Math.sqrt(length);
                posX *= length;
                posY *= length;
                posZ *= length;

                double px = posX * 100.0;
                double py = posY * 100.0;
                double pz = posZ * 100.0;

                double angle = Math.atan2(posX, posZ);
                double sin1 = Math.sin(angle);
                double cos1 = Math.cos(angle);
                angle = Math.atan2(Math.sqrt(posX * posX + posZ * posZ), posY);
                double sin2 = Math.sin(angle);
                double cos2 = Math.cos(angle);
                angle = random.nextDouble() * Math.PI * 2.0;
                double sin3 = Math.sin(angle);
                double cos3 = Math.cos(angle);

                for (int index = 0; index < 4; ++index) {
                    double x = (double) ((index & 2) - 1) * size;
                    double y = (double) ((index + 1 & 2) - 1) * size;
                    double aa = x * cos3 - y * sin3;
                    double ab = y * cos3 + x * sin3;
                    double dy = aa * sin2 + 0.0 * cos2;
                    double ae = 0.0 * sin2 - aa * cos2;
                    double dx = ae * sin1 - ab * cos1;
                    double dz = ab * sin1 + ae * cos1;
                    buffer.vertex(matrix, (float)(px + dx), (float)(py + dy), (float)(pz + dz)).endVertex();
                }
            }
        }
    }

    private void makeUVStars(BufferBuilder buffer, PoseStack poseStack, double minSize, double maxSize, int count, long seed) {
        RandomSource random = new LegacyRandomSource(seed);
        org.joml.Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < count; ++i) {
            double posX = random.nextDouble() * 2.0 - 1.0;
            double posY = random.nextDouble() * 2.0 - 1.0;
            double posZ = random.nextDouble() * 2.0 - 1.0;
            double size = MHelper.randRange(minSize, maxSize, random);
            double length = posX * posX + posY * posY + posZ * posZ;

            if (length < 1.0 && length > 0.001) {
                length = 1.0 / Math.sqrt(length);
                posX *= length;
                posY *= length;
                posZ *= length;

                double px = posX * 100.0;
                double py = posY * 100.0;
                double pz = posZ * 100.0;

                double angle = Math.atan2(posX, posZ);
                double sin1 = Math.sin(angle);
                double cos1 = Math.cos(angle);
                angle = Math.atan2(Math.sqrt(posX * posX + posZ * posZ), posY);
                double sin2 = Math.sin(angle);
                double cos2 = Math.cos(angle);
                angle = random.nextDouble() * Math.PI * 2.0;
                double sin3 = Math.sin(angle);
                double cos3 = Math.cos(angle);

                float minV = random.nextInt(4) / 4F;
                for (int index = 0; index < 4; ++index) {
                    double x = (double) ((index & 2) - 1) * size;
                    double y = (double) ((index + 1 & 2) - 1) * size;
                    double aa = x * cos3 - y * sin3;
                    double ab = y * cos3 + x * sin3;
                    double dy = aa * sin2 + 0.0 * cos2;
                    double ae = 0.0 * sin2 - aa * cos2;
                    double dx = ae * sin1 - ab * cos1;
                    double dz = ab * sin1 + ae * cos1;
                    float texU = (index >> 1) & 1;
                    float texV = (((index + 1) >> 1) & 1) / 4F + minV;
                    buffer.vertex(matrix, (float)(px + dx), (float)(py + dy), (float)(pz + dz)).uv(texU, texV).endVertex();
                }
            }
        }
    }

    private void makeFarFog(BufferBuilder buffer, PoseStack poseStack, double minSize, double maxSize, int count, long seed) {
        RandomSource random = new LegacyRandomSource(seed);
        org.joml.Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < count; ++i) {
            double posX = random.nextDouble() * 2.0 - 1.0;
            double posY = random.nextDouble() - 0.5;
            double posZ = random.nextDouble() * 2.0 - 1.0;
            double size = MHelper.randRange(minSize, maxSize, random);
            double length = posX * posX + posY * posY + posZ * posZ;
            double distance = 2.0;

            if (length < 1.0 && length > 0.001) {
                length = distance / Math.sqrt(length);
                size *= distance;
                posX *= length;
                posY *= length;
                posZ *= length;

                double px = posX * 100.0;
                double py = posY * 100.0;
                double pz = posZ * 100.0;

                double angle = Math.atan2(posX, posZ);
                double sin1 = Math.sin(angle);
                double cos1 = Math.cos(angle);
                angle = Math.atan2(Math.sqrt(posX * posX + posZ * posZ), posY);
                double sin2 = Math.sin(angle);
                double cos2 = Math.cos(angle);
                angle = random.nextDouble() * Math.PI * 2.0;
                double sin3 = Math.sin(angle);
                double cos3 = Math.cos(angle);

                for (int index = 0; index < 4; ++index) {
                    double x = (double) ((index & 2) - 1) * size;
                    double y = (double) ((index + 1 & 2) - 1) * size;
                    double aa = x * cos3 - y * sin3;
                    double ab = y * cos3 + x * sin3;
                    double dy = aa * sin2 + 0.0 * cos2;
                    double ae = 0.0 * sin2 - aa * cos2;
                    double dx = ae * sin1 - ab * cos1;
                    double dz = ab * sin1 + ae * cos1;
                    float texU = (index >> 1) & 1;
                    float texV = ((index + 1) >> 1) & 1;
                    buffer.vertex(matrix, (float)(px + dx), (float)(py + dy), (float)(pz + dz)).uv(texU, texV).endVertex();
                }
            }
        }
    }

    private void makeCylinder(BufferBuilder buffer, PoseStack poseStack, int segments, double height, double radius) {
        for (int i = 0; i < segments; i++) {
            double a1 = (double) i * Math.PI * 2.0 / (double) segments;
            double a2 = (double) (i + 1) * Math.PI * 2.0 / (double) segments;
            double px1 = Math.sin(a1) * radius;
            double pz1 = Math.cos(a1) * radius;
            double px2 = Math.sin(a2) * radius;
            double pz2 = Math.cos(a2) * radius;

            float u0 = (float) i / (float) segments;
            float u1 = (float) (i + 1) / (float) segments;

            // 使用poseStack变换顶点
            org.joml.Matrix4f matrix = poseStack.last().pose();
            buffer.vertex(matrix, (float)px1, (float)-height, (float)pz1).uv(u0, 0).endVertex();
            buffer.vertex(matrix, (float)px1, (float)height, (float)pz1).uv(u0, 1).endVertex();
            buffer.vertex(matrix, (float)px2, (float)height, (float)pz2).uv(u1, 1).endVertex();
            buffer.vertex(matrix, (float)px2, (float)-height, (float)pz2).uv(u1, 0).endVertex();
        }
    }
}