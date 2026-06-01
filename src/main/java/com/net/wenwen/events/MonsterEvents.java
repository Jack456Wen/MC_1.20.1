package com.net.wenwen.events;

import com.net.wenwen.Config;
import com.net.wenwen.common.WorldState;
import com.net.wenwen.common.WorldStateManager;
import com.net.wenwen.init.WenwenModSounds;
import com.net.wenwen.utils.EntityUtils;
import com.net.wenwen.utils.MonsterGrowthHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;


@Mod.EventBusSubscriber
public class MonsterEvents
{
    private static final UUID HEALTH_MODIFIER_UUID = UUID.randomUUID();
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.randomUUID();
    private static final UUID SPEED_MODIFIER_UUID = UUID.randomUUID();

    private static final Map<UUID, Double> cachedAttackDamage = new HashMap<>();

    private static final long DAY_IN_TICKS = 24000L;

    private static final Map<Difficulty, Map<Long, String>> REMINDER_EVENTS_BY_DIFFICULTY = new HashMap<>();

    static {
        // --- 简单 (EASY) 难度的事件 ---
        Map<Long, String> easyEvents = new HashMap<>();
        easyEvents.put(20L, "受到神秘力量影响：所有怪物开始随着时间强化！");
        easyEvents.put(25L, "由于世界积累大量负向能量：所有怪物大幅度强化！");
        easyEvents.put(100L, "已查明：力量来源为遥远的冰冻深海：所有怪物进一步强化！");
        REMINDER_EVENTS_BY_DIFFICULTY.put(Difficulty.EASY, easyEvents);

        // --- 普通 (NORMAL) 难度的事件 ---
        Map<Long, String> normalEvents = new HashMap<>();
        normalEvents.put(12L, "受到神秘力量影响：所有怪物开始随着时间强化！");
        normalEvents.put(20L, "由于世界积累大量负向能量：所有怪物大幅度强化！");
        normalEvents.put(100L, "已查明：力量来源为遥远的冰冻深海：所有怪物进一步强化！");
        REMINDER_EVENTS_BY_DIFFICULTY.put(Difficulty.NORMAL, normalEvents);

        // --- 困难 (HARD) 难度的事件 ---
        Map<Long, String> hardEvents = new HashMap<>();
        hardEvents.put(7L, "受到神秘力量影响：所有怪物开始随着时间强化！");
        hardEvents.put(20L, "力量来源为遥远的冰冻深海,所有怪物大幅度强化！");
        hardEvents.put(100L, "终焉：深海君主本体苏醒！所有怪物进一步强化！这是最后的生存之战！");
        REMINDER_EVENTS_BY_DIFFICULTY.put(Difficulty.HARD, hardEvents);
    }

    // 保存上一个检查过的天数，避免重复计算
    private static long lastCheckedDay = -1;
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 60;

