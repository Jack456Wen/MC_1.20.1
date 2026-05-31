package com.net.wenwen.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityOctorokSnowball extends Snowball {

    private int OctorokLevel=0;
    public EntityOctorokSnowball(EntityType<? extends Snowball> entityType, Level level) {
        super(entityType, level);
    }

    public EntityOctorokSnowball(Level level, LivingEntity shooter) {
        super(level, shooter);
        if(shooter instanceof EntityOctorok oct){
            OctorokLevel=oct.getOctorokType();
        }
    }
    @Override
    public void tick() {
        if (this.isInWater()) {
            this.setNoGravity(true);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.99F));
        } else {
            this.setNoGravity(false);
        }
        if (this.tickCount > 40) {
            Tnt();
            this.discard();
            return;
        }
        super.tick();
    }
    @Override
    protected void onHit(HitResult result) {
        // 只在服务端执行爆炸和生成逻辑，防止客户端和服务端打架导致双重爆炸
        if (!this.level().isClientSide) {
            Tnt();
            // 如果击中的是实体，给个稍微大一点的额外击退力（模拟气浪）
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) result;
                if (entityHitResult.getEntity() instanceof LivingEntity target) {
                    // 产生一个强烈的向上的气浪，把玩家炸飞起来
                    target.setDeltaMovement(target.getDeltaMovement().add(0, 0.5D, 0));
                    target.hurtMarked = true; // 强制同步击飞动作给客户端
                }
            }
        }

        // 雪球本身消失
        this.discard();
    }
    private int Getdifficulty(){
        return this.level().getDifficulty().getId();
    }
    private void Tnt(){
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.TNT_PRIMED, SoundSource.HOSTILE, 2.0F, 0.5F);

        int damage=OctorokLevel == 1 ? 2 : 1;
        if (this.level() instanceof ServerLevel serverLevel) {
            // 产生 30 个向外扩散的巨大烟雾粒子
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX(), this.getY(), this.getZ(), 30*damage,
                    0.5D, 0.5D, 0.5D, 0.1D);
            // 产生 18 个爆裂的火焰粒子
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 18*damage,
                    0.3D, 0.3D, 0.3D, 0.05D);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 0.8F);
        this.level().explode(this.getOwner(), this.getX(), this.getY(), this.getZ(), (2F+Getdifficulty()*0.5f)*damage,
                Level.ExplosionInteraction.BLOCK);
    }
}
