package com.net.wenwen.arrrows;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ExplossiveArrow extends Arrow {

    // 必须保留这个构造函数供 Minecraft 内部实例化使用
    public ExplossiveArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }
    public ExplossiveArrow(Level level, double x, double y, double z) {
        super(level, x, y, z); // 调用原版 Arrow 自带的坐标构造器
    }
    public ExplossiveArrow(EntityType<? extends Arrow> entityType, Level level, Arrow originalArrow) {
        super(entityType, level);
        this.moveTo(originalArrow.getX(), originalArrow.getY(), originalArrow.getZ(), originalArrow.getYRot(), originalArrow.getXRot());
        // 复制基础物理状态
        this.setBaseDamage(originalArrow.getBaseDamage());
        this.setKnockback(originalArrow.getKnockback());
        this.setOwner(originalArrow.getOwner());
        this.setCritArrow(originalArrow.isCritArrow());
        // 复制运动向量（速度方向，极其重要！）
        this.setDeltaMovement(originalArrow.getDeltaMovement());
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            // 参数说明：产生爆炸的实体, x, y, z, 爆炸威力 是否引发火灾, 爆炸模式(NONE不破坏方块)
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.7F*this.level().getDifficulty().getId(), false, Level.ExplosionInteraction.BLOCK);
        }

        super.onHit(result);
        this.discard();
    }
}