    private static int day=5;
    private static void sendMessageToWorld(MinecraftServer level, String str) {
        for (ServerPlayer p : level.getPlayerList().getPlayers()) {
            net.minecraft.network.chat.Component message = Component.literal(str).withStyle(ChatFormatting.AQUA);
            p.displayClientMessage(message, true);
            p.sendSystemMessage(message);
            p.level().playSound(null, p.getX(), p.getY(), p.getZ(), WenwenModSounds.LEVELUP_BELL.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    /**
     *
     * @param monster 怪物实体
     * @param multiplier 你期望的“最终倍率”（比如传 2.0 就是变成原来的 2 倍）
     * @param attri 属性实例
     * @param type 是加血还是加伤害（用来决定用哪个 UUID）
     */
    private static void applyModifier(LivingEntity monster, double multiplier, AttributeInstance attri, String type) {

        // 1. 安全检查：如果倍率是 1.0（不增强），直接跳过，节省性能
        if (multiplier <= 1.0f) return;

        double correctedValue = multiplier - 1.0;

        // 3. 根据类型选择不同的固定 UUID
        UUID uuidToUse;
        if ("health".equals(type)) {
            uuidToUse = HEALTH_MODIFIER_UUID;
        }
        else if ("speed".equals(type)) {
            uuidToUse = SPEED_MODIFIER_UUID;
        }
        else {
            uuidToUse = DAMAGE_MODIFIER_UUID;
        }

        // 4. 创建带有固定 UUID 的修饰符
        AttributeModifier modifier = new AttributeModifier(
                uuidToUse,
                "WenWenScalingMobs_" + type,
                correctedValue,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // 5. 添加修饰符
        attri.addTransientModifier(modifier);

        // 6. 如果是修改血量，立刻回满血（如果是修改伤害，这行没意义但也不会报错）
        if ("health".equals(type)) {
            monster.setHealth(monster.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide()) {
            return;
        }
        CreeperPower(event.getEntity());
        if(Config.isepic){
            if(!WorldStateManager.isWorldUp(event.getLevel())){
                return;
            }
            if (event.getEntity() instanceof LivingEntity living && isScalingMob(living))
            {
                if (!living.getPersistentData().getBoolean("PowerScaled")){
                    AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
                    AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
                    AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
                    Player nearestPlayer = EntityUtils.getNearestPlayer(living, 96);
                    if (nearestPlayer != null) {
                        // 1. 从玩家的缓存里读取战力 (O(1) 极速读取，不重新算)
                        double power = cachedAttackDamage.getOrDefault(nearestPlayer.getUUID(), 0.0);

                        // 2. 根据战力获取成长倍率
                        float healthMulti = MonsterGrowthHelper.getHealthMultiplier(power);
                        float damageMulti = MonsterGrowthHelper.getDamageMultiplier(power);

                        // 3. 应用到怪物身上
                        if (healthMulti > 1.0f) {
                            applyModifier(living, healthMulti, maxHealth, "health");
                        }
                        if (damageMulti > 1.0f) {
                            applyModifier(living, damageMulti, damage, "damage");
                        }

                        //速度
                        double Playerspeed = nearestPlayer.getAttributeValue(Attributes.MOVEMENT_SPEED);
                        double Monsterspeed = living.getAttributeValue(Attributes.MOVEMENT_SPEED);
                        applyModifier(living, Playerspeed/Monsterspeed, speed, "speed");
                        living.getPersistentData().putBoolean("PowerScaled", true);
                    }
                }
            }
            return;
        }
        if(!WorldStateManager.isWorldUp(event.getLevel())){
            return;
        }
        Difficulty level=event.getLevel().getDifficulty();
        switch (level){
            case EASY: day=20; break;
            case NORMAL: day=12; break;
            case HARD: day=7; break;
            default: day=100; break;
        }

        if (event.getEntity() instanceof LivingEntity living && isScalingMob(living))
        {
            int currentDay = (int) (event.getLevel().getDayTime() / 24000L);
            if(currentDay<day){
                return;
            }
            if(currentDay>=100){
                currentDay+=200;
            }
            AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
            AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
            AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);

            float currentHealthPercent = living.getHealth() / living.getMaxHealth();
            boolean exponential = Config.getInstance().areStatsExponential();

            if (damage != null)
            {
                double damageRate = Config.getInstance().getMobDamageRate();
                double damageMax = Config.getInstance().getMobDamageMax();
                double baseDamage = Config.getInstance().getMobDamageBase();

                damage.addTransientModifier(new AttributeModifier("ScalingMobs:DamageBase",
                                                                  baseDamage - 1,
                                                                  AttributeModifier.Operation.MULTIPLY_BASE));
                damage.addTransientModifier(new AttributeModifier("ScalingMobs:Damage",
                                                                  Math.min(damageMax - 1, getStatIncrease(damageRate, currentDay-day+1, exponential)),
                                                                  AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            if (maxHealth != null)
            {
                double baseHealth = Config.getInstance().getMobHealthBase();
                double healthRate = Config.getInstance().getMobHealthRate();
                double healthMax = Config.getInstance().getMobHealthMax();

                maxHealth.addTransientModifier(new AttributeModifier("ScalingMobs:HealthBase",
                                                                     baseHealth - 1,
                                                                     AttributeModifier.Operation.MULTIPLY_BASE));
                if(currentDay<day+16){
                    maxHealth.addTransientModifier(new AttributeModifier("ScalingMobs:Health",
                            Math.min(healthMax - 1, getStatIncrease(((currentDay/40f)), currentDay-1, exponential)),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                else{
                    maxHealth.addTransientModifier(new AttributeModifier("ScalingMobs:Health",
                            Math.min(healthMax - 1, getStatIncrease(healthRate, currentDay-day+1, exponential)),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }

                living.setHealth(living.getMaxHealth() * currentHealthPercent);
            }

            if (speed != null)
            {
                double speedRate = Config.getInstance().getMobSpeedRate();
                double speedMax = Config.getInstance().getMobSpeedMax();
                double baseSpeed = Config.getInstance().getMobSpeedBase();

                speed.addTransientModifier(new AttributeModifier("ScalingMobs:SpeedBase", baseSpeed - 1, AttributeModifier.Operation.MULTIPLY_BASE));
                speed.addTransientModifier(new AttributeModifier("ScalingMobs:Speed",
                                                                 Math.min(speedMax - 1, getStatIncrease(speedRate, currentDay-day+1, exponential)),
                                                                 AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }
    private static void CreeperPower(Entity entity)
    {

        // ② 判断是否为普通苦力怕
        if (entity.getType() == EntityType.CREEPER) {
            Creeper creeper = (Creeper) entity;

            // ③ 如果已经是闪电苦力怕了，跳过
            if (creeper.isPowered()) {
                return;
            }
            int level=WorldStateManager.isWorldUp(creeper.level()) ? 2 : 1;
            // ④ 按配置的概率，直接设置 powered 状态为 true
            if (Math.random() <= Config.creeper_chance*level) {
                creeper.getEntityData().set(Creeper.DATA_IS_POWERED,true);
            }
        }
    }
    public static double getStatIncrease(double rate, int day, boolean exponential)
    {
        if (exponential)
        {   return Math.pow(1 + rate, day) - 1;
        }
        else
        {   return (day * rate);
        }
    }
    public static boolean isScalingMob(LivingEntity entity)
    {
        return entity instanceof Monster
            || Config.getInstance().getMobWhitelist().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        cachedAttackDamage.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END){
            return;
        }
        Player player = event.player;

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer)
        {
            if (serverPlayer.tickCount % 120 != 0) return;
            UUID uuid = serverPlayer.getUUID();
            double currentDamage = EntityUtils.calculateTotalPower(serverPlayer,lastCheckedDay);
            Double lastDamage = cachedAttackDamage.get(uuid);

            if (lastDamage == null || currentDamage != lastDamage) {
                // 更新缓存
                cachedAttackDamage.put(uuid, currentDamage);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        ServerLevel world = server.getLevel(Level.OVERWORLD);
        if (world == null) return;
        long currentDay = world.getDayTime() / DAY_IN_TICKS;
        if (currentDay != lastCheckedDay) {
            lastCheckedDay = currentDay;
            // 获取当前世界的难度
            Difficulty currentDifficulty = world.getDifficulty();
            // 根据当前难度，获取对应的事件Map
            Map<Long, String> eventsForCurrentDifficulty = REMINDER_EVENTS_BY_DIFFICULTY.get(currentDifficulty);
            // 如果当前难度下有事件定义，并且今天这个天数有事件
            if (eventsForCurrentDifficulty != null && eventsForCurrentDifficulty.containsKey(currentDay)) {
                // 获取并发送消息
                WorldState state = WorldStateManager.getState(server.overworld());
                state.setUp(true);
                WorldStateManager.setWorldUp(world,true);
                String message = eventsForCurrentDifficulty.get(currentDay);
                sendMessageToWorld(server, message);
            }
        }
    }
}
