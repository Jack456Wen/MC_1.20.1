package com.net.wenwen.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<ItemStack> event) {
        // 为所有的 ItemStack 提供我们的 Capability
        // 注意：这里我们用 Lazy 懒加载，只有在真正调用时才会实例化，节省性能
        event.addCapability(
                new ResourceLocation("wenwen", "boss_kills"),
                new ICapabilityProvider() {
                    private KillsCapability cap;

                    @Nonnull
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                        if (cap == KillsCapability.BOSS_KILLS) {
                            // 懒初始化：第一次被访问时才创建对象
                            if (this.cap == null) {
                                this.cap = new KillsCapability(event.getObject());
                            }
                            return LazyOptional.of(() -> this.cap).cast();
                        }
                        return LazyOptional.empty();
                    }
                }
        );
    }
}
