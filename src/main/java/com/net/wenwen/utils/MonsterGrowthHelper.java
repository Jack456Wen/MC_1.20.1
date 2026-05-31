package com.net.wenwen.utils;

/**
 * 怪物成长比例计算器
 * 负责将玩家的战力转化为怪物的血量和伤害倍率
 */
public class MonsterGrowthHelper {
    // 在重度魔改包里，安全线也要提高（大概等于穿了一套好点的初级模组装备）
    private static final double SAFE_POWER_THRESHOLD = 120.0;

    private static final double MAX_HEALTH_MULTIPLIER = 500.0;

    private static final double MAX_DAMAGE_MULTIPLIER = 80.0;

    public static float getHealthMultiplier(double playerPower) {
        if (playerPower <= SAFE_POWER_THRESHOLD) {
            return 1.0f;
        }

        double excessPower = playerPower - SAFE_POWER_THRESHOLD;

        double multiplier = 1.0 + Math.pow(excessPower / 125.0, 1.5);

        if (multiplier > MAX_HEALTH_MULTIPLIER) {
            multiplier = MAX_HEALTH_MULTIPLIER;
        }

        return (float) multiplier;
    }

    public static float getDamageMultiplier(double playerPower) {
        if (playerPower <= SAFE_POWER_THRESHOLD) {
            return 1.0f;
        }

        double excessPower = playerPower - SAFE_POWER_THRESHOLD;

        // 伤害增长依然要比血量慢得多（防止被秒杀原则永不变）
        double multiplier = 1.0 + Math.pow(excessPower / 450.0, 1.2);

        if (multiplier > MAX_DAMAGE_MULTIPLIER) {
            multiplier = MAX_DAMAGE_MULTIPLIER;
        }

        return (float) multiplier;
    }
}
