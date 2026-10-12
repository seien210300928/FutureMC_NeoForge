package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.entity.CreakingHeartBlockEntity;

/**
 * Garden Awakens（1.21.4）的方块实体类型注册表。
 * <p>
 * 目前只有嘎吱之心一个方块实体，用于管理嘎吱的生成、游荡半径、
 * 树脂蔓延与模拟信号输出。
 */
public final class ModBlockEntities {
    /** 方块实体类型注册表。 */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "futuremc");

    /** 嘎吱之心方块实体，只挂在嘎吱之心方块上。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreakingHeartBlockEntity>> CREAKING_HEART = BLOCK_ENTITIES.register("creaking_heart",
        () -> BlockEntityType.Builder.of(CreakingHeartBlockEntity::new, ModBlocks.CREAKING_HEART.get()).build(null));

    private ModBlockEntities() {
    }
}
