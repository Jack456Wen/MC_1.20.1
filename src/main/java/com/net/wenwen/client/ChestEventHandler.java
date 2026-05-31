package com.net.wenwen.client;

import com.net.wenwen.init.Registration;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "wenwen", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void doClientStuff(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(Registration.CHEST_TILE_TYPE.get(), TileEntityChestRenderer::new);
    }
}
