package com.net.wenwen.entity;

import com.net.wenwen.init.WenwenModItems;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class EntityOctorok extends Squid {

    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(EntityOctorok.class, EntityDataSerializers.INT);

    public EntityOctorok(EntityType<? extends EntityOctorok> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, 0);
    }

    public int getOctorokType() {
        return this.entityData.get(DATA_TYPE_ID);
    }

    @Override
    protected void dropEquipment()
    {
        if (this.level() instanceof ServerLevel server) {
            if(this.getOctorokType() != 1){
                return;
            }
            if (this.random.nextDouble() <= 0.04F) {
                final ItemEntity item = new ItemEntity(EntityType.ITEM, server);
                item.setItem(new ItemStack(WenwenModItems.HEARTCONTAINER.get()));
                item.moveTo(this.position());
                server.addFreshEntity(item);
            }
            if (this.random.nextDouble() >= 0.92F) {
                final ItemEntity item = new ItemEntity(EntityType.ITEM, server);
                item.setItem(new ItemStack(WenwenModItems.MIRROR.get()));
                item.moveTo(this.position());
                server.addFreshEntity(item);
            }
        }
    }


    public static AttributeSupplier prepareAttributes() {
        return Squid.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.MAX_HEALTH, 50.0)
                .build();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new OctorokSurfaceGoal(this));
        this.goalSelector.addGoal(2, new OctorokRangedAttackGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 32, true, false, (player) -> {
            // 排除创造和旁观
            if (player instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return true;
        }));
    }

    public static class OctorokSurfaceGoal extends Goal {
        private final EntityOctorok octorok;
        private int surfaceCheckCooldown = 0;
        private double surfaceY = -1;

        public OctorokSurfaceGoal(EntityOctorok octorok) {
            this.octorok = octorok;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            return this.octorok.getTarget() != null && this.octorok.isInWater();
        }

        @Override
        public boolean canContinueToUse() {
            return this.octorok.isInWater() && this.octorok.getTarget() != null && this.octorok.getTarget().isAlive();
        }

        @Override
        public void tick() {
            if (!this.octorok.level().isClientSide) {
                if (--this.surfaceCheckCooldown <= 0) {
                    this.surfaceCheckCooldown = 20;
                    BlockPos pos = this.octorok.blockPosition();
                    if (this.octorok.level().getFluidState(pos).is(FluidTags.WATER)) {
                        this.surfaceY = pos.getY() + this.octorok.level().getFluidState(pos).getHeight(this.octorok.level(), pos);
                    } else {
                        this.surfaceY = this.octorok.getEyeY();
                    }
                }
                if (this.octorok.getEyeY() >= this.surfaceY - 0.5D) {
                    this.octorok.setMovementVector(0.0F, 0.0F, 0.0F);
                    this.octorok.setDeltaMovement(this.octorok.getDeltaMovement().multiply(0.1, 0.1, 0.1));
                } else {
                    this.octorok.setMovementVector(0.0F, 0.3F, 0.0F);
                }
            }
        }

    }

    public static class OctorokRangedAttackGoal extends Goal {
        private final EntityOctorok octorok;
        private int attackTimer = 0;

        private int seeTimeCooldown = 0;

        public OctorokRangedAttackGoal(EntityOctorok octorok) {
            this.octorok = octorok;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.octorok.getTarget();
            if (target != null && target.isAlive()) {
                if (this.seeTimeCooldown > 0) {
                    this.seeTimeCooldown--;
                    return false;
                }
                double distance = this.octorok.distanceToSqr(target);
                return distance > 9.0D && distance < 512.0D;
            }
            return false;
        }

        @Override
        public void stop() {
            this.seeTimeCooldown = 20;
            this.attackTimer = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = this.octorok.getTarget();
            if (target == null) return;

            this.octorok.getLookControl().setLookAt(target, 30.0F, 30.0F);

            this.attackTimer++;
            if (this.attackTimer >= 50) {
                if (this.octorok.getRandom().nextFloat() < 0.8F) {
                    this.shootProjectile(target);
                }
                this.attackTimer = 0;
            }
        }

        private void shootProjectile(LivingEntity target) {
            Vec3 launchPos = this.octorok.getEyePosition().add(0, 0.2D, 0);
            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);

            double deltaX = targetPos.x - launchPos.x;
            double deltaY = targetPos.y - launchPos.y;
            double deltaZ = targetPos.z - launchPos.z;

            EntityOctorokSnowball snowball = new EntityOctorokSnowball(this.octorok.level(), this.octorok);
            snowball.setPos(launchPos.x, launchPos.y, launchPos.z);
            snowball.shoot(deltaX, deltaY+1, deltaZ, 3F, 0.0F);
            this.octorok.level().addFreshEntity(snowball);
            this.octorok.playSound(WenwenModSounds.BOMB.get(), 1.0F, 1.0F);
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.isAlive()) {
            this.discard();
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(source);
    }


    public static boolean checkSpawnRules(EntityType<? extends EntityOctorok> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random)  {

        if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;

        if (pos.getY() < 40) return false;
        if (random.nextFloat() > 0.12f){
            return false;
        }
        return true;
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);

        if (!this.level().isClientSide && data == null) {
            int type = this.random.nextInt(5) == 0 ? 1 : 0;
            this.entityData.set(DATA_TYPE_ID, type);
            if (type == 1) {
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
                this.setHealth(this.getMaxHealth());
            }
        }

        return data;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation("wenwen", "entities/octorok");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("OctorokType", this.getOctorokType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("OctorokType")) {
            this.entityData.set(DATA_TYPE_ID, tag.getInt("OctorokType"));
        }
    }
    @Override
    public int getExperienceReward(){
        if(this.getOctorokType() == 1){
            return 800;
        }
        return 400;
    }
}
