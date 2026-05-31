
/*
*	MCreator note: This file will be REGENERATED on each build.
*/
package com.net.wenwen.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.common.BasicItemListing;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.npc.VillagerProfession;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WenwenModTrades {
	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event) {
		if (event.getType() == VillagerProfession.ARMORER) {
			event.getTrades().get(1).add(new BasicItemListing(new ItemStack(Items.EMERALD, 24), new ItemStack(Items.DIAMOND, 4), new ItemStack(WenwenModItems.HEART_PIECE.get()), 10, 20, 0.05f));
			event.getTrades().get(5).add(new BasicItemListing(new ItemStack(Items.EMERALD, 64), new ItemStack(Items.DIAMOND, 12), new ItemStack(WenwenModItems.HEARTCONTAINER.get()), 2, 20, 0.05f));
			event.getTrades().get(4).add(new BasicItemListing(new ItemStack(Blocks.EMERALD_BLOCK, 15), new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(WenwenModItems.HEARTCONTAINER.get()), 1, 100, 0.05f));
		}
		if (event.getType() == VillagerProfession.WEAPONSMITH) {
			event.getTrades().get(2).add(new BasicItemListing(new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.DIAMOND_SWORD), new ItemStack(WenwenModItems.HEART_PIECE.get()), 5, 50, 0.05f));
		}
	}
}
