package com.net.wenwen.damage;


import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ModDamageSources {

    private final Level level;

    public ModDamageSources(Level level) {
        this.level = level;
    }

    public Holder<DamageType> getTrueDamageType() {
        ResourceLocation location = new ResourceLocation("wenwen", "true_damage");
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, location);
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    public DamageSource trueDamage() {
        return new DamageSource(getTrueDamageType());
    }

    public DamageSource trueDamage(LivingEntity source) {
        return new DamageSource(getTrueDamageType(), source);
    }

    public Holder<DamageType> getBeheadDamageType() {
        ResourceLocation location = new ResourceLocation("wenwen", "behead_damage");
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, location);

        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    public static boolean isBeheadDamageSimple(DamageSource source) {
        return "wenwen.behead_damage".equals(source.getMsgId());
    }
    public DamageSource beheadDamage() {
        return new DamageSource(getBeheadDamageType());
    }

    public DamageSource beheadDamage(LivingEntity source) {
        return new DamageSource(getBeheadDamageType(), source);
    }
}

