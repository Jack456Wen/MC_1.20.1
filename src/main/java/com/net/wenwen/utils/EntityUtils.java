package com.net.wenwen.utils;

import com.net.wenwen.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @Project: CageBox
 * @Author: cnlimiter
 * @CreateTime: 2025/7/6 00:54
 * @Note:
 */
public class EntityUtils {
    // Self-explanatory
    public static EntityType<?> getEntityTypeFromTag(CompoundTag nbt, @Nullable EntityType<?> alt) {
        if (nbt != null && nbt.contains("EntityTag", 10)) {
            CompoundTag entityNBT = nbt.getCompound("EntityTag");
            if (entityNBT.contains("id", 8)) {
                return EntityType.byString(entityNBT.getString("id")).orElse(alt);
            }
        }
        return alt;
    }

    /**
     * 计算总战力 (调用这个方法即可)
     */
    public static double calculateTotalPower(Player player) {
        // 1. 基础防御生存分 (占比约 60%)
        double survivalScore = calculateSurvivalScore(player);

        // 2. 基础输出分 (占比约 40%)
        double offenseScore = calculateOffenseScore(player);

        double curioScore = calculateCurioScore(player);

        return survivalScore + offenseScore+curioScore;
    }

    /**
     * 高性能获取指定范围内最近的玩家
     *
     * @param monster 触发搜索的怪物（以它为中心）
     * @param range   搜索半径 (比如 64.0)
     * @return 最近的玩家，如果没有则返回 null
     */
    public static Player getNearestPlayer(LivingEntity monster, double range) {
        Level level = monster.level();

        // 【性能核心】构建一个轴对齐边界框
        // 怪物坐标加减范围值，直接锁定一个正方体空间，跳过外部所有实体
        AABB searchBox = monster.getBoundingBox().inflate(range);

        // 【性能核心】利用底层空间树，只获取这个框里的玩家
        // 这里的开销极小，不会遍历怪物，只遍历玩家
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);

        if (nearbyPlayers.isEmpty()) {
            return null;
        }

        // 预设一个极大的距离
        double closestDistanceSq = Double.MAX_VALUE;
        Player closestPlayer = null;

        // 【优化细节】不要用 distanceTo()，那个方法内部会做开方运算，非常耗性能。
        // 比较距离的平方就够了，数学结果是一样的。
        Vec3 monsterPos = monster.position();

        for (Player player : nearbyPlayers) {
            double distSq = monsterPos.distanceToSqr(player.position());
            if (distSq < closestDistanceSq) {
                closestDistanceSq = distSq;
                closestPlayer = player;
            }
        }

        // 【极其重要的防错机制】
        // AABB 是正方体，但你要的是 64格 "球体" 范围。
        // 正方体的对角线距离是 64 * 1.732 ≈ 110 格！
        // 必须用真正的距离再卡一次，否则怪物会在对角线死角外偷偷生成强化
        if (closestDistanceSq > range * range) {
            return null;
        }

