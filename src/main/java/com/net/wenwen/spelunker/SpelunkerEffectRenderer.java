package com.net.wenwen.spelunker;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SpelunkerEffectRenderer {

    private final ConcurrentMap<BlockPos, ChunkOres> chunkSections = new ConcurrentHashMap<>();
    private boolean active = false;

    public void render(PoseStack matrices, net.minecraft.client.Camera camera) {
        if (!active) {
            return;
        }

        Vec3 pos = camera.getPosition();
        matrices.pushPose();
        matrices.translate(-pos.x, -pos.y, -pos.z);

        // 【关键修改1】：不再传入 vertexConsumers，改用原生的 MultiBufferSource
        renderAllChunks(matrices, Minecraft.getInstance().level);

        matrices.popPose();
    }

    public boolean setActive(boolean value) {
        boolean init = value && !active;
        this.active = value;
        return init;
    }

    public boolean isActive() {
        return active;
    }

    public void clear() {
        chunkSections.clear();
    }

    public void updateChunks(Level world, java.util.Collection<BlockPos> remove, java.util.Collection<ChunkOres> add) {
        for (BlockPos v : remove)
            chunkSections.remove(v);
        for (ChunkOres chunk : add) {
            ChunkPos chunkPos = chunk.getPos();
            BlockPos pos = new BlockPos(chunkPos.getMinBlockX(), chunk.sectionY, chunkPos.getMinBlockZ());
            if (!chunk.isRemapped()) {
                chunk.remapToBlockCoordinates(chunk.sectionY);
            }
            chunkSections.put(pos, chunk);
        }
    }

    public void removeChunk(BlockPos pos) {
        chunkSections.remove(pos);
    }

    public ChunkOres get(BlockPos pos) {
        return chunkSections.get(pos);
    }

    public void addChunks(int bottomSectionCord, java.util.Collection<ChunkOres> chunks) {
        for (ChunkOres chunk : chunks) {
            ChunkPos chunkPos = chunk.getPos();
            BlockPos pos = new BlockPos(chunkPos.getMinBlockX(), chunk.sectionY, chunkPos.getMinBlockZ());
            chunkSections.put(pos, chunk.remapToBlockCoordinates(chunk.sectionY));
        }
    }

    private static final int MAX_RENDER_ORES = 200;
    private static final RenderType LINES_LAYER = RenderType.lines();

    private void renderAllChunks(PoseStack matrices, Level world) {
        if (world == null) return;

        double playerX = Minecraft.getInstance().player.getX();
        double playerY = Minecraft.getInstance().player.getY();
        double playerZ = Minecraft.getInstance().player.getZ();

        java.util.Map<Integer, java.util.List<java.util.Map.Entry<BlockPos, Float>>> colorGroups = new java.util.HashMap<>();
        int renderedOres = 0;

        for (Map.Entry<BlockPos, ChunkOres> chunkSections : chunkSections.entrySet()) {
            ChunkOres chunk = chunkSections.getValue();
            for (Map.Entry<BlockPos, SpelunkerBlockConfig> ore : chunk.entrySet()) {
                if (renderedOres >= MAX_RENDER_ORES) break;

                BlockPos pos = ore.getKey();
                SpelunkerBlockConfig block = ore.getValue();

                double squareDistance = toSquaredDistanceFromCenter(pos, playerX, playerY, playerZ);
                if (squareDistance > block.getBlockRadiusMax()) continue;

                float fade;
                if (block.isTransition()) {
                    fade = Math.min(1 - (float) ((squareDistance - block.getBlockRadiusMin()) / (block.getBlockRadiusMax() - block.getBlockRadiusMin())), 1);
                    fade = easeOutCirc(fade);
                } else {
                    fade = 1;
                }

                int color = block.getColor();
                if (!colorGroups.containsKey(color)) {
                    colorGroups.put(color, new java.util.ArrayList<>());
                }
                colorGroups.get(color).add(new java.util.AbstractMap.SimpleEntry<>(pos, fade));
                renderedOres++;
            }
            if (renderedOres >= MAX_RENDER_ORES) break;
        }

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        try {
            for (java.util.Map.Entry<Integer, java.util.List<java.util.Map.Entry<BlockPos, Float>>> group : colorGroups.entrySet()) {
                int color = group.getKey();
                float red = ((color >> 16) & 0xFF) / 255.0f;
                float green = ((color >> 8) & 0xFF) / 255.0f;
                float blue = (color & 0xFF) / 255.0f;

                VertexConsumer consumer = bufferSource.getBuffer(LINES_LAYER);

                for (java.util.Map.Entry<BlockPos, Float> entry : group.getValue()) {
                    BlockPos pos = entry.getKey();
                    float fade = entry.getValue();

                    matrices.pushPose();
                    matrices.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    matrices.scale(fade, fade, fade);

                    compileWireframe(matrices.last(), consumer, 1.0f, red, green, blue);

                    matrices.popPose();
                }
            }

            // ==========================================
            // 【终极破防写法】：在真正刷入显卡的前一毫秒，
            // 绕过 RenderSystem，直接用 LWJGL 底层命令篡改显卡状态
            // ==========================================
            RenderSystem.disableDepthTest();
            // GL_ALWAYS 的意思是：无论深度如何，全部通过（实现透视）
            org.lwjgl.opengl.GL11.glDepthFunc(org.lwjgl.opengl.GL11.GL_ALWAYS);

            bufferSource.endBatch(LINES_LAYER);

        } finally {
            // 画完之后，必须立刻把状态洗白，恢复原版逻辑，否则整个世界都会穿模错乱
            org.lwjgl.opengl.GL11.glDepthFunc(org.lwjgl.opengl.GL11.GL_LEQUAL);
            RenderSystem.enableDepthTest();
        }
    }




    private void compileWireframe(PoseStack.Pose pose, VertexConsumer consumer, float size, float red, float green, float blue) {
        float half = size / 2.0f;
        float x0 = -half, y0 = -half, z0 = -half;
        float x1 = half,  y1 = half,  z1 = half;

        // 底面
        addVertexLine(pose, consumer, x0, y0, z0, x1, y0, z0, red, green, blue);
        addVertexLine(pose, consumer, x1, y0, z0, x1, y0, z1, red, green, blue);
        addVertexLine(pose, consumer, x1, y0, z1, x0, y0, z1, red, green, blue);
        addVertexLine(pose, consumer, x0, y0, z1, x0, y0, z0, red, green, blue);
        // 顶面
        addVertexLine(pose, consumer, x0, y1, z0, x1, y1, z0, red, green, blue);
        addVertexLine(pose, consumer, x1, y1, z0, x1, y1, z1, red, green, blue);
        addVertexLine(pose, consumer, x1, y1, z1, x0, y1, z1, red, green, blue);
        addVertexLine(pose, consumer, x0, y1, z1, x0, y1, z0, red, green, blue);
        // 竖边
        addVertexLine(pose, consumer, x0, y0, z0, x0, y1, z0, red, green, blue);
        addVertexLine(pose, consumer, x1, y0, z0, x1, y1, z0, red, green, blue);
        addVertexLine(pose, consumer, x1, y0, z1, x1, y1, z1, red, green, blue);
        addVertexLine(pose, consumer, x0, y0, z1, x0, y1, z1, red, green, blue);
    }

    private void addVertexLine(PoseStack.Pose pose, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue) {
        // 【关键修改5】：直接将颜色写死在顶点里，不再依赖 Outline 的全局变色
        consumer.vertex(pose.pose(), x1, y1, z1).color(red, green, blue, 1.0f).normal(pose.normal(), 1.0f, 0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
        consumer.vertex(pose.pose(), x2, y2, z2).color(red, green, blue, 1.0f).normal(pose.normal(), 1.0f, 0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
    }

    private static float easeOutCirc(float x) {
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    private static double toSquaredDistanceFromCenter(BlockPos pos, double x, double y, double z) {
        double d = (double) pos.getX() + 0.5D - x;
        double e = (double) pos.getY() + 0.5D - y;
        double f = (double) pos.getZ() + 0.5D - z;
        return d * d + e * e + f * f;
    }
}
