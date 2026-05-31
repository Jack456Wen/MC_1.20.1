package com.net.wenwen.init;

import com.net.wenwen.WenwenMod;
import com.net.wenwen.potion.SpelunkerMobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WenwenModPotions {
    public static final DeferredRegister<Potion> REGISTRY =
            DeferredRegister.create(ForgeRegistries.POTIONS, WenwenMod.MODID);

    // 注册 Spelunker 药水
    public static final RegistryObject<Potion> SPELUNKER = REGISTRY.register("spelunker",
            () -> new Potion(new MobEffectInstance(WenwenModMobEffects.SPELUNKER.get(), 1800)));

    // 注册 Spelunker 药水的延长版本
    public static final RegistryObject<Potion> LONG_SPELUNKER = REGISTRY.register("long_spelunker",
            () -> new Potion(new MobEffectInstance(WenwenModMobEffects.SPELUNKER.get(), 3600)));

}
