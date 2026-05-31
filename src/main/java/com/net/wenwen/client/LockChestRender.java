package com.net.wenwen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.net.wenwen.block.LockChestBlock;
import com.net.wenwen.block.TileEntityChest;
import com.net.wenwen.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LockChestRender extends BlockEntityWithoutLevelRenderer {
    public static final LockChestRender INSTANCE = new LockChestRender();

    private TileEntityChest tile = new TileEntityChest(BlockPos.ZERO, Registration.CHEST.get().defaultBlockState());

    public LockChestRender() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext context, PoseStack stack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Block block = Block.byItem(itemStackIn.getItem());
        if (block instanceof LockChestBlock) {
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(this.tile, stack, bufferIn, combinedLightIn, combinedOverlayIn);
        } else {
            super.renderByItem(itemStackIn, context, stack, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }
}
