package com.net.wenwen.init;

import com.net.wenwen.WenwenMod;
import com.net.wenwen.enchantment.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModEnchantments {


    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, WenwenMod.MODID);

    public static final RegistryObject<Enchantment> DAMAGE_MULTIPLIER =
            register("damage_multiplier", DamageMultiplierEnchantment::new);
    public static final RegistryObject<Enchantment> Beheaded =
            register("beheaded", BeheadedEnchantment::new);
    public static final RegistryObject<Enchantment> Distance =
            register("distance", DistancePowerEnchantment::new);
    public static final RegistryObject<Enchantment> Killer =
            register("killer", KillerEnchantment::new);
    public static final RegistryObject<Enchantment> Lenient =
            register("lenient", LenientEnchantment::new);
    public static final RegistryObject<Enchantment> Unbreak =
            register("unbreak", UnbreakableEnchant::new);
    public static final RegistryObject<Enchantment> Mingdao =
            register("mingdao", MingdaoEnchant::new);

    public static final RegistryObject<Enchantment> Fortune =
            register("fortune", FortuneEnchantment::new);

    public static final RegistryObject<Enchantment> Purge =
            register("purge", PurgeEnchantment::new);
    public static final RegistryObject<Enchantment> Berserker =
            register("berserker", BerserkerEnchantment::new);

    public static final RegistryObject<Enchantment> Dehp =
            register("dehp", DehpEnchantment::new);

    public static final RegistryObject<Enchantment> Penetrate =
            register("penetrate", PenetrateEnchantment::new);
    public static final RegistryObject<Enchantment> Arrow =
            register("arrow", ArrowEnchantment::new);

    public static final RegistryObject<Enchantment> Armor =
            register("armor", ArmorEnchantment::new);

    public static final RegistryObject<Enchantment> ItemSave =
            register("save", ItemSaveEnchantment::new);

    public static final RegistryObject<Enchantment> Health =
            register("health", HealthBoostEnchantment::new);

    public static final RegistryObject<Enchantment> DROP =
            register("drop", DropEnchantment::new);

    public static final RegistryObject<Enchantment> Egg =
            register("egg", EggEnchantment::new);

    public static final RegistryObject<Enchantment> DragonKiller =
            register("dragon_killer", DragonKillerEnchantment::new);

    public static final RegistryObject<Enchantment> PigKiller =
            register("pig_killer", PigKillerEnchantment::new);

    public static final RegistryObject<Enchantment> Purify =
            register("purify", PurifyEnchantment::new);

    public static final RegistryObject<Enchantment> XP =
            register("xp", XpEnchantment::new);
    public static final RegistryObject<Enchantment> XP_Damage =
            register("xp_damage", XpDamageEnchantment::new);

    public static final RegistryObject<Enchantment> Boss =
            register("boss", BossEnchantment::new);

    private static <T extends Enchantment> RegistryObject<T> register(String name, Supplier<T> enchantment) {
        return ENCHANTMENTS.register(name, enchantment);
    }

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
