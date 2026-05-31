package com.net.wenwen;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "wenwen")
public class EntityTracker {
    private static final ConcurrentHashMap<UUID, Warden> nearestWardenMap = new ConcurrentHashMap<>();
    //private static final ConcurrentHashMap<UUID, EntityDragonBase> activeDragons = new ConcurrentHashMap<>();

    public static boolean GetEntityListisEmpty()
    {
        return nearestWardenMap.isEmpty();
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Warden warden) {
            UUID WardenUUID = warden.getUUID();
            nearestWardenMap.put(WardenUUID, warden);
        }
        if(!Config.Animal_ABILITY.get()){
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Cow || entity instanceof Sheep || entity instanceof Chicken || entity instanceof Horse || entity instanceof Pig || entity instanceof Cat) {
            makeAnimalRetaliate((PathfinderMob) entity);
        }
    }

    private static void makeAnimalRetaliate(PathfinderMob mob) {
        for (Goal goal : mob.goalSelector.getAvailableGoals().toArray(new Goal[0])) {
            if (goal instanceof PanicGoal) {
                mob.goalSelector.removeGoal(goal);
                break;
            }
        }
        AttributeInstance attackDamageAttribute = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            if(mob instanceof Cow){
                attackDamageAttribute.setBaseValue(6);
            }
            else if(mob instanceof Cat){
                attackDamageAttribute.setBaseValue(5);
            }
            else if(mob instanceof Horse){
                attackDamageAttribute.setBaseValue(8);
            }
            else{
                attackDamageAttribute.setBaseValue(4);
            }

        }

        mob.targetSelector.addGoal(0, new HurtByTargetGoal(mob, mob.getClass()));
        mob.goalSelector.addGoal(0, new MeleeAttackGoal(mob, 1.2D, true));
    }

    // 当实体离开世界时触发（包括死亡、卸载等）
    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Warden warden) {
            UUID WardenUUID = warden.getUUID();
            nearestWardenMap.remove(WardenUUID);
        }
    }


    public static Warden findNearestWarden(Player player, double maxDistance) {
        // 这个操作非常快，只是获取一个Map的引用
        var wardens = nearestWardenMap.values();

        // 2. 使用Java Stream API进行高效筛选和查找
        Optional<Warden> nearestWarden = wardens.stream()
                // 筛选1：龙还活着（虽然缓存管理好了，但双重检查更安全）
                .filter(Entity::isAlive)
                // 筛选2：龙在同一个维度
                .filter(warden -> warden.level().equals(player.level()))
                // 筛选3：龙在最大距离内（使用距离的平方来避免开方运算，性能更高）
                .filter(warden -> warden.distanceToSqr(player) <= maxDistance * maxDistance)
                // 找出距离最近的那一个
                .min(Comparator.comparingDouble(warden -> warden.distanceToSqr(player)));
        return nearestWarden.orElse(null);
    }


    /*public static EntityDragonBase findNearestDragon(Player player, double maxDistance) {
        // 1. 从我们的缓存中获取所有龙的集合
        // 这个操作非常快，只是获取一个Map的引用
        var dragons = activeDragons.values();

        // 2. 使用Java Stream API进行高效筛选和查找
        Optional<EntityDragonBase> nearestDragon = dragons.stream()
                // 筛选1：龙还活着（虽然缓存管理好了，但双重检查更安全）
                .filter(Entity::isAlive)
                .filter(dragon -> dragon.getOwnerUUID()==null)
                .filter(dragon -> !dragon.isModelDead())
                // 筛选2：龙在同一个维度
                .filter(dragon -> dragon.level().equals(player.level()))
                // 筛选3：龙在最大距离内（使用距离的平方来避免开方运算，性能更高）
                .filter(dragon -> dragon.distanceToSqr(player) <= maxDistance * maxDistance)
                // 找出距离最近的那一个
                .min(Comparator.comparingDouble(dragon -> dragon.distanceToSqr(player)));

        return nearestDragon.orElse(null);
    }*/

}
