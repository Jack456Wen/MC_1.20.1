package com.net.wenwen.arrrows;

import com.net.wenwen.arrrows.ExplossiveArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ExplosiveArrowRenderer extends ArrowRenderer<ExplossiveArrow> {

    public ExplosiveArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ExplossiveArrow arrow) {
        return new ResourceLocation("wenwen", "textures/entity/arrow/explosive_arrow.png");
        //return new ResourceLocation("minecraft", "textures/entity/projectiles/arrow.png");
    }
}
