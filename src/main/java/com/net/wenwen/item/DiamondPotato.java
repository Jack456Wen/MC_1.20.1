package com.net.wenwen.item;

import com.net.wenwen.init.WenwenModMobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DiamondPotato extends Item {
    public DiamondPotato() {
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
        list.add(Component.literal("§e获得霸体效果，免疫一切负面效果"));
        list.add(Component.literal("§e立即获得40级经验。若拥有【马铃薯领域】，效果翻倍！"));
        list.add(Component.empty());
        list.add(Component.literal("§a马铃薯领域:"));
        /*list.add(Component.literal("§a你将不断汲取经验之力,经验涌动的速度，既取决于效果本身的强度，也与你周围马铃薯的数量息息相关。"));
        list.add(Component.empty());
        list.add(Component.literal("§b它的外表与普通马铃薯相似，但表皮呈现出微弱的钻石光泽，切开后内部结构坚硬如钻石，并含有高浓度的矿物精华。这种马铃薯不仅是食物，更是一种战略资源和神秘力量的象征"));*/
        list.add(Component.literal("§a你将不断汲取经验之力！当你拥有此效果后，通过附魔马铃薯获得的经验数量会大幅度提升！"));

    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = super.finishUsingItem(itemstack, world, entity);

        if (entity instanceof Player _player){
            //判断有没有马铃薯领域
            MobEffect potatoEffect = WenwenModMobEffects.POTATO_EFFECT.get();
            MobEffectInstance currentEffectInstance = _player.getEffect(potatoEffect);
            if(currentEffectInstance!=null){
                _player.giveExperienceLevels(80);
            }
            else{
                _player.giveExperienceLevels(40);
            }
        }
        execute(entity);
        return retval;
    }
    private void execute(Entity entity) {
        if (entity == null || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        if (!livingEntity.level().isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(WenwenModMobEffects.HUJIA_EFFECT.get(), 1200, 4));
            livingEntity.addEffect(new MobEffectInstance(WenwenModMobEffects.BATI_EFFECT.get(), 1200, 0, false, true));
            MobEffect potatoEffect = WenwenModMobEffects.POTATO_EFFECT.get();
            // 检查实体是否已经有这个效果
            if (livingEntity.hasEffect(potatoEffect)) {
                // --- 情况一：玩家身上已经有这个效果 ---
                // 获取当前的效果实例
                MobEffectInstance currentEffectInstance = livingEntity.getEffect(potatoEffect);
                // 获取当前效果的等级 (0 代表 I 级, 1 代表 II 级, 以此类推)
                int currentAmplifier = currentEffectInstance.getAmplifier();
                // 获取当前效果的剩余持续时间
                int currentDuration = currentEffectInstance.getDuration();
                // 计算新的等级 (在原有基础上 +1)
                int newAmplifier = currentAmplifier + 1;
                // 为了防止等级过高导致游戏崩溃或出现意外行为，可以设置一个最大等级上限
                // 例如，最高设置为 IV 级 (amplifier = 3)
                int MAX_AMPLIFIER = 255;
                if (newAmplifier > MAX_AMPLIFIER) {
                    newAmplifier = MAX_AMPLIFIER; // 如果超过上限，就保持在上限
                }
                // 移除旧的效果
                livingEntity.removeEffect(potatoEffect);
                livingEntity.addEffect(new MobEffectInstance(potatoEffect, currentDuration+500, newAmplifier, false, true));

            } else {
                livingEntity.addEffect(new MobEffectInstance(potatoEffect, 800, 0, false, true));
            }
        }
    }
}
