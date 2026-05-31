package com.net.wenwen.entity;

import com.net.wenwen.Config;
import com.net.wenwen.common.WorldStateManager;
import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class HostileBat extends Bat {
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TYPE_ID = SynchedEntityData.defineId(HostileBat.class, EntityDataSerializers.INT);

    public HostileBat(EntityType<? extends Bat> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new HostileBatMoveControl(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_TYPE_ID, this.random.nextInt(100));
    }

    public int getAttackType() {
        return this.entityData.get(DATA_ATTACK_TYPE_ID);
    }

    public void setAttackType() {
        this.entityData.set(DATA_ATTACK_TYPE_ID, this.random.nextInt(100));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new BatAttackGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 20, true, false, (player) -> {
            // 排除创造和旁观
            if (player instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return true;
        }));
    }

    public static boolean checkSpawnRules(EntityType<? extends HostileBat> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // 1. 基础环境过滤：水面上不刷
        if (level.getBlockState(pos.below()).is(Blocks.WATER)) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            long dayTime = serverLevel.dayTime() % 24000;
            boolean isUp = WorldStateManager.isWorldUp;
            boolean isDaytime = dayTime < 12000 || dayTime > 13000; // 增加了黄昏/黎明的缓冲期

            float spawnChance;
            if (!isUp) {
                spawnChance = 0.005f;
            } else if (isDaytime) {
                spawnChance = 0.01f;
            } else {
                spawnChance = 0.1f;
            }
            return random.nextFloat() < spawnChance;
        }
        return false;
    }

    public static AttributeSupplier prepareAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FLYING_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MAX_HEALTH, 50.0)
                .build();
    }

    @Override
    public int getExperienceReward() {
        boolean isUp = WorldStateManager.isWorldUp;
        if(!isUp){
            return 50;
        }
        int randomType = this.getAttackType();
        if(randomType < 20){
            return 800;
        }
        else if(randomType < 40) {
            return 400;
        }
        else {
            return 100;
        }
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation("wenwen", "entities/hostile_bat");
    }

    @Override
    public boolean isSunBurnTick() {
        return true;
    }

    @Override
    public @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    public @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    public static class BatAttackGoal extends Goal {
        private final HostileBat bat;
        private int attackTime = 0;

        public BatAttackGoal(HostileBat bat) {
            this.bat = bat;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.bat.getTarget();
            if (target == null) return false;
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
                this.bat.setTarget(null);
                return false;
            }
            return target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.bat.getTarget();
            if (target == null) return false;
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
            if (this.bat.distanceToSqr(target) > 512) return false; // 距离太远放弃
            return target.isAlive();
        }

        @Override
        public void start() {
            this.attackTime = 0;
            this.bat.setSilent(false);
        }

        @Override
        public void stop() {
            this.bat.setTarget(null);
            this.bat.setResting(false);
        }

        @Override
        public void tick() {
            // 提取目标到局部变量，防止后续操作中目标消失导致空指针异常
            LivingEntity target = this.bat.getTarget();
            if (target == null) return;

            // 1. 让蝙蝠看着目标
            this.bat.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // 2. 计算与目标的距离并移动
            double distance = this.bat.distanceToSqr(target);
            double offsetY = target.getEyeY() + (this.bat.getRandom().nextDouble() - 0.5D) * 1.5D;
            this.bat.getMoveControl().setWantedPosition(
                    target.getX(),
                    offsetY,
                    target.getZ(),
                    1.0D
            );

            // 3. 如果距离足够近且冷却结束，就进行攻击
            if (distance < 6.0D && this.attackTime <= 0) {
                this.bat.doHurtTarget(target);
                if (!target.isAlive()){
                    return;
                }

                int randomType = this.bat.getAttackType();
                boolean isUp = WorldStateManager.isWorldUp;

                if(isUp && Config.batAbility){
                    if(randomType < 20){
                        target.addEffect(new MobEffectInstance(WenwenModMobEffects.BeHead.get(), 1200, 0, false, true));
                        if(this.bat.getRandom().nextFloat() < 0.25f){
                            target.addEffect(new MobEffectInstance(WenwenModMobEffects.SUILIE_EFFECT.get(), 100, 10, false, true));
                            target.addEffect(new MobEffectInstance(WenwenModMobEffects.DEHP_EFFECT.get(), 100, 0, false, true));
                            target.addEffect(new MobEffectInstance(WenwenModMobEffects.NOPLAYER_EFFECT.get(), 100, 0, false, true));
                        }
                    }
                    else if(randomType < 40){
                        target.addEffect(new MobEffectInstance(WenwenModMobEffects.DAMAGE_EFFECT.get(), 100, 0, false, true));
                        target.addEffect(new MobEffectInstance(WenwenModMobEffects.SUILIE_EFFECT.get(), 100, 6, false, true));
                        target.addEffect(new MobEffectInstance(WenwenModMobEffects.DEHP_EFFECT.get(), 100, 0, false, true));
                    }
                    else if(randomType < 60){
                        target.addEffect(new MobEffectInstance(WenwenModMobEffects.DAMAGE_EFFECT.get(), 100, 0, false, true));
                        target.addEffect(new MobEffectInstance(WenwenModMobEffects.SUILIE_EFFECT.get(), 100, 4, false, true));
                    }
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 4, false, true));
                }
                else{
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
                }

                this.attackTime = 20; // 1秒攻击冷却
            }

            // 4. 减少攻击冷却时间
            if (this.attackTime > 0) {
                this.attackTime--;
            }
        }
    }
}
