package com.net.wenwen.network;

import com.net.wenwen.init.Registration;
import com.net.wenwen.spelunker.SpelunkerBlockConfig;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class SpelunkerBlockConfigManager {

    private static final Map<net.minecraft.world.level.block.Block, SpelunkerBlockConfig> blockConfigs = new HashMap<>();

    public static void init() {
        blockConfigs.clear();
        addBlockConfig(Registration.MYSTERIOUS_STONE.get(), 0xffffff, true, 36);
        // 铁矿石配置
        addBlockConfig(Blocks.IRON_ORE, 0xd4af94, true, 24);
        addBlockConfig(Blocks.DEEPSLATE_IRON_ORE, 0xd4af94, true, 24);

        addBlockConfig(Blocks.GOLD_ORE, 0xfff52e, true, 24);
        addBlockConfig(Blocks.DEEPSLATE_GOLD_ORE, 0xfff52e, true, 24);
        addBlockConfig(Blocks.NETHER_GOLD_ORE, 0xfff52e, true, 24);
        
        addBlockConfig(Blocks.DIAMOND_ORE, 0x2ee0ff, true, 20);
        addBlockConfig(Blocks.DEEPSLATE_DIAMOND_ORE, 0x2ee0ff, true, 20);
        
        addBlockConfig(Blocks.EMERALD_ORE, 0x2eff35, true, 24);
        addBlockConfig(Blocks.DEEPSLATE_EMERALD_ORE, 0x2eff35, true, 24);
        
        addBlockConfig(Blocks.LAPIS_ORE, 0x312eff, true, 32);
        addBlockConfig(Blocks.DEEPSLATE_LAPIS_ORE, 0x312eff, true, 32);
        
        addBlockConfig(Blocks.REDSTONE_ORE, 0xff2e2e, true, 24);
        addBlockConfig(Blocks.DEEPSLATE_REDSTONE_ORE, 0xff2e2e, true, 24);
        
        addBlockConfig(Blocks.NETHER_QUARTZ_ORE, 0xffffff, true, 32);
    }

    private static void addBlockConfig(net.minecraft.world.level.block.Block block, int color, boolean transition, int effectRadius) {
        SpelunkerBlockConfig config = new SpelunkerBlockConfig(color, transition, effectRadius);
        config.setBlock(block);
        blockConfigs.put(block, config);
    }

    public static SpelunkerBlockConfig getConfig(net.minecraft.world.level.block.Block block) {
        return blockConfigs.get(block);
    }

    public static boolean isOreBlock(net.minecraft.world.level.block.Block block) {
        return blockConfigs.containsKey(block);
    }

    public static int getBlockConfigsSize() {
        return blockConfigs.size();
    }
}
