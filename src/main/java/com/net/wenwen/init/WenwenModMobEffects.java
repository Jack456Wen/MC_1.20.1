
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package com.net.wenwen.init;

import com.net.wenwen.item.DiamondPotato;
import com.net.wenwen.potion.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import com.net.wenwen.WenwenMod;
import com.net.wenwen.potion.SpelunkerMobEffect;

public class WenwenModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, WenwenMod.MODID);
	public static final RegistryObject<MobEffect> BUSI = REGISTRY.register("busi", () -> new BusiMobEffect());
	public static final RegistryObject<MobEffect> HEART_BUFF_01 = REGISTRY.register("heart_buff_01", () -> new HeartBuff01MobEffect());
	public static final RegistryObject<MobEffect> MIRROR_EFFECT = REGISTRY.register("mirror", () -> new MirrorEffectMob());
	public static final RegistryObject<MobEffect> POTATO_EFFECT = REGISTRY.register("diamond_potato", () -> new PotatoEffectMob());
	public static final RegistryObject<MobEffect> DEHP_EFFECT = REGISTRY.register("dehp", () -> new DehpEffect());
	public static final RegistryObject<MobEffect> SUILIE_EFFECT = REGISTRY.register("suilie", () -> new SuilieEffect());
	public static final RegistryObject<MobEffect> HUJIA_EFFECT = REGISTRY.register("hujia", () -> new HujiaEffect());
	public static final RegistryObject<MobEffect> MIANSHANG_EFFECT = REGISTRY.register("mianshang", () -> new MianshangEffect());
	public static final RegistryObject<MobEffect> HEAL_EFFECT = REGISTRY.register("heal", () -> new HealEffect());

	public static final RegistryObject<MobEffect> NOMOVE_EFFECT = REGISTRY.register("nomove", () -> new NoMoveEffect());
	public static final RegistryObject<MobEffect> NOATTACK_EFFECT = REGISTRY.register("jiaoxie", () -> new NoAttackEffect());
	public static final RegistryObject<MobEffect> DAMAGE_EFFECT = REGISTRY.register("damage", () -> new DamageEffect());
	public static final RegistryObject<MobEffect> FANSHANG_EFFECT = REGISTRY.register("fanshang", () -> new FanShangEffect());
	public static final RegistryObject<MobEffect> ATTACKUP_EFFECT = REGISTRY.register("attackup", () -> new AttackUpEffect());
	public static final RegistryObject<MobEffect> NOPLAYER_EFFECT = REGISTRY.register("noplayer", () -> new NoPlayerEffect());
	public static final RegistryObject<MobEffect> BATI_EFFECT = REGISTRY.register("bati", () -> new BatiEffect());
	public static final RegistryObject<MobEffect> RANGE = REGISTRY.register("range", () -> new RangeEffect());
	public static final RegistryObject<MobEffect> Fear = REGISTRY.register("fear", () -> new FearEffect());
	public static final RegistryObject<MobEffect> BeHead = REGISTRY.register("behead", () -> new BeHeadEffect());
	public static final RegistryObject<MobEffect> XiXue = REGISTRY.register("xixue", () -> new XixueMobEffect());
	public static final RegistryObject<MobEffect> SPELUNKER = REGISTRY.register("spelunker", () -> new SpelunkerMobEffect());
}
