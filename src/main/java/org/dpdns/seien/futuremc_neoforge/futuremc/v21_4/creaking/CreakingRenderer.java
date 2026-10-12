package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client.ClientSetup;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.CreakingModel;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.CreakingEyesLayer;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.Creaking;

/**
 * 嘎吱的实体渲染器。
 * <p>
 * 使用嘎吱模型 + 0.6 阴影半径，并叠加眼睛发光层（CreakingEyesLayer）
 * 让眼睛在激活/解体时发光。纹理路径指向 futuremc 命名空间的
 * textures/entity/creaking/creaking.png。
 */
public class CreakingRenderer extends MobRenderer<Creaking, CreakingModel> {
    /** 嘎吱主体纹理。 */
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("futuremc", "textures/entity/creaking/creaking.png");

    public CreakingRenderer(EntityRendererProvider.Context context) {
        super(context, new CreakingModel(context.bakeLayer(ClientSetup.CREAKING_LAYER), false), 0.6F);
        this.addLayer(new CreakingEyesLayer(this, new CreakingModel(context.bakeLayer(ClientSetup.CREAKING_EYES_LAYER), true)));
    }

    /** 返回嘎吱纹理路径。 */
    @Override
    public ResourceLocation getTextureLocation(Creaking entity) {
        return TEXTURE;
    }
}
