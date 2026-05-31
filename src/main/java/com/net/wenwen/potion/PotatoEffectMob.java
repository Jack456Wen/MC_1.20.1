package com.net.wenwen.potion;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

public class PotatoEffectMob extends MobEffect {
    public PotatoEffectMob() {
        super(MobEffectCategory.BENEFICIAL, -16776961);
    }

    /*@Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            //int potatoCount = countMaturePotatoesAround(player, 3, 1);
            int potatoCount =10;
            double effectivePotatoCount = Math.log(potatoCount + 1) * 10; // 缩放因子用10
            int experienceToAdd = (int) ((1 + amplifier + effectivePotatoCount) * 20);
            // 6. 给予玩家经验
            if (experienceToAdd > 0) {
                player.giveExperiencePoints(experienceToAdd);
                if(potatoCount>60){
                    player.experienceLevel+=1;
                }
                if(potatoCount>120){
                    player.experienceLevel+=2;
                }
                if(potatoCount>200){
                    player.experienceLevel+=3;
                }
                int experienceAmount = (100+(potatoCount*2))*amplifier;
                ExperienceOrb orb = new ExperienceOrb(player.level(), player.position().x, player.position().y, player.position().z, experienceAmount);
                player.level().addFreshEntity(orb);
            }
        }
    }*/
    private int countMaturePotatoesAround(Player player, int horizontalRadius, int verticalHeight) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        AABB area = new AABB(
                playerPos.getX() - horizontalRadius,
                playerPos.getY() - verticalHeight,
                playerPos.getZ() - horizontalRadius,
                playerPos.getX() + horizontalRadius,
                playerPos.getY() + verticalHeight,
                playerPos.getZ() + horizontalRadius
        );

        int maturePotatoCount = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                (int) area.minX, (int) area.minY, (int) area.minZ,
                (int) area.maxX, (int) area.maxY, (int) area.maxZ
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.POTATOES)) {
                // 检查生长阶段是否为最大值（7）
                if (state.getValue(CropBlock.AGE) == 7) {
                    maturePotatoCount++;
                }
            }
        }

        return maturePotatoCount;
    }
}
