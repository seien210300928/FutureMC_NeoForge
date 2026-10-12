package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModEntities;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client.model.CreakingModel;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client.renderer.entity.CreakingRenderer;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client.renderer.entity.PaleOakBoatRenderer;

/**
 * Garden Awakens（1.21.4）内容的客户端初始化。
 * <p>
 * 在客户端模组总线上注册嘎吱/苍白橡木船的实体渲染器与模型图层定义。
 * 模型图层路径用 futuremc 命名空间，供模型烘焙使用。
 * <p>
 * 注意：不要在这里注册告示牌（sign/hanging_sign）的模型图层。1.21.1 的
 * LayerDefinitions 在静态初始化时会遍历 WoodType.values()（包含 mod 运行时
 * 注册的苍白橡木），已经为 sign/pale_oak 与 hanging_sign/pale_oak 生成了
 * 图层定义；再注册一次会因重复 key 抛 IllegalArgumentException 导致客户端
 * 资源重载失败（加载画面消失卡死）。
 */
@EventBusSubscriber(modid = "futuremc_neoforge", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    /** 嘎吱主体模型图层。 */
    public static final ModelLayerLocation CREAKING_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("futuremc", "creaking"), "main");
    /** 嘎吱眼睛模型图层（发光层）。 */
    public static final ModelLayerLocation CREAKING_EYES_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("futuremc", "creaking"), "eyes");
    /** 苍白橡木船模型图层。 */
    public static final ModelLayerLocation PALE_OAK_BOAT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("futuremc", "boat/pale_oak"), "main");
    /** 苍白橡木箱子船模型图层。 */
    public static final ModelLayerLocation PALE_OAK_CHEST_BOAT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("futuremc", "chest_boat/pale_oak"), "main");

    /** 注册实体渲染器。 */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CREAKING.get(), CreakingRenderer::new);
        event.registerEntityRenderer(ModEntities.PALE_OAK_BOAT.get(), PaleOakBoatRenderer::new);
        event.registerEntityRenderer(ModEntities.PALE_OAK_CHEST_BOAT.get(), PaleOakBoatRenderer::new);
    }

    /** 注册模型图层定义。 */
    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CREAKING_LAYER, CreakingModel::createBodyLayer);
        event.registerLayerDefinition(CREAKING_EYES_LAYER, CreakingModel::createEyesLayer);
        event.registerLayerDefinition(PALE_OAK_BOAT_LAYER, BoatModel::createBodyModel);
        event.registerLayerDefinition(PALE_OAK_CHEST_BOAT_LAYER, ChestBoatModel::createBodyModel);
    }

    private ClientSetup() {
    }
}
