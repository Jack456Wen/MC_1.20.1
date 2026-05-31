package com.net.wenwen.curios;


import com.net.wenwen.init.WenwenModItems;
import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DarknessHeart extends Item implements ICurioItem {
    private static final String DARKNESS_TAG = "Wenwen:has_darkness_amulet";
    public DarknessHeart()  {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }
    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        list.add(Component.empty());
        super.appendHoverText(itemstack, level, list, flag);
        list.add(Component.literal("§e重伤:攻击目标时，对其施加持续30秒的【重伤】效果"));
        list.add(Component.empty());
        list.add(Component.literal("§b天谴:当受到致命伤害时，免疫该伤害，并剥夺其所有装备物品"));
        list.add(Component.empty());
        list.add(Component.literal("§d致命:攻击目标时，对其施加持续30秒的【斩杀】效果。期间，若目标生命值低于30%，则立即死亡"));
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
    public static void onLivingHurt(LivingHurtEvent event)
    {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        if (!player.getTags().contains(DARKNESS_TAG)) {
            return;
        }
        event.getEntity().addEffect(new MobEffectInstance(WenwenModMobEffects.DEHP_EFFECT.get(), 600, 4, false, true));
        event.getEntity().addEffect(new MobEffectInstance(WenwenModMobEffects.BeHead.get(), 600, 0, false, true));
    }
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!wearer.level().isClientSide) {
            // 装备时：添加标签
            wearer.getTags().add(DARKNESS_TAG);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!wearer.level().isClientSide) {
            // 卸载时：移除标签
            wearer.getTags().remove(DARKNESS_TAG);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if(!(attacker instanceof Player))
            return;
        if(attacker.getUUID()==event.getEntity().getUUID())
            return;
        if (!player.getTags().contains(DARKNESS_TAG)) {
            return;
        }
        event.setCanceled(true);
        event.getEntity().setHealth(1);
        Clear(attacker);
    }

    private static void Clear(LivingEntity entity)
    {
        if(entity instanceof ServerPlayer player){
            // 清空玩家的物品栏
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.items.size(); i++) {
                inventory.items.set(i, ItemStack.EMPTY);
            }
            for (int i = 0; i < inventory.armor.size(); i++) {
                inventory.armor.set(i, ItemStack.EMPTY);
            }
            inventory.offhand.set(0, ItemStack.EMPTY);
            clearAllEquippedCurios(player);
        }
        else {
            // 清空生物的物品栏
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                    entity.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }
    public static void clearAllEquippedCurios(Player player) {
        // 1. 获取玩家的饰品库存处理器
        // 这是一个 Optional，需要用 ifPresent 来安全处理
        CuriosApi.getCuriosInventory(player).ifPresent(curiosHandler -> {

            // 2. 获取所有饰品类型的处理器 Map<String, ICurioStacksHandler>
            // String 是饰品类型ID (e.g., "necklace", "ring")
            // ICurioStacksHandler 是该类型饰品槽的管理器
            Map<String, ICurioStacksHandler> curioMap = curiosHandler.getCurios();

            // 3. 遍历每一种饰品类型
            for (ICurioStacksHandler stacksHandler : curioMap.values()) {

                // 4. 获取该类型的功能性物品槽处理器
                IDynamicStackHandler itemStacks = stacksHandler.getStacks();

                // 5. 获取该类型饰品槽位的数量
                int slotCount = stacksHandler.getSlots();

                // 6. 循环遍历所有槽位并设置为空
                for (int i = 0; i < slotCount; i++) {
                    itemStacks.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        });
    }


}
