
package com.net.wenwen.item;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;


import java.util.List;

public class GoldEnaradishItem extends Item {
	public GoldEnaradishItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC).food((new FoodProperties.Builder()).nutrition(20).saturationMod(1f).alwaysEat().build()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, level, list, flag);
		list.add(Component.literal("\u5403\u4E0B\u53EF\u83B7\u5F97\u795E\u5947\u6548\u679C"));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		entity.addEffect(new MobEffectInstance(WenwenModMobEffects.MIANSHANG_EFFECT.get(), 1200, 3));
		entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1000, 4));
		entity.addEffect(new MobEffectInstance(WenwenModMobEffects.ATTACKUP_EFFECT.get(), 1200, 0));
		entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 3000, 4));
		return retval;
	}
}
