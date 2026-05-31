package com.net.wenwen.network;

import com.net.wenwen.inventory.InventorySorter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SortInventoryPacket {

    private final InventorySorter.SortType sortType;
    private final boolean isContainer; // true 表示整理容器，false 表示整理玩家背包

    public SortInventoryPacket(InventorySorter.SortType sortType, boolean isContainer) {
        this.sortType = sortType;
        this.isContainer = isContainer;
    }

    public SortInventoryPacket(FriendlyByteBuf buf) {
        this.sortType = InventorySorter.SortType.values()[buf.readInt()];
        this.isContainer = buf.readBoolean();
    }

    public static SortInventoryPacket decode(FriendlyByteBuf buf) {
        return new SortInventoryPacket(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.sortType.ordinal());
        buf.writeBoolean(this.isContainer);
    }

    public static void handle(SortInventoryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (msg.isContainer) {
                    // 整理容器物品
                    InventorySorter.sortContainer(player, msg.sortType);
                } else {
                    // 整理玩家背包
                    InventorySorter.sortInventory(player, msg.sortType);
                }
            }
        });
        context.setPacketHandled(true);
    }
}