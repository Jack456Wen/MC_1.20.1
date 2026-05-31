
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.net.wenwen.init;

import com.net.wenwen.curios.DarknessHeart;
import com.net.wenwen.curios.HeartAmulet;
import com.net.wenwen.entity.ModEntities;
import com.net.wenwen.item.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import com.net.wenwen.WenwenMod;

public class WenwenModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, WenwenMod.MODID);
	public static final RegistryObject<Item> GOLD_ENARADISH = REGISTRY.register("gold_carrot", () -> new GoldEnaradishItem());
	public static final RegistryObject<Item> GOLD_POTATO = REGISTRY.register("gold_potato", () -> new GoldPotatoItem());
	public static final RegistryObject<Item> GOLD_EN_POTATO = REGISTRY.register("gold_en_potato", () -> new GoldEnPotatoItem());
	public static final RegistryObject<Item> HEARTCONTAINER = REGISTRY.register("heartcontainer", () -> new HeartcontainerItem());
	public static final RegistryObject<Item> HEART_PIECE = REGISTRY.register("heart_piece", () -> new HeartPieceItem());
	public static final RegistryObject<Item> Break = REGISTRY.register("totem_break", () -> new totem_break());
	public static final RegistryObject<Item> MIRROR = REGISTRY.register("magic_mirror", () -> new MagicMirrorItem());
	public static final RegistryObject<Item> ENMELON = REGISTRY.register("enmelon", () -> new EnmelonItem());
	public static final RegistryObject<Item> DIAMOND_POTATO = REGISTRY.register("diamond_potato", () -> new DiamondPotato());
	public static final RegistryObject<Item> DIAMOND_CARROT = REGISTRY.register("diamond_carrot", () -> new DiamondCarrot());

	public static final RegistryObject<Item> HEART_AMULET = REGISTRY.register("heart_amulet", () -> new HeartAmulet());
	public static final RegistryObject<Item> DARKNESS = REGISTRY.register("darkness_heart", () -> new DarknessHeart());
	public static final RegistryObject<Item> Tears = REGISTRY.register("tears", () -> new TearsItem());
	public static final RegistryObject<Item> Key = REGISTRY.register("chest_key", () -> new ChestKeyItem());

	public static final RegistryObject<Item> BAT_SPAWN_EGG = REGISTRY.register("bat_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.HOSTILE_BAT, -12630232, -5658199,
					new Item.Properties()));

	public static final RegistryObject<Item> OCTOROK_SPAWN_EGG = REGISTRY.register("octorok_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.OCTOROK, -8388480, -16777216,
					new Item.Properties()));

}
