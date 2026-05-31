package com.net.wenwen.block;

import com.net.wenwen.init.Registration;
import com.net.wenwen.init.WenwenModItems;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class LockChestBlock extends ChestBlock {

    public LockChestBlock() {
        super(Properties.of().strength(-1.0F).sound(SoundType.STONE), () -> Registration.CHEST_TILE_TYPE.get());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityChest(pos, state);
    }
    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, pos, player);
        if (!level.isClientSide) {
            level.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_BREAK,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player Player, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);

            if (be instanceof TileEntityChest lockChest) {

                // 1. 如果玩家主手拿着钻石
                if (Player.getItemInHand(pHand).is(WenwenModItems.Key.get())) {
                    if (lockChest.isLocked()) {
                        // 上锁状态 -> 解锁
                        lockChest.setLocked(false);
                        Player.getItemInHand(pHand).shrink(1);
                        Player.level().playSound(
                                null,
                                Player.blockPosition(),
                                WenwenModSounds.UNLOCK_CHEST.get(),
                                SoundSource.AMBIENT,
                                1.0F,
                                1.0F
                        );
                        Player.displayClientMessage(Component.translatable("message.wenwen.unlock_chest_unlock_success"),true);
                    } else {
                        // 已经解锁了 -> 提示
                        Player.displayClientMessage(Component.translatable("message.wenwen.unlock_chest"),true);
                    }
                    return InteractionResult.CONSUME; // 拦截默认打开箱子的行为
                }

                else if (lockChest.isLocked()) {
                    Player.displayClientMessage(Component.translatable("message.wenwen.lock_chest"),true);
                    Player.level().playSound(
                            null,
                            Player.blockPosition(),
                            WenwenModSounds.LOCK_CHEST.get(),
                            SoundSource.AMBIENT,
                            1.0F,
                            1.0F
                    );
                    return InteractionResult.FAIL; // 拦截默认打开箱子的行为
                }
            }
        }

        // 3. 如果箱子没上锁，或者不是我们的 TileEntity，走原版逻辑（打开箱子）
        return super.use(pState, pLevel, pPos, Player, pHand, pHit);
    }
}
