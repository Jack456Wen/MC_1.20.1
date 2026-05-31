package com.net.wenwen;

import com.net.wenwen.init.ModEnchantments;
import com.net.wenwen.init.Registration;
import com.net.wenwen.init.WenwenModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber
public class LootTableModifier {
    private static final ResourceLocation LOOT_TABLE = new ResourceLocation("minecraft", "entities/warden");
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {

        ResourceLocation name = event.getName();

        // 获取命名空间 (例如 "minecraft") 和 路径 (例如 "entities/warden")
        String namespace = name.getNamespace();
        String path = name.getPath();

        if (path.startsWith("chests/")) {

            LootTable table = event.getTable();

            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .name("wenwen_pool")
                    .setRolls(ConstantValue.exactly(Config.loot_rools)) // 掉落数量
                    .add(LootItem.lootTableItem(WenwenModItems.ENMELON.get()).setWeight(5)) // 掉落物品
                    .add(LootItem.lootTableItem(WenwenModItems.HEART_PIECE.get()).setWeight(10)) // 掉落物品
                    .add(LootItem.lootTableItem(WenwenModItems.GOLD_EN_POTATO.get()).setWeight(5)) // 掉落物品
                    .add(LootItem.lootTableItem(WenwenModItems.GOLD_ENARADISH.get()).setWeight(5)) // 掉落物品
                    .add(LootItem.lootTableItem(WenwenModItems.GOLD_POTATO.get()).setWeight(40)) // 掉落物品
                    .add(LootItem.lootTableItem(WenwenModItems.MIRROR.get()).setWeight(1)) // 掉落物品
                    .add(LootItem.lootTableItem(WenwenModItems.Break.get()).setWeight(2))
                    .add(LootItem.lootTableItem(Registration.TRAP_CAGE.get()).setWeight(2))
                    .add(LootItem.lootTableItem(WenwenModItems.DIAMOND_POTATO.get()).setWeight(1))
                    .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(60))
                    .add(LootItem.lootTableItem(WenwenModItems.Tears.get()).setWeight(2))
                    .add(LootItem.lootTableItem(WenwenModItems.DIAMOND_CARROT.get()).setWeight(1))
                    .when(LootItemRandomChanceCondition.randomChance((float)Config.loot_chance)); // 掉落概率

            // 将战利品池添加到战利品表中
            table.addPool(poolBuilder.build());
        }

        if (namespace.equals("minecraft") && path.equals("entities/warden")) {

            LootTable table = event.getTable();

            // --- 你的战利品池构建代码 ---

            // 池子 1: Heart Container (2%)
            LootPool.Builder pool1 = LootPool.lootPool()
                    .name("wenwen:heart_container")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(WenwenModItems.HEARTCONTAINER.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.02f));

            // 池子 2: Heart Piece (1-2 个)
            LootPool.Builder pool2 = LootPool.lootPool()
                    .name("wenwen:heart_piece")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(WenwenModItems.HEART_PIECE.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));

            // 池子 3: Break (25%)
            LootPool.Builder pool3 = LootPool.lootPool()
                    .name("wenwen:break")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(WenwenModItems.Break.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.25f));

            // 池子 4: 黑暗之心 (0.01%)
            LootPool.Builder pool4 = LootPool.lootPool()
                    .name("wenwen:darkness_heart")
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(WenwenModItems.HEART_AMULET.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.0001f));

            // --- 添加到表中 ---
            table.addPool(pool1.build());
            table.addPool(pool2.build());
            table.addPool(pool3.build());
            table.addPool(pool4.build());
        }
    }


    public static LootPool fetchLootPool(String location) {
        return fetchLootPool(WenwenMod.MODID + ":", location);
    }

    public static LootPool fetchLootPool(String namespace, String location) {

        LootPoolEntryContainer.Builder<?> entry = LootTableReference.lootTableReference(new ResourceLocation(namespace + location)).setQuality(1);
        return LootPool.lootPool().add(entry).name("wenwen_inject").build();
    }
}
