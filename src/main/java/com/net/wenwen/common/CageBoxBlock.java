package com.net.wenwen.common;

import com.net.wenwen.init.Registration;
import com.net.wenwen.utils.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * @Project: CageBox
 * @Author: cnlimiter
 * @CreateTime: 2025/7/6 00:51
 * @Note:
 */
public class CageBoxBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape CAGE_COLLISION_AABB_EMPTY = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);
    private static final VoxelShape CAGE_COLLISION_AABB_FULL = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public CageBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, Boolean.FALSE).setValue(WATERLOGGED, Boolean.FALSE));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, WATERLOGGED);
    }

    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        this.spawnDestroyParticles(worldIn, player, pos, state);
        if (!worldIn.isClientSide) {
            if (worldIn.getBlockEntity(pos) instanceof CageBoxBlockEntity te) {

                ItemStack itemstack = new ItemStack(Registration.TRAP_CAGE.get());
                CompoundTag compound = new CompoundTag();
                if (te.hasTagCompound() && te.isLocked()) {
                    compound.putBoolean("closed", te.isLocked());
                    compound.put("EntityTag", te.getTagCompound().getCompound("EntityTag"));
                    itemstack.setTag(compound);
                }

                popResource(worldIn, pos, itemstack);
                worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
            else { super.playerWillDestroy(worldIn, pos, state, player); }
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return state.getValue(OPEN) ? CAGE_COLLISION_AABB_EMPTY : CAGE_COLLISION_AABB_FULL;
    }

    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return state.getValue(OPEN);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return blockstate.setValue(OPEN, Boolean.TRUE).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }


    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.hasTag()) {
            BlockEntity te = worldIn.getBlockEntity(pos);
            if (stack.getTag() != null && te instanceof CageBoxBlockEntity blockEntity) {
                te.load(stack.getTag());
                if (
                        blockEntity.isLocked() &&
                                blockEntity.hasTagCompound()) {
                    worldIn.setBlockAndUpdate(pos, state.setValue(OPEN, Boolean.FALSE));
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide) {
            if (worldIn.hasNeighborSignal(pos) && worldIn.getBlockEntity(pos) instanceof CageBoxBlockEntity boxBlockEntity) {
                trySpawningEntity(boxBlockEntity, state, (ServerLevel) worldIn, pos);
            }
        }
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.FAIL;
        }
            var te = worldIn.getBlockEntity(pos);
            if (te instanceof CageBoxBlockEntity blockEntity) {
                if (!state.getValue(OPEN)) {
                    boolean success = trySpawningEntity(blockEntity, state, (ServerLevel) worldIn, pos);
                    return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                } else if (playerIn.isCrouching()) {
                    blockEntity.setTagCompound(null);
                    blockEntity.setLocked(false);
                    blockEntity.setChanged();
                    playerIn.displayClientMessage(Component.translatable("message.wenwen.unlock_success"),  true);
                    return InteractionResult.PASS;
                } else return super.use(state, worldIn, pos, playerIn, hand, hit);
            } else return super.use(state, worldIn, pos, playerIn, hand, hit);

    }

    private boolean trySpawningEntity(CageBoxBlockEntity te, BlockState state, ServerLevel worldIn, BlockPos pos) {
        BlockPos check = pos.below();
        if (te != null) {
            BlockPos spawnpos = /*!worldIn.getBlockState(check).isSolid() ? pos :*/ new BlockPos(pos.getX(), (int) (pos.getY() + 1F), pos.getZ());
            if (te.spawnCagedCreature(worldIn, spawnpos, worldIn.isEmptyBlock(check))) {

                spawnParticles(worldIn, pos, ParticleTypes.POOF);
                worldIn.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.8F);
                worldIn.setBlockAndUpdate(pos, state.setValue(OPEN, Boolean.TRUE));
                return true;
            }
            else {
                spawnParticles(worldIn, pos, ParticleTypes.SMOKE);
                worldIn.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 0.6F);
            }
        }
        return false;
    }

    private <T extends ParticleOptions> void spawnParticles(Level worldIn, BlockPos pos, T particle) {
        RandomSource random = worldIn.getRandom();
        float d3 = random.nextFloat() * 0.02F;
        float d1 = random.nextFloat() * 0.02F;
        float d2 = random.nextFloat() * 0.02F;
        ((ServerLevel)worldIn).sendParticles(particle, pos.getX() + random.nextFloat(), pos.getY(), pos.getZ() + random.nextFloat(), 15, d3, d1, d2, 0.12F);
    }


    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (world.getBlockEntity(pos) instanceof CageBoxBlockEntity te && !world.isClientSide && !(entity instanceof Player) && entity.isAlive() && entity instanceof LivingEntity) {
            if (te != null && !te.isLocked() && entity instanceof Mob mob) {
                if(mob.getHealth()>mob.getMaxHealth()*0.25){
                    FindPlayer(mob);
                    return;
                }
                if (te.cageEntity(mob)) {
                    world.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.8F);
                    world.setBlockAndUpdate(pos, state.setValue(OPEN, Boolean.FALSE));
                    spawnParticles(world, pos, ParticleTypes.POOF);
                }
            }
        }
    }
    private void FindPlayer(Entity entity){
        final Vec3 center = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        List<LivingEntity> list = entity.level().getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(10), e -> true).stream()
                .sorted(Comparator.comparingDouble(ent -> ent.distanceToSqr(center))).toList();
        list.forEach(e -> {
            if(e instanceof Player player && !player.level().isClientSide()){
                player.displayClientMessage(Component.translatable("message.wenwen.fail"),  true);
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (stack.getTag() != null) {
            EntityType<?> type = EntityUtils.getEntityTypeFromTag(stack.getTag(), null);
            if (type != null) {
                EntityUtils.buildTooltipData(stack, tooltip, type, "");
            }
        }
        else {
            tooltip.add(Component.translatable("block.trap_cage.state_empty").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CageBoxBlockEntity(pos, state);
    }

    public static class DispenserBehaviorTrapCage implements DispenseItemBehavior {

        @Override
        public @NotNull ItemStack dispense(@NotNull BlockSource source, ItemStack stack) {
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.getPos().relative(direction);
                Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
                boolean successful = ((BlockItem)item).place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1)) == InteractionResult.SUCCESS;
            }
            return stack;
        }

        protected void playDispenseSound(BlockSource source) {
            source.getLevel().globalLevelEvent(1000, source.getPos(), 0);
        }
    }
}
