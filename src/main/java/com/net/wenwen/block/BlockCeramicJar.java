package com.net.wenwen.block;

import com.net.wenwen.entity.HostileBat;
import com.net.wenwen.entity.ModEntities;
import com.net.wenwen.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


public class BlockCeramicJar extends Block {

    private static final VoxelShape SHAPE = Block.box(4.5D, 0.0D, 4.5D, 10.5D, 10.5D, 10.5D);

    public BlockCeramicJar(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (level.random.nextFloat() < 0.05F) {
            spawnBat(level, pos);
        }
    }

    private void spawnBat(ServerLevel level, BlockPos pos) {
        HostileBat bat = ModEntities.HOSTILE_BAT.get().create(level);
        if (bat != null) {
            bat.setAttackType();
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 0.2D;
            double z = pos.getZ() + 0.5D;
            bat.moveTo(x, y, z, level.random.nextFloat() * 360F, 0.0F);
            level.addFreshEntity(bat);
        }
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            level.destroyBlock(pos, true);
            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty() && heldItem.isDamageableItem()) {
                heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            }
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (projectile instanceof AbstractArrow) {
            if (!level.isClientSide) {
                level.destroyBlock(hit.getBlockPos(), true);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && fromPos.getY() == pos.getY() - 1) {
            if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                level.destroyBlock(pos, true);
            }
        }
    }
}
