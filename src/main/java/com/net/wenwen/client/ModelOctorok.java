package com.net.wenwen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.net.wenwen.entity.EntityOctorok;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class ModelOctorok extends HierarchicalModel<EntityOctorok> {

    private final ModelPart root;
    private final ModelPart[] tentacles;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        var root = meshdefinition.getRoot();

        // 身体部件保持不变
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 7, 4, 6), PartPose.offset(-3F, 11F, -1F));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(0, 41).addBox(0F, 0F, 0F, 12, 2, 12), PartPose.offset(-6F, 10F, -4F));
        root.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(22, 21).addBox(0F, 0F, 0F, 10, 5, 10), PartPose.offset(-5F, 5F, -3F));
        root.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(30, 0).addBox(0F, 0F, 0F, 8, 5, 8), PartPose.offset(-4F, 0F, -2F));
        root.addOrReplaceChild("shape5", CubeListBuilder.create().texOffs(88, 0).addBox(0F, 0F, 0F, 4, 4, 1), PartPose.offset(-5F, -1F, -3F));
        root.addOrReplaceChild("shape6", CubeListBuilder.create().texOffs(88, 0).addBox(0F, 0F, 0F, 4, 4, 1), PartPose.offset(1F, -1F, -3F));
        root.addOrReplaceChild("shape7", CubeListBuilder.create().texOffs(0, 31).addBox(0F, 0F, 0F, 5, 5, 3), PartPose.offset(-2.5F, 7F, -6F));

        // 触角
        for (int i = 0; i < 6; ++i) {
            double d0 = (double) i * Math.PI * 2.0D / 6.0D;
            float f = (float) Math.cos(d0) * 2.35F - 0.4F;
            float f1 = (float) Math.sin(d0) * 2.35F + 1.25F;
            d0 = (double) i * Math.PI * -2.0D / 6.0D + (Math.PI / 2D);
            float angleY = (float) d0;

            root.addOrReplaceChild("tentacle_" + i,
                    CubeListBuilder.create().texOffs(67, 0).addBox(0.0F, 0.0F, 0.0F, 3, 13, 3),
                    PartPose.offsetAndRotation(f, 13.0F, f1, 0, angleY, 0));
        }

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public ModelOctorok(ModelPart root) {
        this.root = root;
        this.tentacles = new ModelPart[6];
        for (int i = 0; i < 6; ++i) {
            this.tentacles[i] = root.getChild("tentacle_" + i);
        }
    }

    // 【关键1】必须实现 root() 方法
    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float scale) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, scale);
    }

    @Override
    public void setupAnim(EntityOctorok entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for(ModelPart modelpart : this.tentacles) {
            modelpart.xRot = ageInTicks;
        }
    }
}
