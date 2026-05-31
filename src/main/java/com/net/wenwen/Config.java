package com.net.wenwen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final Config INSTANCE = new Config();
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec.BooleanValue EASY_MOB_CAPTURING = BUILDER.comment("如果设置为false，将不会捕获敌对生物").define("gamerules.easy_mob_capture", true);
    public static ForgeConfigSpec.BooleanValue SCIENTIFIC_NAMES = BUILDER.comment("是否显示笼内生物名字").define("gamerules.scientific_names", true);
    public static ForgeConfigSpec.BooleanValue MUSIC = BUILDER.comment("是否在特定环境下播放音乐，提高沉浸感").define("gamerules.music", true);
    public static ForgeConfigSpec.BooleanValue ENDER = BUILDER.comment("是否启用末地独特天空").define("gamerules.ender", true);
    public static ForgeConfigSpec.IntValue LOOT_ROOLS = BUILDER
            .comment("文文的小玩意战利品宝箱中的额外抽取次数（范围：0-10）")
            .defineInRange("gamerules.loot.wenwen_drop_rolls", 1, 0, 10);
    public static ForgeConfigSpec.DoubleValue LOOT_CHANCE = BUILDER
            .comment("文文的小玩意出现在战利品宝箱中的概率（范围：0-1）")
            .defineInRange("gamerules.loot.wenwen_drop_chance", 0.2, 0, 1);

    public static ForgeConfigSpec.BooleanValue MIRROR_ABILITY = BUILDER.comment("是否启用魔镜的特殊能力").define("gamerules.mirror_ability", true);
    public static ForgeConfigSpec.DoubleValue MIRROR_BREAK = BUILDER
            .comment("文文的魔镜使用时破碎的概率（范围：0-1）")
            .defineInRange("gamerules.mirror_break_chance", 0.1, 0, 1);

    public static ForgeConfigSpec.IntValue Player_Protect = BUILDER
            .comment("重生保护时间")
            .defineInRange("gamerules.player_protect", 10, 0, 9999999);

    public static ForgeConfigSpec.BooleanValue Animal_ABILITY = BUILDER.comment("启用后被动生物将会反击").define("gamerules.animal_ability", true);
    public static ForgeConfigSpec.BooleanValue Bat_ABILITY = BUILDER.comment("启用后蝙蝠会变得更强（适合高手）").define("gamerules.bat_ability", true);
    public static ForgeConfigSpec.DoubleValue Creeper = BUILDER
            .comment("闪电苦力怕自然生成的概率（范围：0-1）")
            .defineInRange("gamerules.creeper_chance", 0.1, 0, 1);

    public static ForgeConfigSpec.DoubleValue Skeleton = BUILDER
            .comment("骷髅骑士自然生成的概率（范围：0-1）")
            .defineInRange("gamerules.skeleton_chance", 0.05, 0, 1);

    private static final ForgeConfigSpec.DoubleValue mobHealthRate;
    private static final ForgeConfigSpec.DoubleValue mobHealthBase;
    private static final ForgeConfigSpec.DoubleValue maxHealth;

    private static final ForgeConfigSpec.DoubleValue mobDamageRate;
    private static final ForgeConfigSpec.DoubleValue mobDamageBase;
    private static final ForgeConfigSpec.DoubleValue maxDamage;

    private static final ForgeConfigSpec.DoubleValue mobSpeedRate;
    private static final ForgeConfigSpec.DoubleValue mobSpeedBase;
    private static final ForgeConfigSpec.DoubleValue maxSpeed;

    private static final ForgeConfigSpec.BooleanValue exponential;
    private static final ForgeConfigSpec.BooleanValue isEpic;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> mobWhitelist;

    static
    {
        BUILDER.push("怪物成长系统");
        BUILDER.push("注意");

        exponential = BUILDER
                .comment("如果为True，怪物属性将呈指数级增长(非常恐怖)")
                .define("使用指数增长", false);

        isEpic = BUILDER
                .comment("如果为True，怪物成长将采用玩家战力方式增长")
                .define("使用战力成长", false);

        mobWhitelist = BUILDER
                .comment("可以自己添加需要成长的怪物")
                .defineList("Mob Whitelist", List.of(),
                        element -> element instanceof String);
        BUILDER.pop();

        BUILDER.push("生命值");

        mobHealthRate = BUILDER
                .comment("敌对生物每天生命值增加的数值比例")
                .defineInRange("Health Scale Rate", 2.0, 0.0, Double.POSITIVE_INFINITY);
        mobHealthBase = BUILDER
                .comment("敌对生物的基础数值比例")
                .defineInRange("Mob Health Base", 0.7, 0.0, Double.POSITIVE_INFINITY);
        maxHealth = BUILDER
                .comment("敌对生物生命值最高增加的数值比例")
                .defineInRange("Max Scaled Health", Double.POSITIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY);

        BUILDER.pop();
        BUILDER.push("攻击力");

        mobDamageRate = BUILDER
                .comment("敌对生物每天攻击力增加的数值比例")
                .defineInRange("Damage Scale Rate", 0.1, 0.0, Double.POSITIVE_INFINITY);
        mobDamageBase = BUILDER
                .comment("敌对生物的基础数值比例")
                .defineInRange("Mob Damage Base", 0.7, 0.0, Double.POSITIVE_INFINITY);
        maxDamage = BUILDER
                .comment("敌对生物攻击力最高增加的数值比例")
                .defineInRange("Max Scaled Damage", Double.POSITIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY);

        BUILDER.pop();
        BUILDER.push("速度");

        mobSpeedRate = BUILDER
                .comment("敌对生物每天速度增加的数值比例")
                .defineInRange("Speed Scale Rate", 0.005, 0.0, Double.POSITIVE_INFINITY);

        mobSpeedBase = BUILDER
                .comment("敌对生物的基础数值比例")
                .defineInRange("Mob Speed Base", 1.0, 0.0, Double.POSITIVE_INFINITY);

        maxSpeed = BUILDER
                .comment("敌对生物速度最高增加的数值比例")
                .defineInRange("Max Scaled Speed", 1.5, 0.0, Double.POSITIVE_INFINITY);

        BUILDER.pop();
        BUILDER.pop();
    }
    public List<? extends String> getMobWhitelist()
    {   return mobWhitelist.get();
    }
    public double getMobHealthRate()
    {
        return mobHealthRate.get();
    }
    public double getMobHealthBase()
    {
        return mobHealthBase.get();
    }
    public double getMobHealthMax()
    {
        return maxHealth.get();
    }

    public double getMobDamageRate()
    {
        return mobDamageRate.get();
    }
    public double getMobDamageBase()
    {
        return mobDamageBase.get();
    }
    public double getMobDamageMax()
    {
        return maxDamage.get();
    }

    public double getMobSpeedRate()
    {
        return mobSpeedRate.get();
    }

    public double getMobSpeedBase()
    {
        return mobSpeedBase.get();
    }

    public double getMobSpeedMax()
    {
        return maxSpeed.get();
    }

    public boolean areStatsExponential()
    {
        return exponential.get();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean easyMobCapturing;
    public static boolean scientificNames;
    public static boolean enderSky;
    public static int loot_rools;
    public static double loot_chance;
    public static double mirror_break;
    public static boolean mirror_ability;
    public static boolean music;
    public static int player_protect;
    public static boolean batAbility;
    public static boolean isepic;
    public static double skeleton_chance;
    public static double creeper_chance;


    public static Config getInstance()
    {
        return INSTANCE;
    }
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        easyMobCapturing = EASY_MOB_CAPTURING.get();
        scientificNames = SCIENTIFIC_NAMES.get();
        loot_chance = LOOT_CHANCE.get();
        loot_rools=LOOT_ROOLS.get();
        mirror_break=MIRROR_BREAK.get();
        mirror_ability=MIRROR_ABILITY.get();
        music=MUSIC.get();
        player_protect=Player_Protect.get();
        batAbility=Bat_ABILITY.get();
        isepic=isEpic.get();
        creeper_chance=Creeper.get();
        skeleton_chance=Skeleton.get();
        enderSky= ENDER.get();
    }
}
