package com.net.wenwen.curios;

import com.net.wenwen.WenwenMod;
import com.net.wenwen.damage.ModDamageSources;
import com.net.wenwen.init.WenwenModItems;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.network.PlayMusicPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeartAmulet extends Item implements ICurioItem {
    private static final String HeartAmulet_TAG = "Wenwen:has_HeartAmulet";
    public HeartAmulet()  {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }
    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);
        list.add(Component.empty());
        list.add(Component.literal("§d铸镜:获得【免疫远程】效果"));
        list.add(Component.empty());
        list.add(Component.literal("§b镜面反射:将自身受到一半的伤害返还给目标"));
        list.add(Component.literal("§b魔镜守护:自身受到致命伤害时，免疫该次伤害并立即回到出生点"));
        list.add(Component.empty());
        list.add(Component.literal("§e当触发魔镜守护时，立即获得10秒【免疫】效果"));
        list.add(Component.literal("§e免疫效果:你将永远不死，获得免疫一切伤害的能力"));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide()) {
            LivingEntity entity = slotContext.entity();
            if (!entity.hasEffect(WenwenModMobEffects.RANGE.get())) {
                entity.addEffect(new MobEffectInstance(WenwenModMobEffects.RANGE.get(), 9999999, 0, true, true, true));
            }
        }
    }
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!wearer.level().isClientSide) {
            // 装备时：添加标签
            wearer.getTags().add(HeartAmulet_TAG);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!wearer.level().isClientSide) {
            // 卸载时：移除标签
            wearer.getTags().remove(HeartAmulet_TAG);
        }
    }

    // 其他可选方法
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true; // 允许右键装备
    }
    @Override
    public ICurio.@NotNull SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
    }
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!player.getTags().contains(HeartAmulet_TAG)) {
            return;
        }
        event.setAmount(event.getAmount()*0.5f);
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        ModDamageSources damageSources = new ModDamageSources(player.level());
        DamageSource trueDamage = damageSources.trueDamage(player);
        attacker.hurt(trueDamage,event.getAmount()*0.5f);
        player.heal(2);
    }
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.getTags().contains(HeartAmulet_TAG)) {
            return;
        }
        ItemStack equippedAmuletStack = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.is(WenwenModItems.HEART_AMULET.get())))
                .map(slotResult -> slotResult.stack())
                .orElse(ItemStack.EMPTY);
        if(equippedAmuletStack.isEmpty()){
            return;
        }
        if(player.getCooldowns().isOnCooldown(equippedAmuletStack.getItem())){
            return;
        }
        event.setCanceled(true);
        event.getEntity().setHealth(1);
        player.getCooldowns().addCooldown(equippedAmuletStack.getItem(), 1200);
        Move(player);
    }
    private static void Move(Player player)
    {
        if (!player.level().isClientSide()) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            BlockPos blockPos = serverPlayer.getRespawnPosition();
            if (blockPos == null) {
                blockPos=serverPlayer.level().getSharedSpawnPos();
            }
            player.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), 200, 0, true, true, true));
            serverPlayer.teleportTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            PlayMusicPacket packet = new PlayMusicPacket(999);
            WenwenMod.CHANNEL.sendTo(packet, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

}
