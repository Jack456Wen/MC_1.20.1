package com.net.wenwen.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.net.wenwen.WenwenMod;
import com.net.wenwen.inventory.InventorySorter;
import com.net.wenwen.network.SortInventoryPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;



@Mod.EventBusSubscriber(modid = WenwenMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InventorySortButtonHandler {

    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(WenwenMod.MODID, "textures/gui/button.png");
    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 18;
    private static final int TEXTURE_WIDTH = 20;
    private static final int TEXTURE_HEIGHT = 37;
    private static final int NORMAL_V = 0;
    private static final int HOVERED_V = 19;

    // 玩家背包整理按钮的位置
    private static int buttonX;
    private static int buttonY;
    private static boolean isHovered = false;
    
    // 箱子整理按钮的位置
    private static int chestButtonX;
    private static int chestButtonY;
    private static boolean isChestButtonHovered = false;

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen)) {
            // 重置悬停状态
            isHovered = false;
            isChestButtonHovered = false;
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        
        int actualWidth = BUTTON_WIDTH / 2;
        int actualHeight = BUTTON_HEIGHT / 2;
        
        // 检查是否是箱子等容器类型
        AbstractContainerMenu containerMenu = screen.getMenu();
        boolean isChestContainer = false;

        if (containerMenu != null) {
            String containerClassName = containerMenu.getClass().getName();
            isChestContainer = containerClassName.contains("Chest") || containerClassName.contains("Shulker") || containerClassName.contains("Barrel") || containerClassName.contains("AbstractHandler");
        }

        // 重置悬停状态
        isHovered = false;
        isChestButtonHovered = false;

        // 渲染玩家背包的整理按钮
        if (screen instanceof InventoryScreen || isChestContainer || containerMenu != null) {
            // 玩家背包：保持原来的位置
            buttonX = screen.getGuiLeft() + screen.getXSize() - 20;
            buttonY = screen.getGuiTop() + (screen.getYSize() - 95);
            isHovered = mouseX >= buttonX && mouseX < buttonX + actualWidth &&
                         mouseY >= buttonY && mouseY < buttonY + actualHeight;

            RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            
            int v = isHovered ? HOVERED_V : NORMAL_V;
            
            PoseStack poseStack = event.getGuiGraphics().pose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 1.0F);
            poseStack.translate(buttonX * 2, buttonY * 2, 0);
            
            event.getGuiGraphics().blit(
                BUTTON_TEXTURE,
                0,
                0,
                0,
                v,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
            );
            
            poseStack.popPose();
            
            if (isHovered) {
                renderTooltip(event, mouseX, mouseY, "整理背包");
            }
        }

        // 渲染箱子的整理按钮
        if (isChestContainer) {
            // 箱子等容器：使用新位置
            chestButtonX = screen.getGuiLeft() + screen.getXSize() - 20;
            chestButtonY = screen.getGuiTop() + 6;
            isChestButtonHovered = mouseX >= chestButtonX && mouseX < chestButtonX + actualWidth &&
                                   mouseY >= chestButtonY && mouseY < chestButtonY + actualHeight;

            RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            
            int v = isChestButtonHovered ? HOVERED_V : NORMAL_V;
            
            PoseStack poseStack = event.getGuiGraphics().pose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 1.0F);
            poseStack.translate(chestButtonX * 2, chestButtonY * 2, 0);
            
            event.getGuiGraphics().blit(
                BUTTON_TEXTURE,
                0,
                0,
                0,
                v,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
            );
            
            poseStack.popPose();
            
            if (isChestButtonHovered) {
                renderTooltip(event, mouseX, mouseY, "整理箱子");
            }
        }
    }

    private static void renderTooltip(ScreenEvent.Render.Post event, double mouseX, double mouseY, String text) {
        Component tooltipText = Component.literal(text).withStyle(ChatFormatting.WHITE);
        event.getGuiGraphics().renderTooltip(
            Minecraft.getInstance().font,
            tooltipText,
            (int) mouseX,
            (int) mouseY
        );
    }

    @SubscribeEvent
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen)) {
            // 重置悬停状态
            isHovered = false;
            isChestButtonHovered = false;
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
        // 检查是否是箱子等容器类型
        net.minecraft.world.inventory.AbstractContainerMenu containerMenu = screen.getMenu();
        boolean isChestContainer = false;

        if (containerMenu != null) {
            String containerClassName = containerMenu.getClass().getName();
            isChestContainer = containerClassName.contains("Chest") || containerClassName.contains("Shulker") || containerClassName.contains("Barrel") || containerClassName.contains("AbstractHandler");
        }

        // 响应玩家背包整理按钮的点击
        if (isHovered) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.5F, 1.0F);
                // 整理玩家背包
                WenwenMod.CHANNEL.sendToServer(new SortInventoryPacket(InventorySorter.SortType.NAME, false));
            }
            event.setCanceled(true);
        }

        // 响应箱子整理按钮的点击
        if (isChestContainer && isChestButtonHovered) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.5F, 1.0F);
                // 整理箱子
                WenwenMod.CHANNEL.sendToServer(new SortInventoryPacket(InventorySorter.SortType.NAME, true));
            }
            event.setCanceled(true);
        }
    }
}