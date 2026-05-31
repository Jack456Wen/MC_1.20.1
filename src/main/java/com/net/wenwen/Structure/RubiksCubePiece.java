package com.net.wenwen.Structure;

import com.net.wenwen.block.MysteriousStoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import com.net.wenwen.init.Registration;
import net.minecraft.world.level.material.Fluids;

public class RubiksCubePiece extends StructurePiece {

    private static final ResourceLocation RUBIKS_LOOT = new ResourceLocation("wenwen", "chests/rubiks_cube_loot");
    private static final ResourceLocation LOCK_LOOT = new ResourceLocation("wenwen", "chests/lock_chest_loot");

    // ★ 性能优化1：将方块状态提取为静态常量，避免每次生成结构时重复调用 defaultBlockState()
    // 这会在类加载时计算一次，之后直接从内存读取，极大减少方法调用开销
    private static final BlockState OVERWORLD_CORE = Blocks.STONE.defaultBlockState();
    private static final BlockState NETHER_CORE = Blocks.NETHER_BRICKS.defaultBlockState();
    private static final BlockState END_CORE = Blocks.END_STONE_BRICKS.defaultBlockState();

    private static final BlockState OVERWORLD_OUTER = Registration.MYSTERIOUS_STONE.get().defaultBlockState();
    private static final BlockState NETHER_OUTER = Registration.MYSTERIOUS_STONE.get().defaultBlockState()
            .setValue(MysteriousStoneBlock.DIMENSION, MysteriousStoneBlock.DimensionType.THE_NETHER);
    private static final BlockState END_OUTER = Registration.MYSTERIOUS_STONE.get().defaultBlockState()
            .setValue(MysteriousStoneBlock.DIMENSION, MysteriousStoneBlock.DimensionType.THE_END);

    private static final BlockState VANILLA_CHEST_NORTH = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
    private static final BlockState MOD_CHEST_NORTH = Registration.CHEST.get().defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);

    private BlockState coreBlockState;
    private BlockState outerBlockState;
    private RubiksCubeStructure.DimensionVariant variant;

    public RubiksCubePiece(StructurePieceType type, int genDepth, BoundingBox boundingBox, RubiksCubeStructure.DimensionVariant variant) {
        super(type, genDepth, boundingBox);
        this.variant = variant;
        initializeBlockStates();
    }

    public RubiksCubePiece(StructurePieceType type, CompoundTag tag) {
        super(type, tag);
        try {
            this.variant = RubiksCubeStructure.DimensionVariant.valueOf(tag.getString("Variant"));
        } catch (Exception e) {
            this.variant = RubiksCubeStructure.DimensionVariant.OVERWORLD;
        }
        initializeBlockStates();
    }

    private void initializeBlockStates() {
        if (this.variant == null) this.variant = RubiksCubeStructure.DimensionVariant.OVERWORLD;

        // ★ 性能优化1体现：直接赋值静态常量，零运行时开销
        switch (this.variant) {
            case NETHER:
                this.coreBlockState = NETHER_CORE;
                this.outerBlockState = NETHER_OUTER;
                break;
            case END:
                this.coreBlockState = END_CORE;
                this.outerBlockState = END_OUTER;
                break;
            default:
                this.coreBlockState = OVERWORLD_CORE;
                this.outerBlockState = OVERWORLD_OUTER;
                break;
        }
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Variant", this.variant.name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos blockPos) {

        // 下界防浮空逻辑 (保持不变，逻辑清晰且安全)
        if (this.variant == RubiksCubeStructure.DimensionVariant.NETHER) {
            int centerX = this.boundingBox.minX() + 2;
            int centerZ = this.boundingBox.minZ() + 2;
            int bottomY = this.boundingBox.minY();

            int dropAmount = 0;
            BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
            for (int i = 1; i <= 8; i++) {
                checkPos.set(centerX, bottomY - i, centerZ);
                BlockState belowState = level.getBlockState(checkPos);
                if (belowState.isAir() || belowState.getFluidState().is(Fluids.LAVA)) {
                    dropAmount = i;
                } else {
                    break;
                }
            }
            if (dropAmount > 0) {
                this.move(0, -dropAmount, 0);
            }
        }

        // --- 极速生成逻辑 ---
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int targetX = this.boundingBox.minX() + 2;
        int targetY = this.boundingBox.minY() + 2;
        int targetZ = this.boundingBox.minZ() + 2;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {

                    mutablePos.set(targetX + dx, targetY + dy, targetZ + dz);

                    // ★ 性能优化3：位运算替代 Math.max
                    // Java的Math.max是方法调用，有栈帧开销。对于取绝对值这种底层操作，位运算快得多
                    int adx = dx < 0 ? -dx : dx;
                    int ady = dy < 0 ? -dy : dy;
                    int adz = dz < 0 ? -dz : dz;
                    int maxDist = adx > ady ? (adx > adz ? adx : adz) : (ady > adz ? ady : adz);

                    if (maxDist == 2) {
                        level.setBlock(mutablePos, this.outerBlockState, 18); // 18 = 16(MOVE_BY_PISTON防掉落) + 2(UPDATE)
                    } else if (maxDist == 1) {
                        level.setBlock(mutablePos, this.coreBlockState, 18);
                    } else {
                        // ★ 性能优化4：最核心的战利品箱生成优化
                        boolean isVanillaChest = random.nextBoolean();
                        BlockState chestState = isVanillaChest ? VANILLA_CHEST_NORTH : MOD_CHEST_NORTH;
                        ResourceLocation lootTable = isVanillaChest ? RUBIKS_LOOT : LOCK_LOOT;

                        // 1. 构造包含战利品表信息的轻量级NBT，避免后续触发复杂的方块更新链
                        CompoundTag lootTag = new CompoundTag();
                        lootTag.putString("LootTable", lootTable.toString());
                        lootTag.putLong("LootTableSeed", random.nextLong());

                        // 2. 放置箱子方块
                        level.setBlock(mutablePos, chestState, 18);
                        BlockEntity be = level.getBlockEntity(mutablePos);
                        if (be != null) {
                            be.load(lootTag);
                        }
                    }
                }
            }
        }
    }
}
