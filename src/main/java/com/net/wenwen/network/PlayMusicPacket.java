package com.net.wenwen.network;

import com.net.wenwen.PlayMusicPacketClientHandler;
import com.net.wenwen.init.WenwenModItems;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayMusicPacket {

    public final int musicId; // 0 代表第一首, 1 代表第二首

    // 1. 构造函数：用于创建要发送的消息
    public PlayMusicPacket(int musicId) {
        this.musicId = musicId;
    }

    // 2. 解码器：从网络数据中读取信息，创建消息对象
    public static PlayMusicPacket decode(FriendlyByteBuf buf) {
        return new PlayMusicPacket(buf.readInt());
    }

    // 3. 编码器：将消息对象写入网络数据
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.musicId);
    }

    // 4. 处理器：当消息被接收时执行
    public static void handle(PlayMusicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        // 将处理逻辑排队到主游戏线程执行，这对于任何与游戏状态交互的操作都是必需的
        context.enqueueWork(() -> {
            // 使用 DistExecutor 确保只在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PlayMusicPacketClientHandler.handle(msg);
            });
        });
        // 标记这个包已经被处理
        context.setPacketHandled(true);
    }

}
