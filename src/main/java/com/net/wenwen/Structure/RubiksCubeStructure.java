package com.net.wenwen.Structure;

import com.mojang.serialization.Codec;
import com.net.wenwen.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RubiksCubeStructure extends Structure {

    public static final Codec<RubiksCubeStructure> CODEC = simpleCodec(RubiksCubeStructure::new);

    public RubiksCubeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return Registration.RUBIKS_CUBE_STRUCTURE.get();
    }

    // ★ 性能优化1：缓存坐标计算，提取为类局部变量，避免重复位移运算
    private int checkX;
    private int checkZ;

    // ★ 性能优化1：将生物群系获取提取为独立方法，使用正确的Y坐标，并缓存结果
    private DimensionVariant cachedVariant;

    private DimensionVariant inferDimension(GenerationContext context) {
        if (cachedVariant != null) return cachedVariant; // 避免重复查表

        // ★ 重要修复：1.18+ 是3D生物群系，传入 Y=0 在下界会极度不准！
        // 这里使用 Quart 档位 (>> 2)，我们预期结构生成在 Y=30 左右，所以传入 30 >> 2 = 7
        int checkQuartY = 7;
        Holder<Biome> biome = context.chunkGenerator().getBiomeSource().getNoiseBiome(
                checkX >> 2, checkQuartY, checkZ >> 2, context.randomState().sampler()
        );

        if (biome.is(BiomeTags.IS_NETHER)) cachedVariant = DimensionVariant.NETHER;
        else if (biome.is(BiomeTags.IS_END)) cachedVariant = DimensionVariant.END;
        else cachedVariant = DimensionVariant.OVERWORLD;

        return cachedVariant;
    }

    @Override
    public @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // 计算区块中心坐标，全局复用
        this.checkX = context.chunkPos().getMiddleBlockX();
        this.checkZ = context.chunkPos().getMiddleBlockZ();
        this.cachedVariant = null; // 每次生成新区块时重置缓存

        DimensionVariant variant = inferDimension(context);
        RandomSource random = context.random();
        int baseY;

        // ★ 性能优化2：极其精简的高度获取和拦截逻辑，减少不必要的方法调用栈
        switch (variant) {
            case NETHER:
                baseY = random.nextIntBetweenInclusive(25, 40);
                break;
            case END:
                baseY = context.chunkGenerator().getFirstOccupiedHeight(checkX, checkZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                if (baseY < 30) return Optional.empty(); // 末端岛太低，不生成
                baseY -= 3;
                break;
            default: // OVERWORLD
                int floorY = context.chunkGenerator().getFirstOccupiedHeight(checkX, checkZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
                if (floorY < context.heightAccessor().getMinBuildHeight() + 5) return Optional.empty();

                // ★ 性能优化3：只查一次地表高度，用简单的逻辑判断水深，避免重复调用 getFirstOccupiedHeight
                int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(checkX, checkZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                if (surfaceY - floorY > 8) return Optional.empty(); // 水太深(超过8格)，不生成

                baseY = floorY - 3 - random.nextInt(3);
                break;
        }

        int finalBaseY = baseY;
        // 直接构造方块坐标，无需再取一次 startX/startZ
        return Optional.of(new GenerationStub(
                new BlockPos(checkX, finalBaseY, checkZ),
                (builder) -> generatePieces(builder, context, finalBaseY, variant)
        ));
    }

    private void generatePieces(StructurePiecesBuilder builder, GenerationContext context, int baseY, DimensionVariant variant) {
        RandomSource random = context.random();
        int startX = context.chunkPos().getMinBlockX();
        int startZ = context.chunkPos().getMinBlockZ();

        int maxPieces = 2;
        int piecesPlaced = 0;
        int maxAttempts = 8;

        for (int attempt = 0; attempt < maxAttempts && piecesPlaced < maxPieces; attempt++) {
            if (random.nextFloat() > 0.6f) continue;

            int x = startX + random.nextIntBetweenInclusive(2, 13);
            int z = startZ + random.nextIntBetweenInclusive(2, 13);


            int yOffset = random.nextIntBetweenInclusive(-4, 1);
            int y = baseY + yOffset;

            BoundingBox actualBox = new BoundingBox(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2);

            if (builder.findCollisionPiece(actualBox) != null) {
                continue;
            }

            builder.addPiece(new RubiksCubePiece(
                    Registration.RUBIKS_CUBE_PIECE.get(), 0, actualBox, variant
            ));
            piecesPlaced++;
        }
    }


    public enum DimensionVariant {
        OVERWORLD, NETHER, END
    }
}
