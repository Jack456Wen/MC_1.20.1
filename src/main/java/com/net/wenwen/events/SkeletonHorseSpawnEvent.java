package com.net.wenwen.events;

import com.net.wenwen.Config;
import com.net.wenwen.WenwenMod;
import com.net.wenwen.capabilities.KillsCapability;
import com.net.wenwen.common.WorldStateManager;
import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class SkeletonHorseSpawnEvent {

    private static final boolean ONLY_SURFACE = true;        // 只在地表生成// 白天是否燃烧
    // ============================================

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        int level= WorldStateManager.isWorldUp(event.getLevel()) ? 2 : 1;
        //僵尸骑士
        if ((event.getEntity() instanceof Zombie zombie))
        {
            if(zombie.getType()!=EntityType.ZOMBIE){
                return;
            }
            if (zombie.getRandom().nextDouble() >= Config.skeleton_chance*level) return;
            if(zombie.isBaby()){
                return;
            }
            ZombieHorse horse = EntityType.ZOMBIE_HORSE .create(event.getLevel());
            if (horse == null) return;
            horse.moveTo(
                    zombie.getX(),
                    zombie.getY(),
                    zombie.getZ(),
                    zombie.getYRot(),
                    zombie.getXRot()
            );
            var horseSpeed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
            if (horseSpeed != null) {
                horseSpeed.setBaseValue(0.4);
            }
            event.getLevel().addFreshEntity(horse);
            zombie.startRiding(horse, true);
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));

        }
        if ((event.getEntity() instanceof Skeleton skeleton))
        {
            if (skeleton.getRandom().nextDouble() >= (0.95f-(level*0.05f))){
                var Speed = skeleton.getAttribute(Attributes.MOVEMENT_SPEED);
                if (Speed != null) {
                    Speed.setBaseValue(0.3);
                }
                skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                return;
            }
            if (skeleton.getRandom().nextDouble() <= Config.skeleton_chance*level) {
                if (ONLY_SURFACE) {
                    BlockPos spawnPos = skeleton.blockPosition();
                    boolean isSurface = true;
                    for (int i = 1; i <= 3; i++) {
                        if (event.getLevel().getBlockState(spawnPos.above(i))
                                .isSolidRender(event.getLevel(), spawnPos.above(i))) {
                            isSurface = false;
                            break;
                        }
                    }

                    if (!isSurface) return;
                }
                //生成骨马
                SkeletonHorse horse = EntityType.SKELETON_HORSE.create((ServerLevel) event.getLevel());
                if (horse == null) return;
                horse.moveTo(
                        skeleton.getX(),
                        skeleton.getY(),
                        skeleton.getZ(),
                        skeleton.getYRot(),
                        skeleton.getXRot()
                );
                var horseSpeed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
                if (horseSpeed != null) {
                    horseSpeed.setBaseValue(0.4);
                }
                skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                skeleton.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));

                //把马加入世界
                event.getLevel().addFreshEntity(horse);

                //强制骷髅骑上马
                skeleton.startRiding(horse, true);
            }
        }

    }


    @SubscribeEvent
    public static void onItemAttributeModification(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        // 1. 判断是否有附魔
        if (stack.getEnchantmentLevel(ModEnchantments.Boss.get()) <= 0) return;

        // 2. 从 Capability 读取击杀数
        int kills = stack.getCapability(KillsCapability.BOSS_KILLS)
                .map(cap -> cap.getKills())
                .orElse(0); // 如果没有 Capability，默认返回 0

        if (kills <= 0) return;

        // 3. 核心：必须判断槽位！只在主手时添加
        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    UUID.fromString("d3b7e8c9-10a1-4b2f-8c9d-4e5f6a7b8c9d"),
                    "boss_kills",
                    kills,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }



}

