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

public class EnmelonItem extends Item {
    public EnmelonItem() {
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
        list.add(Component.literal("罕见的大金瓜，有什么神奇的效果呢？"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 8000, 1, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 4, false, true));
        entity.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), 150, 0, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 9000, 0, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 9000, 0, false, true));
        return retval;
    }
}
