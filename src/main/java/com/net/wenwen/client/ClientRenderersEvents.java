package com.net.wenwen.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.net.wenwen.arrrows.ExplosiveArrowRenderer;
import com.net.wenwen.entity.EntityOctorok;
import com.net.wenwen.entity.ModEntities;
import com.net.wenwen.render.EndSkyRenderer;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = "wenwen", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRenderersEvents {
    public static final ModelLayerLocation OCTOROK_LAYER = new ModelLayerLocation(new ResourceLocation("wenwen", "octorok"), "main");

    // 【这就是 1.20.1 Forge 的核心注册方式！】
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 使用 event.registerLayer，而不是 LayerDefinitions.register！
        event.registerLayerDefinition(OCTOROK_LAYER, ModelOctorok::createBodyLayer);
    }
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOSTILE_BAT.get(), BatRenderer::new);
        /*event.registerEntityRenderer(ModEntities.OCTOROK.get(), (context) -> {
            SquidModel<EntityOctorok> model = new SquidModel<>(context.bakeLayer(ModelLayers.SQUID));
            return new SquidRenderer<>(context, model);
        });*/
        event.registerEntityRenderer(ModEntities.OCTOROK.get(), (context) -> {
            ModelOctorok model = new ModelOctorok(context.bakeLayer(OCTOROK_LAYER));

            return new LivingEntityRenderer<EntityOctorok, ModelOctorok>(context, model, 0.5f) {
                @Override
                public ResourceLocation getTextureLocation(EntityOctorok entity) {
                    int type = entity.getOctorokType();

                    if (type == 1) {
                        // type为1，返回精英怪纹理
                        return new ResourceLocation("wenwen", "textures/entity/octorok1.png");
                    } else {
                        // type为0，返回普通怪纹理
                        return new ResourceLocation("wenwen", "textures/entity/octorok2.png");
                    }
                }
                @Override
                protected boolean shouldShowName(EntityOctorok entity) {
                    return false;
                }
                @Override
                protected void setupRotations(EntityOctorok p_116035_, PoseStack p_116036_, float p_116037_, float p_116038_, float p_116039_) {
                    float f = Mth.lerp(p_116039_, p_116035_.xBodyRotO, p_116035_.xBodyRot);
                    float f1 = Mth.lerp(p_116039_, p_116035_.zBodyRotO, p_116035_.zBodyRot);
                    p_116036_.translate(0.0F, 0.5F, 0.0F);
                    p_116036_.mulPose(Axis.YP.rotationDegrees(180.0F - p_116038_));
                    p_116036_.mulPose(Axis.XP.rotationDegrees(f));
                    p_116036_.mulPose(Axis.YP.rotationDegrees(f1));
                    p_116036_.translate(0.0F, -1.2F, 0.0F);
                }
                @Override
                protected float getBob(EntityOctorok p_116032_, float p_116033_) {
                    return Mth.lerp(p_116033_, p_116032_.oldTentacleAngle, p_116032_.tentacleAngle);
                }
            };
        });

        event.registerEntityRenderer(ModEntities.EXPLOSIVE_ARROW.get(), ExplosiveArrowRenderer::new);

    }

    @SubscribeEvent
    public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(BuiltinDimensionTypes.END_EFFECTS, new EndSkyRenderer());
    }
}
