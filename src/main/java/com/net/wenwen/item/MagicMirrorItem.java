package com.net.wenwen.item;

import com.net.wenwen.Config;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MagicMirrorItem extends Item {
    public MagicMirrorItem()  {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).durability(100));
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResultHolder<ItemStack> resultHolder = super.use(level, player, hand);
        //动画
        if (level.isClientSide) Minecraft.getInstance().gameRenderer.displayItemActivation(resultHolder.getObject().copy());
        level.playLocalSound(player.getBlockX(), player.getBlockY(), player.getBlockZ(), WenwenModSounds.Mirror.get(), SoundSource.PLAYERS, 1, 1, false);
        player.getCooldowns().addCooldown(resultHolder.getObject().getItem(), 100);
        player.swing(hand);
        //移动
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            BlockPos blockPos = serverPlayer.getRespawnPosition();
            if(player.experienceLevel<2){
                serverPlayer.displayClientMessage(Component.translatable("message.wenwen.XP"), true);
                return InteractionResultHolder.consume(itemStack);
            }
            if (blockPos == null) {
                serverPlayer.displayClientMessage(Component.translatable("message.wenwen.cannotFindRespawnPosition"), true);
                return InteractionResultHolder.consume(itemStack);
            }
            serverPlayer.setExperienceLevels(serverPlayer.experienceLevel-2);
            ServerLevel serverLevel = level.getServer().getLevel(serverPlayer.getRespawnDimension());
            ServerLevel serverLevel1 = serverLevel != null ? serverLevel : level.getServer().overworld();
            if (level != serverLevel1) {
                player.changeDimension(serverLevel1, new ITeleporter() {
                    @Override
                    public boolean isVanilla() {
                        return false;
                    }
                });
            }
            serverPlayer.teleportTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if(Math.random()<0.1){
                itemStack.shrink(1);
                SoundEvent soundEvent = WenwenModSounds.MirrorBREAK.get();
                if (soundEvent != null) {
                    serverPlayer.level().playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                //-----------
                ResourceLocation advancementId = new ResourceLocation("wenwen", "magic_mirror_1");
                Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(advancementId);
                if (advancement != null) {
                    AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                    if (!progress.isDone()) {
                        serverPlayer.getAdvancements().award(advancement, "magic_mirror_1");
                    }
                }
            }

        }
        return resultHolder;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag iTooltipFlag) {
        super.appendHoverText(itemStack, world, list, iTooltipFlag);
        list.add(Component.translatable("explain.wenwen.magic_mirror").withStyle(ChatFormatting.AQUA));
        if(Config.mirror_ability){
            list.add(Component.empty());
            list.add(Component.translatable("explain.wenwen.magic_mirror_1"));
            list.add(Component.empty());
            list.add(Component.literal("\u00A7b镜像:在物品栏时获得镜像效果，受到的伤害超过50%最大生命值时将一半的伤害传递给魔镜（魔镜会受到损伤）"));
            list.add(Component.empty());
            list.add(Component.literal("\u00A7e碎裂:魔镜破碎后获得5秒无敌效果"));
        }
        list.add(Component.empty());
        list.add(Component.literal("\u00A7e使用魔镜时有小概率破碎！"));
    }
    @Override
    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
        if(slot<=8 && Config.mirror_ability){
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                if (!_entity.hasEffect(WenwenModMobEffects.MIRROR_EFFECT.get())) {
                    _entity.addEffect(new MobEffectInstance(WenwenModMobEffects.MIRROR_EFFECT.get(), 1200, 0, false, false));
                }
        }
    }
}
