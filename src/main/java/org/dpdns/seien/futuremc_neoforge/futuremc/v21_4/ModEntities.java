package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.entity.Creaking;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.entity.boat.PaleOakBoat;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.entity.boat.PaleOakChestBoat;

/**
 * Garden Awakens（1.21.4）的实体类型注册表。
 * <p>
 * 包含嘎吱（敌对生物）与苍白橡木船/箱子船（载具实体），全部使用
 * futuremc 命名空间，尺寸与视线高度按 26.3 原版实体类型配置。
 */
public final class ModEntities {
    /** 实体类型注册表。 */
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "futuremc");

    /** 嘎吱：苍白花园的守卫生物，2.7 格高、0.9 格宽，客户端跟踪距离 8 区块。 */
    public static final DeferredHolder<EntityType<?>, EntityType<Creaking>> CREAKING = ENTITY_TYPES.register("creaking",
        () -> EntityType.Builder.of(Creaking::new, MobCategory.MONSTER)
            .sized(0.9F, 2.7F)
            .eyeHeight(2.3F)
            .clientTrackingRange(8)
            .build("creaking"));

    /** 苍白橡木船：载具实体，尺寸与原版船一致。 */
    public static final DeferredHolder<EntityType<?>, EntityType<PaleOakBoat>> PALE_OAK_BOAT = ENTITY_TYPES.register("pale_oak_boat",
        () -> EntityType.Builder.<PaleOakBoat>of((type, level) -> new PaleOakBoat(type, level), MobCategory.MISC)
            .sized(1.375F, 0.5625F)
            .eyeHeight(0.5625F)
            .clientTrackingRange(10)
            .build("pale_oak_boat"));

    /** 苍白橡木箱子船。 */
    public static final DeferredHolder<EntityType<?>, EntityType<PaleOakChestBoat>> PALE_OAK_CHEST_BOAT = ENTITY_TYPES.register("pale_oak_chest_boat",
        () -> EntityType.Builder.<PaleOakChestBoat>of((type, level) -> new PaleOakChestBoat(type, level), MobCategory.MISC)
            .sized(1.375F, 0.5625F)
            .eyeHeight(0.5625F)
            .clientTrackingRange(10)
            .build("pale_oak_chest_boat"));

    private ModEntities() {
    }
}
