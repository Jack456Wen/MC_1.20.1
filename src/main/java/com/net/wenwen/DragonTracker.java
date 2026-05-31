package com.net.wenwen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "wenwen")
public class DragonTracker {
    private static final ConcurrentHashMap<UUID, Entity> activeDragons = new ConcurrentHashMap<>();
    private static Class<?> entityDragonBaseClass;
    private static Method isModelDeadMethod;
    public static boolean isLoad=false;

    public static boolean GetactiveDragonsisNull()
    {
        return activeDragons.isEmpty();
    }

    public static void initReflection() {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById("iceandfire");

        try {
            ClassLoader targetClassLoader = modContainer.get().getMod().getClass().getClassLoader();
            entityDragonBaseClass = Class.forName("com.github.alexthe666.iceandfire.entity.EntityDragonBase", true, targetClassLoader);
            isModelDeadMethod = entityDragonBaseClass.getMethod("isModelDead");
            isLoad=true;

        } catch (Exception e) {

        }


    }


    public static boolean isDragon(Entity entity) {
        if (entityDragonBaseClass == null) {
            return false;
        }
        return entityDragonBaseClass.isInstance(entity);
    }

    // 当实体加入世界时触发
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (isDragon(entity)) {
            try {
                UUID dragonUUID = entity.getUUID();
                activeDragons.put(dragonUUID, entity);

            } catch (Exception e) {
                // 忽略反射调用异常
            }
        }

    }

    // 当实体离开世界时触发（包括死亡、卸载等）
    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (isDragon(entity)) {
            try {
                UUID dragonUUID = entity.getUUID();
                activeDragons.remove(dragonUUID);
            } catch (Exception e) {
                // 忽略反射调用异常
            }
        }
    }

    public static Entity findNearestDragon(Player player, double maxDistance) {
        var dragons = activeDragons.values();

        Optional<Entity> nearestDragon = dragons.stream()
                .filter(Entity::isAlive)
                .filter(dragon -> {
                    if(dragon instanceof TamableAnimal animal){
                        return (animal.getOwner()==null);
                    }
                    return false;
                })
                .filter(dragon -> {
                    try {
                        // 检查龙模型是否未死亡
                        return !(boolean) isModelDeadMethod.invoke(dragon);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(dragon -> dragon.level().equals(player.level()))
                .filter(dragon -> dragon.distanceToSqr(player) <= maxDistance * maxDistance)
                .min(Comparator.comparingDouble(dragon -> dragon.distanceToSqr(player)));

        return nearestDragon.orElse(null);
    }
}
