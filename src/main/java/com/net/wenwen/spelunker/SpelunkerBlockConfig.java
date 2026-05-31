package com.net.wenwen.spelunker;

import net.minecraft.world.level.block.Block;

public class SpelunkerBlockConfig {

    private Block block;

    private final int color;
    private final boolean transition;
    private final int effectRadius;

    private int blockRadiusMax;
    private int blockRadiusMin;

    public SpelunkerBlockConfig(int color, boolean transition, int effectRadius) {
        this.color = color;
        this.transition = transition;
        this.effectRadius = effectRadius;
        parseEffectRadius();
    }

    private void parseEffectRadius() {
        int chunkRadius = (int) Math.ceil(effectRadius / 16f);
        if(chunkRadius > 1)
            chunkRadius = 1;
        blockRadiusMax = (int) Math.pow(effectRadius, 2);
        blockRadiusMin = (int) Math.pow(effectRadius - 1, 2);
    }

    public SpelunkerBlockConfig setBlock(Block block) {
        this.block = block;
        return this;
    }

    public Block getBlock() {
        return block;
    }

    public int getColor() {
        return color;
    }

    public boolean isTransition() {
        return transition;
    }

    public int getEffectRadius() {
        return effectRadius;
    }

    public int getBlockRadiusMax() {
        return blockRadiusMax;
    }

    public int getBlockRadiusMin() {
        return blockRadiusMin;
    }
}
