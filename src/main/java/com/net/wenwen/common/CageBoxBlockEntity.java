package com.net.wenwen.common;

import com.net.wenwen.Config;
import com.net.wenwen.Const;
import com.net.wenwen.init.Registration;
import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.utils.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
/**
 * @Project: CageBox
 * @Author: cnlimiter
 * @CreateTime: 2025/7/6 00:52
 * @Note:
 */
public class CageBoxBlockEntity extends BlockEntity {

    private CompoundTag data;
    private boolean locked;

    public CageBoxBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.TRAP_CAGE_TILE.get(), pos, state);
    }

    public static boolean isBlacklisted(Entity entity) {
        return entity.getType().is(Registration.EntityTags.CAGE_BLACKLIST);
    }

    public boolean cageEntity(Mob entity) {
        if (!this.isLocked()) {
            if (!isBlacklisted(entity) && (Config.easyMobCapturing || entity.getTarget() == null)) {
                this.setTagCompound(EntityUtils.writeEntityToNBT(entity));
                this.setLocked(true);
                entity.discard();
                setChanged();
                return true;
            }
        }
        return false;
    }

    public boolean spawnCagedCreature(ServerLevel worldIn, BlockPos pos, boolean offsetHitbox) {
        if (!worldIn.isClientSide && this.isLocked()) {
            EntityType<?> entity = EntityUtils.getEntityTypeFromTag(this.getTagCompound(), null);
            if (entity != null) {
                if (worldIn.noCollision(entity.getAABB(pos.getX() + 0.5F, pos.getY() - (offsetHitbox ? entity.getHeight() + 1.2F : 0), pos.getZ() + 0.5F))) {
                    if (worldIn.getEntity(this.data.getCompound("EntityTag").getUUID("UUID")) != null) {
                        this.data.getCompound("EntityTag").putUUID("UUID", Mth.createInsecureUUID(worldIn.random));
                    }
                    Entity caged_entity = entity.create(worldIn, this.data, null, pos, MobSpawnType.DISPENSER, true, !Objects.equals(pos, this.getBlockPos()));
                    if (caged_entity != null) {
                        caged_entity.moveTo(pos.getX() + 0.5F, pos.getY() - (offsetHitbox ? caged_entity.getBbHeight() + 1.2 : 0.8), pos.getZ() + 0.5F, Mth.wrapDegrees(worldIn.random.nextFloat() * 360.0F), 0.0F);
                        if (!worldIn.tryAddFreshEntityWithPassengers(caged_entity)) {
                            caged_entity.setUUID(Mth.createInsecureUUID(worldIn.random));
                            worldIn.addFreshEntityWithPassengers(caged_entity);
                        }
                        this.setTagCompound(null);
                        this.setLocked(true);
                        //添加无敌效果
                        if(caged_entity instanceof LivingEntity e){
                            e.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), 60, 0,false, false));
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public CompoundTag getTagCompound() { return this.data; }

    public void setTagCompound(@Nullable CompoundTag nbt) { this.data = nbt; }

    public boolean hasTagCompound() { return this.data != null; }

    public boolean isLocked() { return this.locked; }

    public void setLocked(boolean locked) { this.locked = locked; }

    @Override
    public void load(@NotNull CompoundTag compound) {
        super.load(compound);
        this.setTagCompound(compound.copy());
        this.setLocked(compound.getBoolean("closed"));
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("closed", this.isLocked());
        if (this.getTagCompound() != null) {
            compound.put("EntityTag", this.getTagCompound().getCompound("EntityTag"));
        }
    }
}
