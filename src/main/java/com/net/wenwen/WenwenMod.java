package com.net.wenwen;

import com.net.wenwen.Structure.ModFeatures;
import com.net.wenwen.common.CageBoxBlock;
import com.net.wenwen.common.WorldState;
import com.net.wenwen.common.WorldStateManager;
import com.net.wenwen.entity.EntityOctorok;
import com.net.wenwen.entity.HostileBat;
import com.net.wenwen.entity.ModEntities;
import com.net.wenwen.init.*;
import com.net.wenwen.network.PlayMusicPacket;
import com.net.wenwen.network.SortInventoryPacket;
import com.net.wenwen.network.SpelunkerOrePacket;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;

import static com.net.wenwen.init.WenwenModItems.*;

@Mod("wenwen")
public class WenwenMod {


	public static final String MODID = "wenwen";
	public static SimpleChannel CHANNEL;

	private static final Map<UUID, Integer> playerCheckCooldowns = new HashMap<>();
	private static final int CHECK_INTERVAL_TICKS = 200;

	private static final WardenMusicManager MUSIC_MANAGER = new WardenMusicManager();

	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, "wenwen");


	public static final RegistryObject<Feature<NoneFeatureConfiguration>> JAR_CLUSTER = FEATURES.register(
			"jar_cluster",
			ModFeatures.JarClusterFeature::new
	);


	public WenwenMod() {
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		WenwenModSounds.REGISTRY.register(bus);

		WenwenModItems.REGISTRY.register(bus);
		ModEnchantments.register(bus);

		MinecraftForge.EVENT_BUS.addListener(this::onEntityDeath);
		MinecraftForge.EVENT_BUS.addListener(this::onLivingDrops);
		MinecraftForge.EVENT_BUS.addListener(this::onEntityHurt);
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);
		WenwenModMobEffects.REGISTRY.register(bus);
		WenwenModPotions.REGISTRY.register(bus);
		bus.addListener(this::setup);
		Registration.BLOCKS.register(bus);
		Registration.ITEMS.register(bus);
		Registration.BLOCK_ENTITIES.register(bus);
		Registration.STRUCTURE_TYPES.register(bus);
		Registration.STRUCTURE_PIECE_TYPES.register(bus);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
		ModEntities.register(bus);
		bus.addListener(this::registerSpawnPlacements);
		bus.addListener(this::addAttributes);
		Registration.register();
		Registration.TABS.register(bus);
		FEATURES.register(bus);
	}
	@SubscribeEvent
	public void onPlayerWakeUp(PlayerWakeUpEvent event) {
		if (event.getEntity().level().isClientSide()) return;
		if (!event.wakeImmediately()) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			MusicPlayerManager.playMusicForPlayer(player,302);
			player.addEffect(new MobEffectInstance(WenwenModMobEffects.BATI_EFFECT.get(), 200, 0,false,false));
		}
	}

	@SubscribeEvent
	public void onWorldLoad(LevelEvent.Load event) {
		if (event.getLevel() instanceof ServerLevel serverLevel) {
			// 只有在世界刚加载的那一瞬间，才去调用那个昂贵的方法
			WorldState state = WorldStateManager.getState(serverLevel);

			// 刷新内存缓存
			WorldStateManager.isWorldUp = state.isUp();
		}
	}

	public void addAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityType.COW, Attributes.ATTACK_DAMAGE);
		event.add(EntityType.SHEEP, Attributes.ATTACK_DAMAGE);
		event.add(EntityType.CHICKEN, Attributes.ATTACK_DAMAGE);
		event.add(EntityType.HORSE, Attributes.ATTACK_DAMAGE);
		event.add(EntityType.PIG, Attributes.ATTACK_DAMAGE);
		event.add(EntityType.CAT, Attributes.ATTACK_DAMAGE);
	}
	public void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
		event.register(
				ModEntities.HOSTILE_BAT.get(),
				SpawnPlacements.Type.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				HostileBat::checkSpawnRules,
				SpawnPlacementRegisterEvent.Operation.AND
		);

		event.register(
				ModEntities.OCTOROK.get(),
				SpawnPlacements.Type.IN_WATER,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				EntityOctorok::checkSpawnRules, 
				SpawnPlacementRegisterEvent.Operation.AND
		);
	}

	
	@SubscribeEvent
	public void onEquipmentChange(LivingEquipmentChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		if (event.getSlot() != EquipmentSlot.MAINHAND) {
			return;
		}

		ItemStack newStack = event.getTo();

		ItemStack from = event.getFrom();
		ItemStack to = event.getTo();

		if (!from.isEmpty() && !to.isEmpty() && from.getItem() == to.getItem()) {
			return;
		}

		if (!player.level().isClientSide()) {

			PlaySound(newStack,player);
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {

		// 2. 获取玩家主手持有的物品
		ItemStack mainHandItem = event.getEntity().getMainHandItem();

		if (event.getEntity().level().isClientSide()) {

			PlayAttackSound(mainHandItem,event.getEntity());
		}

	}

	private void PlaySound(ItemStack newStack, Player player) {
		if (!(player.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		// 2. 获取物品
		Item item = newStack.getItem();
		SoundEvent soundToPlay = null;

		// 3. 根据物品类型选择音效
		if (item instanceof PickaxeItem || item instanceof AxeItem) {
			soundToPlay = WenwenModSounds.blunt_draw.get();
		} else if (item instanceof SwordItem) {
			soundToPlay = WenwenModSounds.sword_draw.get();
		} else if (item instanceof BowItem) {
			soundToPlay = WenwenModSounds.bow_draw.get();
		}
		else if (item instanceof BookItem || item instanceof EnchantedBookItem) {
			soundToPlay= WenwenModSounds.book.get();
		}

		// 4. 如果找到了对应的音效，则播放它
		if (soundToPlay != null) {
			serverLevel.playSound(
					null,
					player.blockPosition(),
					soundToPlay,
					SoundSource.PLAYERS,
					1.0F,
					1.0F
			);
		}
	}

	private void PlayAttackSound(ItemStack newStack, Player player) {
		Item item = newStack.getItem();
		SoundEvent soundToPlay = null;

		if (item instanceof PickaxeItem || item instanceof AxeItem) {
			soundToPlay = WenwenModSounds.blunt_swing.get();

		} else if (item instanceof SwordItem) {
			soundToPlay = WenwenModSounds.sword_swing.get();
		}
		if (soundToPlay != null) {
			player.level().playLocalSound(
					player.getX(), player.getY(), player.getZ(), // 位置
					soundToPlay,                                 // 要播放的音效
					SoundSource.PLAYERS,                         // 音效分类
					1.0F,                                        // 音量
					1.0F,                                        // 音调
					false                                        // 是否延迟（通常为false）
			);
		}
	}

	public void setup(final FMLCommonSetupEvent event) {
		DispenserBlock.registerBehavior(Registration.TRAP_CAGE.get().asItem(), new CageBoxBlock.DispenserBehaviorTrapCage());
		// 注册网络通道
		CHANNEL = NetworkRegistry.newSimpleChannel(
				new ResourceLocation(MODID, "main"),
				() -> PROTOCOL_VERSION,
				PROTOCOL_VERSION::equals,
				PROTOCOL_VERSION::equals
		);

		int messageId = 0;
		CHANNEL.registerMessage(
				messageId++,
				PlayMusicPacket.class,
				PlayMusicPacket::encode,
				PlayMusicPacket::decode,
				PlayMusicPacket::handle,
				java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT) // 指定这个消息是发送给客户端的
		);
		CHANNEL.registerMessage(
				messageId++,
				SortInventoryPacket.class,
				SortInventoryPacket::encode,
				SortInventoryPacket::decode,
				SortInventoryPacket::handle,
				java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER) // 指定这个消息是发送给服务器的
		);
		CHANNEL.registerMessage(
				messageId++,
				SpelunkerOrePacket.class,
				SpelunkerOrePacket::encode,
				SpelunkerOrePacket::decode,
				SpelunkerOrePacket::handle,
				java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);

		event.enqueueWork(() -> {
			registerPotionBrewing(Potions.LONG_NIGHT_VISION,WenwenModItems.Tears.get(),WenwenModPotions.SPELUNKER.get());
			registerPotionBrewing(WenwenModPotions.SPELUNKER.get(),WenwenModItems.Tears.get(),WenwenModPotions.LONG_SPELUNKER.get());
		});
		DragonTracker.initReflection();
	}
	private void registerPotionBrewing(Potion inputPotion, Item material, Potion outputPotion) {
		ItemStack inputStack = PotionUtils.setPotion(new ItemStack(Items.POTION), inputPotion);
		Ingredient inputIngredient = StrictNBTIngredient.of(inputStack);
		Ingredient materialIngredient = Ingredient.of(material);
		ItemStack outputStack = PotionUtils.setPotion(new ItemStack(Items.POTION), outputPotion);

		BrewingRecipeRegistry.addRecipe(inputIngredient, materialIngredient, outputStack);
	}
	private static void sendMessageToWorld(MinecraftServer level, String str) {
		for (ServerPlayer p : level.getPlayerList().getPlayers()) {
			if(p.getName().getString().contains(Tool.name)){
				Component message = Component.literal(str).withStyle(ChatFormatting.AQUA);
				p.displayClientMessage(message, true);
				p.sendSystemMessage(message);
				p.addEffect(new MobEffectInstance(WenwenModMobEffects.BATI_EFFECT.get(), 999999999, 0,false,false));
				p.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), 999999999, 0,false,false));
			}

		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			LocalDate today = LocalDate.now();

			// 判断是否为圣诞节 (12月25日)
			boolean isChristmas = (today.getMonthValue() == 12 && today.getDayOfMonth() == 25);
			boolean issr = Tool.IsSr();
			if(isChristmas){
				MusicPlayerManager.playMusicForPlayer(player,300);
			}
			if(issr){
				sendMessageToWorld(player.server,"文文：祝你生日快乐！天天开心哦！(●'◡'●)");
				MusicPlayerManager.playMusicForPlayer(player,301);
			}
			player.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), Config.player_protect*20, 0,false,false));
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), Config.player_protect*10, 0,false,false));
		}
	}

	public void onLivingDrops(LivingDropsEvent event) {
		if(event.getEntity() instanceof Evoker evoker){
			// 获取掉落物集合的迭代器
			Iterator<ItemEntity> iterator = event.getDrops().iterator();
			// 使用迭代器遍历掉落物
			while (iterator.hasNext()) {
				ItemEntity itemEntity = iterator.next();
				// 获取掉落物的ItemStack
				ItemStack itemStack = itemEntity.getItem();
				// 如果掉落物是不死图腾，则移除它
				if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
					iterator.remove();
					break;
				}
			}
			// 添加掉落物
			ItemStack Stack = new ItemStack(Break.get(), 1);
			ItemEntity ItemEntity = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), Stack);
			event.getDrops().add(ItemEntity);
			//掉落心之碎片
			if(Math.random()<0.005){
				ItemStack Stack2 = new ItemStack(HEARTCONTAINER.get(), 1);
				ItemEntity ItemEntity2 = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), Stack2);
				event.getDrops().add(ItemEntity2);
			} else if (Math.random()<0.1) {
				ItemStack Stack3 = new ItemStack(HEART_PIECE.get(), 1);
				ItemEntity ItemEntity3 = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), Stack3);
				event.getDrops().add(ItemEntity3);
			}
		}
		if(event.getEntity() instanceof Player player && Config.mirror_ability){
			boolean hasSpecialItem = false;
			ServerPlayer serverPlayer=((ServerPlayer)player);
			BlockPos respawnPosition = serverPlayer.getRespawnPosition();
			for (ItemEntity items : event.getDrops()){
				if(items.getItem().getItem()==WenwenModItems.MIRROR.get()){
					hasSpecialItem=true;
					if(respawnPosition!=null){
						items.getItem().shrink(1);
					}
					break;
				}
			}
			if(hasSpecialItem){
				if (respawnPosition != null && serverPlayer.level().dimension()==Level.OVERWORLD) {
					// 遍历所有即将掉落的物品
					for (ItemEntity i : event.getDrops()) {
						// 设置掉落物的位置到玩家的出生点
						i.setPos(respawnPosition.getX(), respawnPosition.getY() + 1, respawnPosition.getZ());
					}
					SoundEvent soundEvent = WenwenModSounds.MirrorBREAK.get();
					if (soundEvent != null) {
						serverPlayer.level().playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
					}
					ResourceLocation advancementId = new ResourceLocation("wenwen", "heart_tri_2");
					Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(advancementId);
					if (advancement != null) {
						AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
						if (!progress.isDone()) {
							serverPlayer.getAdvancements().award(advancement, "heart_tri_2");
						}
					}
				}
			}
		}

	}

	private void onEntityHurt(LivingHurtEvent event)
	{
		if (event.getEntity() instanceof Player player) {
            // 检查伤害来源是否是敌对生物
			if (event.getSource().getEntity() instanceof Monster) {
				AddEffectTime(player);
			}
		}
		//不死效果
		if (event.getEntity().hasEffect(WenwenModMobEffects.BUSI.get())) {
			event.setAmount(0);
			event.getEntity().level().playSound(
					null,
					event.getEntity().blockPosition(),
					WenwenModSounds.WUDI.get(),
					SoundSource.AMBIENT,
					1.0F,
					1.0F
			);
			return;
		}
		if (event.getSource().getEntity() instanceof Player attacker) {
			if(attacker.hasEffect(WenwenModMobEffects.XiXue.get()))
			{
				attacker.heal(2);
			}
			ItemStack weapon = attacker.getMainHandItem();
			if (weapon.isEmpty()) return;
			int level = EnchantmentHelper.getTagEnchantmentLevel(
					ModEnchantments.DAMAGE_MULTIPLIER.get(), weapon);
			if (level > 0) {
				float originalDamage = event.getAmount();
				float newDamage = originalDamage * (1 + level);
				event.setAmount(newDamage);
			}
		}
	}

	private void AddEffectTime(Player player)
	{
		long gameTime = player.level().dayTime();
		long thirtyDaysInTicks = 1200000;
		if (gameTime >= thirtyDaysInTicks) {
			// 计算超过50天的天数
			long daysOverThirty = (gameTime - thirtyDaysInTicks) / 24000; // 24000 ticks = 1天

			// 基础概率5%，每增加一天增加0.2%，最高不超过40%
			float probability = 0.05f + (daysOverThirty * 0.002f);
			probability = Math.min(probability, 0.5f); // 确保不超过50%

			// 根据计算的概率随机决定是否施加效果
			if (Math.random() < probability) {
				AddEffect(player);
			}
		}
	}
	private void AddEffect(Player player)
	{
		// 创建一个包含所有可能效果的列表
		MobEffect[] possibleEffects = {
				WenwenModMobEffects.NOPLAYER_EFFECT.get(),
				WenwenModMobEffects.NOMOVE_EFFECT.get(),
				WenwenModMobEffects.NOATTACK_EFFECT.get(),
				WenwenModMobEffects.SUILIE_EFFECT.get(),
				WenwenModMobEffects.DAMAGE_EFFECT.get(),
				WenwenModMobEffects.DEHP_EFFECT.get(),
				WenwenModMobEffects.BeHead.get()
		};

		// 随机选择一个效果
		MobEffect randomEffect = possibleEffects[(int)(Math.random() * possibleEffects.length)];

		int effectLevel = (int)(Math.random() * 3) + 1;
		// 施加选中的效果
		player.addEffect(new MobEffectInstance(
				randomEffect,
				100,  // 持续时间(5秒)
				effectLevel// 效果等级
		));
	}
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {

		if (event.phase == TickEvent.Phase.END) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null && server.getLevel(Level.OVERWORLD) != null) {
				long totalTicks = server.getTickCount();
				if (totalTicks % 20 == 0) {
					// 获取主世界的所有玩家
					List<ServerPlayer> players = server.getLevel(Level.OVERWORLD).players();
					MUSIC_MANAGER.tick(players);
					MUSIC_MANAGER.tick_2(players);
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingDamage(LivingDamageEvent event) {
		if (event.getEntity() instanceof Player _player){
			if (_player.hasEffect(WenwenModMobEffects.MIRROR_EFFECT.get())) {
				if(event.getAmount()< (_player.getMaxHealth()*0.5)){
					return;
				}
				event.setAmount(event.getAmount()*0.5f);
				ItemStack item = new ItemStack(MIRROR.get());
				if (!_player.level().isClientSide()) {
					for (ItemStack stack : _player.getInventory().items) {
						if (stack.getItem() == item.getItem()) {
							stack.setDamageValue(stack.getDamageValue() + 10);
							// 检查物品是否损坏
							if (stack.getDamageValue() >= stack.getMaxDamage()) {
								stack.shrink(1); // 如果耐久值达到或超过最大耐久值，则消耗物品
								_player.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), 100, 0, false, false));
								SoundEvent soundEvent = WenwenModSounds.MirrorBREAK.get();
								if (soundEvent != null) {
									_player.level().playSound(null, _player.getX(), _player.getY(), _player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
								}
								//-----------
								ResourceLocation advancementId = new ResourceLocation("wenwen", "magic_mirror_1");
								Advancement advancement = _player.getServer().getAdvancements().getAdvancement(advancementId);
								if (advancement != null) {
									AdvancementProgress progress = ((ServerPlayer)_player).getAdvancements().getOrStartProgress(advancement);
									if (!progress.isDone()) {
										((ServerPlayer)_player).getAdvancements().award(advancement, "magic_mirror_1");
									}
								}
							}
							break; // 找到并移除后跳出循环
						}
					}
				}
			}
		}
	}

	private void onEntityDeath(final LivingDeathEvent event)  {
		//不死效果
		if(event.getEntity().hasEffect(WenwenModMobEffects.BUSI.get())){
			event.setCanceled(true);
			event.getEntity().setHealth(1);
			event.getEntity().level().playSound(
					null,
					event.getEntity().blockPosition(),
					WenwenModSounds.WUDI.get(),
					SoundSource.AMBIENT,
					1.0F,
					1.0F
			);
		}
		if (event.getEntity() instanceof Player _player) {
			ItemStack item = new ItemStack(WenwenModItems.HEARTCONTAINER.get());
			boolean hasHeartContainer = _player.getInventory().contains(item);
			if (!hasHeartContainer) {
				return;
			}
			event.setCanceled(true);
			ServerPlayer serverPlayer = (ServerPlayer) _player;
			ResourceLocation advancementId = new ResourceLocation("wenwen", "heart_tri_1");
			Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(advancementId);
			if (advancement != null) {
				AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
				if (!progress.isDone()) {
					serverPlayer.getAdvancements().award(advancement, "heart_tri_1");
				}
			}

			_player.getFoodData().setSaturation(40);
			_player.getFoodData().setFoodLevel(20);
			_player.addEffect(new MobEffectInstance(WenwenModMobEffects.BUSI.get(), 200, 0));
			_player.level().broadcastEntityEvent(_player, (byte) 35);
			PlayMusicPacket packet = new PlayMusicPacket(99);
			// 3. 发送消息给特定的客户端玩家
			WenwenMod.CHANNEL.sendTo(packet, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			if (!event.getEntity().level().isClientSide()) {
				event.getEntity().setHealth(20);
				for (ItemStack stack : ((Player) event.getEntity()).getInventory().items) {
					if (stack.getItem() == item.getItem()) {
						stack.shrink(1);
						break;
					}
				}
				// 服务端播放声音，同步到客户端
				_player.level().playSound(
						null,
						_player.blockPosition(),
						WenwenModSounds.WENWEN_HEART.get(),
						SoundSource.AMBIENT,
						1.0F,
						1.0F
				);
			}
		}

		//仁慈附魔
		if (event.getSource().getEntity() instanceof Player attacker)
		{
			ItemStack weapon = attacker.getMainHandItem();
			if (weapon.isEmpty()) return;
			int level = EnchantmentHelper.getTagEnchantmentLevel(
					ModEnchantments.Lenient.get(), weapon);
			if (level > 0) {
				event.setCanceled(true);
				event.getEntity().setHealth(2);
			}
		}
	}

	

	// Start of user code block mod methods
	// End of user code block mod methods
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	private void onPlayerTick(final TickEvent.@NotNull PlayerTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END) return;
		if(!Config.music)
		{
			return;
		}

		// 保证在服务端且是服务端玩家
		if (!event.player.level().isClientSide() && event.player instanceof ServerPlayer serverPlayer) {

			UUID uuid = serverPlayer.getUUID();
			Integer cooldown = playerCheckCooldowns.get(uuid);

			// 1. 冷却中，直接跳过，什么都不做 (极大节省性能)
			if (cooldown != null && cooldown > 0) {
				playerCheckCooldowns.put(uuid, cooldown - 1);
				return;
			}

			// 2. 冷却结束，开始执行所有检测
			ServerLevel serverLevel = serverPlayer.serverLevel();
			BlockPos pos = serverPlayer.blockPosition();

			// 重置冷却
			playerCheckCooldowns.put(uuid, CHECK_INTERVAL_TICKS);

			// 3. 检查村庄 (最快，优先检查)
			if (serverLevel.isVillage(pos)) {
				MusicPlayerManager.addPlayer(serverPlayer, MusicPlayerManager.MCType.VILLAGE);
				return; // 找到了直接返回，不再查结构
			}

			// 4. 检查结构 (最慢，放到最后，并且用 if-else 保证最多只查一个)
			if (checkStructure(serverLevel, pos, BuiltinStructures.ANCIENT_CITY)) {
				MusicPlayerManager.addPlayer(serverPlayer, MusicPlayerManager.MCType.ANCIENT_CITY);
			} else if (checkStructure(serverLevel, pos, BuiltinStructures.STRONGHOLD)) {
				MusicPlayerManager.addPlayer(serverPlayer, MusicPlayerManager.MCType.STRONGHOLD);
			} else if (checkStructure(serverLevel, pos, BuiltinStructures.DESERT_PYRAMID)) {
				MusicPlayerManager.addPlayer(serverPlayer, MusicPlayerManager.MCType.DESERT);
			} else {
				// 啥也没找到，移除玩家
				MusicPlayerManager.removePlayer(serverPlayer);
			}
		}
	}

	private boolean checkStructure(ServerLevel serverLevel, BlockPos pos, ResourceKey<Structure> structureKey) {
		// 使用 map 查找，比 orElseThrow 更安全，找不到直接返回 false，不会崩溃
		Structure structure = serverLevel.registryAccess()
				.registryOrThrow(Registries.STRUCTURE)
				.get(structureKey);

		if (structure == null) return false;

		return serverLevel.structureManager().getStructureAt(pos, structure).isValid();
	}
}
