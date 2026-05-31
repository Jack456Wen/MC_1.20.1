
package com.net.wenwen.item;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
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
import java.util.Random;

public class GoldEnPotatoItem extends Item {
	public GoldEnPotatoItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC).food((new FoodProperties.Builder()).nutrition(10).saturationMod(0.3f).alwaysEat().build()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, level, list, flag);
		list.add(Component.literal("\u5403\u4E0B\u83B7\u5F97\u5927\u91CF\u7ECF\u9A8C\u548C\u795E\u79D8\u6548\u679C"));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		entity.addEffect(new MobEffectInstance(WenwenModMobEffects.BATI_EFFECT.get(), 200, 0, false, true));
		entity.addEffect(new MobEffectInstance(WenwenModMobEffects.HUJIA_EFFECT.get(), 200, 2));
		entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 3));
		entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 4));
		if (entity instanceof Player _player){
			//判断有没有马铃薯领域
			MobEffect potatoEffect = WenwenModMobEffects.POTATO_EFFECT.get();
			MobEffectInstance currentEffectInstance = _player.getEffect(potatoEffect);
			int level=1;
			if(currentEffectInstance!=null){
				level= currentEffectInstance.getAmplifier()+1;
			}
			if(_player.experienceLevel>100){
				_player.giveExperienceLevels(Math.max((level*12),(2000/_player.experienceLevel)));
			}
			else
			{
				int randomNumber = 20+(level*4);
				_player.giveExperienceLevels(randomNumber);
			}
		}
		return retval;
	}
}
