package com.net.wenwen.client;

import com.net.wenwen.WenwenMod;
import com.net.wenwen.block.TileEntityChest;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.ChestType;

public class TileEntityChestRenderer extends ChestRenderer<TileEntityChest> {

    public static Material single = getChestMaterial("stone");
    public static Material left = getChestMaterial("stone_left");
    public static Material right = getChestMaterial("stone_right");

    public TileEntityChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Material getMaterial(TileEntityChest blockEntity, ChestType chestType) {
        return getChestMaterial(blockEntity, chestType);
    }

    private static Material getChestMaterial(String path) {
        return new Material(Sheets.CHEST_SHEET, new ResourceLocation(WenwenMod.MODID, "entity/chest/" + path));
    }

    private static Material getChestMaterial(TileEntityChest tile, ChestType type) {
        switch(type) {
            case LEFT:
                return left;
            case RIGHT:
                return right;
            case SINGLE:
            default:
                return single;
        }
    }
}
