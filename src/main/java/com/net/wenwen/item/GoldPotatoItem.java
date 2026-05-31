
package com.net.wenwen.item;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;


import java.util.List;

public class GoldPotatoItem extends Item {
	public GoldPotatoItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON).food((new FoodProperties.Builder()).nutrition(10).saturationMod(0.3f).alwaysEat().build()));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, level, list, flag);
		list.add(Component.literal("\u5403\u4E0B\u83B7\u5F97\u5C11\u91CF\u7ECF\u9A8C\u548C\u795E\u79D8\u6548\u679C"));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 3));
		if (entity instanceof Player _player)
		{
			MobEffect potatoEffect = WenwenModMobEffects.POTATO_EFFECT.get();
			MobEffectInstance currentEffectInstance = _player.getEffect(potatoEffect);
			int level=1;
			if(currentEffectInstance!=null){
				level= currentEffectInstance.getAmplifier()+1;
			}
			_player.giveExperiencePoints(860*level);
		}
		return retval;
	}
}
