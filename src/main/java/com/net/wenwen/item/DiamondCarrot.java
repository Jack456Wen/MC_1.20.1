package com.net.wenwen.item;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DiamondCarrot extends Item {
    public DiamondCarrot() {
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
        list.add(Component.literal("§a受到的治疗效果翻倍！"));
        list.add(Component.literal("§b变身钻石人，获得高额护甲与反伤效果"));
        list.add(Component.literal("§c短暂获得【吸血】效果，攻击生物将治疗自己"));

    }


    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1000, 4));
        entity.addEffect(new MobEffectInstance(WenwenModMobEffects.XiXue.get(), 1200, 0, false, true));
        entity.addEffect(new MobEffectInstance(WenwenModMobEffects.HEAL_EFFECT.get(), 1200, 0, false, true));
        entity.addEffect(new MobEffectInstance(WenwenModMobEffects.HUJIA_EFFECT.get(), 1200, 9, false, true));
        entity.addEffect(new MobEffectInstance(WenwenModMobEffects.FANSHANG_EFFECT.get(), 1200, 9, false, true));
        return retval;
    }
}
