package com.net.wenwen.enchantment;
import com.net.wenwen.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FortuneEnchantment extends Enchantment {
    private static final Set<Block> ORE_BLOCKS = Set.of(
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE,
            Blocks.ANCIENT_DEBRIS
    );

    public FortuneEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.create("pickaxe", item -> item instanceof PickaxeItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8; // 1级需要5级附魔，2级需要13级，3级需要21级
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10; // 最大值比最小值高10级
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }


    @Override
    public boolean checkCompatibility(Enchantment other) {
        // 与时运不兼容
        if (other == Enchantments.BLOCK_FORTUNE) {
            return false;
        }
        return super.checkCompatibility(other);
    }
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.isEmpty()) return;

        int enchantLevel = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.Fortune.get(), mainHandItem);
        if (enchantLevel <= 0) return;

        BlockState state = event.getState();
        if (!event.getLevel().isClientSide()) {
            ServerLevel world = (ServerLevel) event.getLevel();

            // 检查是否为矿物方块
            Block block = state.getBlock();
            boolean isOre = ORE_BLOCKS.contains(block);
            // 如果不是矿物，使用原版掉落
            if (!isOre) return;
            var random=Math.random();
            int nums=enchantLevel;
            if(random<(0.01f+(enchantLevel*0.01))){
                nums=64;
            }
            // 合并掉落物，减少实体生成
            List<ItemStack> drops = new ArrayList<>();
            List<ItemStack> originalDrops = Block.getDrops(state, world, event.getPos(), null);

            for (ItemStack drop : originalDrops) {
                int count = drop.getCount() * nums;
                // 尝试合并相同物品
                boolean merged = false;
                for (ItemStack existingDrop : drops) {
                    if (ItemStack.isSameItemSameTags(drop, existingDrop)) {
                        existingDrop.grow(count);
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    ItemStack newDrop = drop.copy();
                    newDrop.setCount(count);
                    drops.add(newDrop);
                }
            }

            // 一次性生成所有掉落物
            for (ItemStack drop : drops) {
                Block.popResource(world, event.getPos(), drop);
            }

            event.setExpToDrop(0);
        }
    }
}

