package com.net.wenwen.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MirrorBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // 定义各个方向的边框形状
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(1, 1, 0, 15, 15, 1), // 中心玻璃
            Block.box(0, 6, 0, 16, 10, 1), // 水平边框
            Block.box(6, 0, 0, 10, 16, 1)  // 垂直边框
    );
    private static final VoxelShape EAST_SHAPE = rotateShape(Direction.NORTH, Direction.EAST, NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateShape(Direction.NORTH, Direction.SOUTH, NORTH_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateShape(Direction.NORTH, Direction.WEST, NORTH_SHAPE);

    public MirrorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 检查是否能放置在墙上
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                BlockState blockstate = this.defaultBlockState().setValue(FACING, direction.getOpposite());
                if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
                    return blockstate;
                }
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        return level.getBlockState(pos.relative(direction)).isSolidRender(level, pos.relative(direction));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        // 如果支撑方块被破坏，镜子也会被破坏
        return direction == state.getValue(FACING) && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case EAST: return EAST_SHAPE;
            case SOUTH: return SOUTH_SHAPE;
            case WEST: return WEST_SHAPE;
            default: return NORTH_SHAPE;
        }
    }

    // --- 核心渲染逻辑 ---
    // 我们通过覆盖 getOcclusionShape 来隐藏相邻镜子的边框
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        // 返回一个完整的方块形状，这样相邻的方块就不会渲染被遮挡的面
        // 这里的关键是，我们让镜子“假装”自己是一个完整的方块，从而骗过相邻方块的渲染逻辑
        // 但我们自己的渲染模型（mirror.json）是带边框的，所以视觉效果上只有边框
        return Shapes.block();
    }

    // 辅助方法：旋转VoxelShape
    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                buffer[1] = Shapes.or(buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX));
            });
        }
        return buffer[1];
    }
}
