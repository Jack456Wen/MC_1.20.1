
package com.net.wenwen.item;

import com.net.wenwen.init.WenwenModMobEffects;
import com.net.wenwen.init.WenwenModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeartcontainerItem extends Item {
	public HeartcontainerItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, level, list, flag);
		list.add(Component.literal("\u00A75帮主人抵挡致命伤害"));
		list.add(Component.literal("\u00A75右键该物品还能感知队友的位置"));
		list.add(Component.literal("\u00A75爱心守护：获得20%免伤效果"));
		list.add(Component.literal("\u00A7a\u5728\u80CC\u5305\u65F6\u653B\u51FB\u751F\u7269\u6709\u6982\u7387\u83B7\u5F971-5\u9897\u7ECF\u9A8C\u7403"));
	}

	@Override
	public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
		{
			if (!_entity.hasEffect(WenwenModMobEffects.HEART_BUFF_01.get())){
				_entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 4, false, false));
				_entity.addEffect(new MobEffectInstance(WenwenModMobEffects.HEART_BUFF_01.get(), 1200, 0, false, false));
				_entity.addEffect(new MobEffectInstance(WenwenModMobEffects.MIANSHANG_EFFECT.get(), 1200, 3, false, false));
			}
		}
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
	{
		InteractionResultHolder<ItemStack> resultHolder = super.use(level, player, hand);
		if (level.isClientSide) return resultHolder; // 确保这是在服务器端执行
		ServerPlayer serverPlayer=(ServerPlayer)player;
		for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
			if (p.getUUID().equals(serverPlayer.getUUID())){
				continue;
			}
			double tempPos= calculateDistance(p,serverPlayer);
			Component message = Component.literal(p.getDisplayName().getString()+"的位置是："+p.blockPosition()+"  距离："+ Math.floor(tempPos) +"米").withStyle(ChatFormatting.AQUA);
			serverPlayer.sendSystemMessage(message);
			p.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 1,false,false));
		}
		player.getCooldowns().addCooldown(resultHolder.getObject().getItem(), 100);
		return resultHolder;
	}
	private double calculateDistance(ServerPlayer player1, ServerPlayer player2) {
		// 获取玩家1和玩家2的世界位置向量
		Vec3 player1Pos = player1.position();
		Vec3 player2Pos = player2.position();

		// 计算两个向量之间的距离
		double dx = player1Pos.x - player2Pos.x;
		double dy = player1Pos.y - player2Pos.y;
		double dz = player1Pos.z - player2Pos.z;

		// 使用勾股定理计算距离
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
		if (event != null && event.getEntity() != null) {
			if(event.getEntity() instanceof Player){
				return;
			}
			if(event.getSource().getEntity() instanceof Player player){
				SpawnXP(player.level(),player.getX(),player.getY(),player.getZ(),player);
			}

		}
	}
	private static void SpawnXP(LevelAccessor world, double x, double y, double z, Player player){
		if (player.hasEffect(WenwenModMobEffects.HEART_BUFF_01.get())) {
			if (Math.random() <= 0.05) {
				for (int index0 = 0; index0 < (int) Mth.nextDouble(RandomSource.create(), 0, 6); index0++) {
					if (world instanceof ServerLevel _level)
						_level.addFreshEntity(new ExperienceOrb(_level, x, y, z, (int) Mth.nextDouble(RandomSource.create(), 50, 160)));
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), WenwenModSounds.EXPDROP.get(), SoundSource.AMBIENT, (float) 0.6, 1);
					} else {
						_level.playLocalSound(x, y, z, WenwenModSounds.EXPDROP.get(), SoundSource.AMBIENT, (float) 0.6, 1, false);
					}
				}
			}
		}
	}


}
