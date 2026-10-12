package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * 苍白橡木的树生长器。
 * <p>
 * TreeGrower 负责把树苗/骨粉事件映射到具体的树地物：骨粉催熟时生成
 * futuremc:pale_oak_bonemeal 的已配置地物，自然生长（树苗成长）走原版
 * 随机选择的逻辑（这里不指定其他地物，保持为空 Optional）。
 */
public final class ModTreeGrowers {
    /** 骨粉催熟苍白橡木树苗时生成的已配置地物键。 */
    private static final ResourceKey<ConfiguredFeature<?, ?>> PALE_OAK_BONEMEAL = ResourceKey.create(Registries.CONFIGURED_FEATURE,
        ResourceLocation.fromNamespaceAndPath("futuremc", "pale_oak_bonemeal"));

    /** 苍白橡木树生长器实例，注册到树苗方块上。 */
    public static final TreeGrower PALE_OAK = new TreeGrower("pale_oak", Optional.of(PALE_OAK_BONEMEAL), Optional.empty(), Optional.empty());

    private ModTreeGrowers() {
    }
}
