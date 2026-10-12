package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.CreakingHeartDecorator;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.pale_oak.PaleMossDecorator;

/**
 * Garden Awakens（1.21.4）的树装饰器类型注册表。
 * <p>
 * 树装饰器是树地物在放置树干/树叶后追加装饰的阶段。苍白花园用两个
 * 装饰器：在树上长苍白苔藓（叶子/树干/地面三档概率），以及在树干节点上
 * 放置嘎吱之心（自然生成的嘎吱之心由此而来）。
 */
public final class ModTreeDecorators {
    /** 树装饰器类型注册表。 */
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, "futuremc");

    /** 苍白苔藓装饰器类型，编解码器由 PaleMossDecorator 提供。 */
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<PaleMossDecorator>> PALE_MOSS = TREE_DECORATOR_TYPES.register("pale_moss",
        () -> new TreeDecoratorType(PaleMossDecorator.CODEC));

    /** 嘎吱之心装饰器类型，编解码器由 CreakingHeartDecorator 提供。 */
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<CreakingHeartDecorator>> CREAKING_HEART = TREE_DECORATOR_TYPES.register("creaking_heart",
        () -> new TreeDecoratorType(CreakingHeartDecorator.CODEC));

    private ModTreeDecorators() {
    }
}
