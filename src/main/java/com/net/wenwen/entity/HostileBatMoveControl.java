package com.net.wenwen.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HostileBatMoveControl extends MoveControl {
    // 统一的时间累加器，用于平滑的正弦波计算，不要用随机倒计时
    private float orbitTime = 0.0F;

    public HostileBatMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        // 1. 没有指令或没目标，平滑减速
        if (this.operation == Operation.WAIT || this.mob.getTarget() == null) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.85D));
            return;
        }

        // 2. 只处理 MOVE_TO
        if (this.operation == Operation.MOVE_TO) {
            // 递增全局时间，保证正弦波是连续平滑的，不会突变
            this.orbitTime += 0.05F;

            // --- 步骤 A：计算带扰动的“虚拟目标点” ---
            // 注意：这里我们基于【指向真实目标的角度】做扰动，而不是基于蝙蝠自身的视角
            double dxRaw = this.wantedX - this.mob.getX();
            double dzRaw = this.wantedZ - this.mob.getZ();
            double rawAngleToTarget = Math.atan2(dzRaw, dxRaw);

            // 加上一个垂直于目标方向的偏移量，实现绕圈效果 (幅度 1.5 格)
            double orbitOffset = Math.sin(this.orbitTime * 2.0F) * 1.5D;
            double perpendicularAngle = rawAngleToTarget + (Math.PI / 2.0F);

            double finalTargetX = this.wantedX + (Math.cos(perpendicularAngle) * orbitOffset);
            double finalTargetY = this.wantedY;
            double finalTargetZ = this.wantedZ + (Math.sin(perpendicularAngle) * orbitOffset);


            // --- 步骤 B：计算指向“虚拟目标点”的向量和距离 ---
            double dx = finalTargetX - this.mob.getX();
            double dy = finalTargetY - this.mob.getY();
            double dz = finalTargetZ - this.mob.getZ();
            double distSqr = dx * dx + dy * dy + dz * dz;
            double dist = Math.sqrt(distSqr);

            // --- 步骤 C：平滑转向 ---
            float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            // 根据距离调整转向速度：远距离转得慢（画大弧线），近距离转得快（灵活走位）
            float turnSpeed = dist < 5.0F ? 25.0F : 12.0F;
            this.mob.setYRot(Mth.approachDegrees(this.mob.getYRot(), targetYaw, turnSpeed));
            this.mob.yBodyRot = this.mob.getYRot();


            double speedAttribute = this.mob.getAttributeValue(Attributes.FLYING_SPEED);

            // 稀释系数：距离越近，速度越慢，防止在玩家头顶疯狂穿模震荡
            float speedModifier = dist < 2.0F ? 0.3F : (dist < 5.0F ? 0.6F : 1.0F);
            double finalSpeed = speedAttribute * speedModifier * 0.7D; // 0.7是手感微调系数

            if (distSqr < 1.5D) {
                // 极近距离：几乎悬停，只给一点点上下浮动的推力
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().x * 0.5D, 0.02D, this.mob.getDeltaMovement().z * 0.5D);
                return;
            }

            // 归一化方向向量，并乘以最终速度
            double motionX = (dx / dist) * finalSpeed;
            double motionY = (dy / dist) * finalSpeed;
            double motionZ = (dz / dist) * finalSpeed;

            // 将计算出的速度平滑应用到实体上 (0.2 是平滑过渡系数，越大越生硬)
            this.mob.setDeltaMovement(
                    this.mob.getDeltaMovement().x + (motionX - this.mob.getDeltaMovement().x) * 0.2D,
                    this.mob.getDeltaMovement().y + (motionY - this.mob.getDeltaMovement().y) * 0.2D,
                    this.mob.getDeltaMovement().z + (motionZ - this.mob.getDeltaMovement().z) * 0.2D
            );
        }
    }
}
