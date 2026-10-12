package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client.model.CreakingModel;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.entity.Creaking;

/**
 * 嘎吱的眼睛发光层。
 * <p>
 * 只渲染头部模型并用发光材质（RenderType.eyes），在激活或解体时
 * 让眼睛发橙光。解体过程中按 eyesGlowing 闪烁。
 */
public class CreakingEyesLayer extends RenderLayer<Creaking, CreakingModel> {
    /** 嘎吱眼睛发光纹理。 */
    private static final ResourceLocation EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath("futuremc", "textures/entity/creaking/creaking_eyes.png");

    /** 仅眼睛的模型。 */
    private final CreakingModel eyesModel;

    public CreakingEyesLayer(RenderLayerParent<Creaking, CreakingModel> renderer, CreakingModel eyesModel) {
        super(renderer);
        this.eyesModel = eyesModel;
    }

    /** 激活（或解体时闪烁）才渲染发光眼睛。 */
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Creaking creaking,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean glowing = creaking.isTearingDown() ? creaking.hasGlowingEyes() : creaking.isActive();
        if (glowing) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(EYES_TEXTURE));
            this.eyesModel.setupAnim(creaking, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.eyesModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