        return closestPlayer;
    }

    /**
     * 计算“生存防御”方面的分数
     */
    private static double calculateSurvivalScore(Player player) {
        double score = 0;

        // 1. 有效生命值 (EHP - Effective Health Pool 的概念)
        // 玩家实际能抗多少伤害 = 最大生命值 * (1 + 护甲值 / 5)
        // 原版玩家20血，10点护甲，EHP是 20 * 3 = 60
        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        double armor = player.getAttributeValue(Attributes.ARMOR);
        double armorToughness = player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);

        // 简化的 EHP 计算公式，考虑了护甲韧性
        double ehp = maxHealth * (1.0 + (armor + armorToughness * 0.4) / 5.0);
        score += ehp * 1.5; // 每点有效生命值算 1.5 分

        // 2. 击退抗性 (防被怪打下悬崖，非常影响生存)
        double knockbackRes = player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        score += knockbackRes * 50; // 满抗性加 50 分

        // 3. 特殊保命道具判定
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) {
            score += 100; // 图腾极大的提升了容错率，给高分
        } else if (offhand.is(Items.SHIELD)) {
            score += 40;  // 盾牌挡伤能力极强
        }

        return score;
    }

    /**
     * 计算“输出伤害”方面的分数
     */
    private static double calculateOffenseScore(Player player) {
        double score = 0;

        // 1. 面板攻击力 (包含武器基础伤害 + 附魔)
        // 原版赤手空拳是 1.0，下界合金剑带锋利5大约是 11.0
        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        score += attackDamage * 5; // 每点攻击力算 5 分

        // 2. 攻击速度 (攻速快意味着DPS高，但不需要给太高的权重)
        double attackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
        // 原版基础是 4.0，如果变快了（比如 5.0），额外加分
        if (attackSpeed > 4.0) {
            score += (attackSpeed - 4.0) * 15;
        }

        // 3. 移动速度 (优秀的走位等于防御和输出)
        double moveSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        // 原版疾跑是 0.13 左右，如果穿了迅捷靴子会变快
        if (moveSpeed > 0.13) {
            score += (moveSpeed - 0.13) * 500;
        }

        return score;
    }

    private static double calculateCurioScore(Player player){
        int num=CuriosHelper.getTotalCuriosCount(player);
        double score = 0;
        score+=num*80f;
        return score;
    }


    // This function builds a full tooltip containing Custom Name, EntityType, Gender and Scientific name (if available)
    public static void buildTooltipData(ItemStack stack, List<Component> tooltip, EntityType<?> entity, String path) {
        if (stack.getTag() != null && stack.getTag().contains("EntityTag")) {
            CompoundTag compound = stack.getTagElement("EntityTag");
            //String component = "mobspawn.tooltip." + (compound.contains("Gender") ? (compound.getInt("Gender") == 0 ? "male" : "female") : "unknown");
            //tooltip.add(Component.translatable(component).mergeStyle(TextFormatting.GRAY));
            String gender = compound.contains("Gender") ? Component.translatable("mobspawn.tooltip." + (compound.getInt("Gender") == 0 ? "male" : "female")).getString() + " " : "";
            String type;
            if (path.isEmpty())
                type = Component.translatable(entity.getDescriptionId()).getString();
            else
                type = Component.translatable(entity.getDescriptionId() + "_" + path).getString();
            if (stack.getTag().getCompound("EntityTag").contains("CustomName")) {
                String customName = stack.getTag().getCompound("EntityTag").getString("CustomName");
                // Entity uses ITextComponent.Serializer.getComponentFromJson(s) instead of substrings
                tooltip.add(Component.literal(customName.substring(9, customName.length() - 2) + " (" + gender + type + ")").withStyle(ChatFormatting.GRAY));
            }
            else {
                tooltip.add(Component.literal(gender + type).withStyle(ChatFormatting.GRAY));
            }
        }
        if (Config.scientificNames) {
            String scipath = path.isEmpty() ? "" : "_" + path;
            var tooltipText = Component.translatable(entity.getDescriptionId() + scipath + ".sciname");
            if (!tooltipText.getString().contains(".")) {
                tooltip.add(tooltipText.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            }
        }
    }

    public static CompoundTag writeEntityToNBT(LivingEntity entity) {
        return writeEntityToNBT(entity, false);
    }

    public static CompoundTag writeEntityToNBT(LivingEntity entity, boolean keepHomeData) {
        return writeEntityToNBT(entity, keepHomeData, false);
    }

    // This method writes this entity into a CompoundTag Tag
    public static CompoundTag writeEntityToNBT(LivingEntity entity, boolean keepHomeData, boolean attachModelData) {
        CompoundTag baseTag = new CompoundTag();
        CompoundTag entityTag = new CompoundTag();
        entity.saveAsPassenger(entityTag);
        entityTag.remove("Pos"); // Remove the Position from the NBT data, as it would fuck things up later on
        entityTag.remove("Motion");
        if (entityTag.contains("BoundingBox")) {
            entityTag.remove("BoundingBox"); // Stripping this NBT data prevents RandomPatches from moving mobs back to their original position
        }
        if (entityTag.contains("Leash")) {
            entityTag.remove("Leash"); // Stripping this NBT data prevents Leash duplication from caging/catching Leashed mobs
        }
//        if (entity instanceof ISpecies && !keepHomeData) {
//            entityTag.remove("HomePosX");
//            entityTag.remove("HomePosY");
//            entityTag.remove("HomePosZ");
//        }
//        if (attachModelData && entity instanceof ComplexMob) {
//            baseTag.putInt("CustomModelData", ((ComplexMob) entity).getVariant());
//        }
        baseTag.put("EntityTag", entityTag); // Put the entity in the Tag
        return baseTag;
    }
}
