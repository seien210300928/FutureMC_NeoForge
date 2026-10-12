package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.client.renderer.entity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.entity.boat.PaleOakChestBoat;

/**
 * 苍白橡木船渲染器。
 * <p>
 * 普通船与箱子船共用同一个渲染器：按实体类型选择模型（BoatModel 或
 * ChestBoatModel）与纹理（pale_oak.png 或 chest_boat/pale_oak.png）。
 */
public class PaleOakBoatRenderer extends BoatRenderer {
    private static final ModelLayerLocation PALE_OAK_BOAT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("futuremc", "boat/pale_oak"), "main");
    private static final ModelLayerLocation PALE_OAK_CHEST_BOAT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("futuremc", "chest_boat/pale_oak"), "main");
    /** 普通船纹理。 */
    private static final ResourceLocation PALE_OAK_TEXTURE = ResourceLocation.fromNamespaceAndPath("futuremc", "textures/entity/boat/pale_oak.png");
    /** 箱子船纹理。 */
    private static final ResourceLocation PALE_OAK_CHEST_TEXTURE = ResourceLocation.fromNamespaceAndPath("futuremc", "textures/entity/chest_boat/pale_oak.png");

    private final ListModel<Boat> boatModel;
    private final ListModel<Boat> chestBoatModel;

    public PaleOakBoatRenderer(EntityRendererProvider.Context context) {
        super(context, false);
        this.boatModel = new BoatModel(context.bakeLayer(PALE_OAK_BOAT_LAYER));
        this.chestBoatModel = new ChestBoatModel(context.bakeLayer(PALE_OAK_CHEST_BOAT_LAYER));
    }

    /** 按实体类型返回（纹理, 模型）对。 */
    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        return boat instanceof PaleOakChestBoat
            ? Pair.of(PALE_OAK_CHEST_TEXTURE, this.chestBoatModel)
            : Pair.of(PALE_OAK_TEXTURE, this.boatModel);
    }
}
