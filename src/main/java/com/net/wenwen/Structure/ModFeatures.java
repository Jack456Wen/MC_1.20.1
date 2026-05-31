package com.net.wenwen.Structure;

import com.net.wenwen.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ModFeatures {

    public static class JarClusterFeature extends Feature<NoneFeatureConfiguration> {

        public JarClusterFeature() {
            super(NoneFeatureConfiguration.CODEC);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            BlockPos origin = context.origin();
            RandomSource rand = context.random();
            WorldGenLevel level = context.level(); // 优化1：缓存 level，避免循环内反复调用方法

            int jarsPerCluster = 8 - rand.nextInt(7); // 保持你的原逻辑：2 到 8 个
            boolean success = false;

            BlockState jarState = Registration.CERAMIC_JAR.get().defaultBlockState();


            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            for (int l = 0; l < 64 && jarsPerCluster > 0; ++l) {
                // 优化4：rand.nextInt(8) - 4 等同于 rand.nextInt(4) - rand.nextInt(4) (范围-3到+4)，但少生成一次随机数
                int x = origin.getX() + rand.nextInt(8) - 4;
                int y = origin.getY() + rand.nextInt(8) - 4; // 保持你原来的 Y 轴波动范围
                int z = origin.getZ() + rand.nextInt(8) - 4;

                // 设置当前位置
                mutablePos.set(x, y, z);

                // 判断当前位置是否为空
                if (level.isEmptyBlock(mutablePos)) {

                    // 优化5：终极性能优化！不调用 mutablePos.below()，避免创建临时 BlockPos 对象
                    // 直接将 Y 坐标减 1 去检查下面的方块
                    mutablePos.setY(y - 1);
                    if (level.getBlockState(mutablePos).isSolid()) {

                        // 检查通过，把 Y 坐标恢复回去，放置罐子
                        mutablePos.setY(y);
                        level.setBlock(mutablePos, jarState, 2);

                        --jarsPerCluster;
                        success = true;
                    }
                }
            }
            return success;
        }
    }
}
