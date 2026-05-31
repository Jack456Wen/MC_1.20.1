package com.net.wenwen.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class MysteriousStoneBlock extends Block {

    // 1. 新增：定义一个维度属性
    public static final EnumProperty<DimensionType> DIMENSION =
            EnumProperty.create("dimension", DimensionType.class);

    public MysteriousStoneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DIMENSION, DimensionType.OVERWORLD));

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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIMENSION);
    }


    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public void setPlacedBy(Level p_49847_, @NotNull BlockPos p_49848_, @NotNull BlockState p_49849_, @Nullable LivingEntity p_49850_, @NotNull ItemStack p_49851_) {
        if (p_49847_.dimension() == Level.NETHER) {
            p_49847_.setBlock(p_49848_, p_49849_.setValue(DIMENSION, DimensionType.THE_NETHER), 3);
        }
        else if (p_49847_.dimension() == Level.END) {
            p_49847_.setBlock(p_49848_, p_49849_.setValue(DIMENSION, DimensionType.THE_END), 3);
        }
    }

    // 4. 新增：定义维度的枚举类 (实现 StringRepresentable 以便JSON能识别)
    public enum DimensionType implements StringRepresentable {
        OVERWORLD("overworld"),
        THE_NETHER("the_nether"),
        THE_END("the_end");
        private final String name;

        DimensionType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}

