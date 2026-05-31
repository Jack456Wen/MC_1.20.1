package com.net.wenwen.entity;


import com.net.wenwen.arrrows.ExplossiveArrow;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "wenwen");


    public static final RegistryObject<EntityType<HostileBat>> HOSTILE_BAT = ENTITY_TYPES.register("hostilebat",
            () -> EntityType.Builder.of(HostileBat::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.9f) // 复用蝙蝠的大小
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("hostilebat"));

    public static final RegistryObject<EntityType<EntityOctorok>> OCTOROK = ENTITY_TYPES.register("octorok",
            () -> EntityType.Builder.of(EntityOctorok::new, MobCategory.WATER_CREATURE)
                    .sized(0.95f, 0.95f)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("octorok"));

    public static final RegistryObject<EntityType<EntityOctorokSnowball>> OCTOROK_SNOWBALL =
            ENTITY_TYPES.register("octorok_snowball",
                    () -> EntityType.Builder.<EntityOctorokSnowball>of(EntityOctorokSnowball::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("octorok_snowball"));

    public static final RegistryObject<EntityType<ExplossiveArrow>> EXPLOSIVE_ARROW =
            ENTITY_TYPES.register("explosive_arrow",
                    () -> EntityType.Builder.<ExplossiveArrow>of(ExplossiveArrow::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4) // 客户端追踪距离
                            .updateInterval(20)    // 更新频率
                            .build("explosive_arrow")
            );


    private static class EntityAttributesHandler {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(ModEntities.HOSTILE_BAT.get(), HostileBat.prepareAttributes());
        }
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        eventBus.addListener(ModEntities::onEntityAttributeCreation);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.HOSTILE_BAT.get(), HostileBat.prepareAttributes());
        event.put(ModEntities.OCTOROK.get(), EntityOctorok.prepareAttributes());
    }
}

