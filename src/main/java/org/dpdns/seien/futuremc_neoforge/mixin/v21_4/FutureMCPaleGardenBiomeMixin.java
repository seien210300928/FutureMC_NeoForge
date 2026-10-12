package org.dpdns.seien.futuremc_neoforge.mixin.v21_4;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 主世界群系表注入 mixin。
 * <p>
 * 1.21.1 的主世界群系参数表里，PLATEAU_BIOMES 的第 3 温度带（-0.15~0.2）、
 * 最湿湿度带（0.3~1.0）第 5 列仍指向黑森林（dark_forest）；26.3 原版
 * 把该位置换成了苍白花园（pale_garden）。
 * 本 mixin 在 {@code MultiNoiseBiomeSourceParameterList.Preset.generateOverworldBiomes}
 * 返回后，把原表中该位置对应的"高原+黑森林"参数点替换为 futuremc:pale_garden 的
 * 参数点（占用点被其他群系占据时跳过，避免覆盖其他群系）。
 * <p>
 * 目标替换的参数域与 26.3 原版一致：温度 -0.15~0.2、湿度 0.3~1.0、
 * 大陆度/侵蚀度两组高原组合（erosions[2]、erosions[3]）、addHighSlice 的
 * 4 个怪诞度切片、每个点 depth=0/1 两个版本（地表/地底，共 16 个点）。
 */
@Mixin(MultiNoiseBiomeSourceParameterList.Preset.class)
public abstract class FutureMCPaleGardenBiomeMixin {
    /** 移植的苍白花园群系键。 */
    @Unique
    private static final ResourceKey<Biome> FUTUREMC_PALE_GARDEN = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("futuremc", "pale_garden"));

    /** 苍白花园的温度参数域（偏凉）。 */
    @Unique
    private static final Climate.Parameter TEMP_COOL = Climate.Parameter.span(-0.15F, 0.2F);
    /** 苍白花园的湿度参数域（偏湿）。 */
    @Unique
    private static final Climate.Parameter HUMID_WET = Climate.Parameter.span(0.3F, 1.0F);
    /** 26.3 原版 PLATEAU_BIOMES[2][4]（高原·温带·最湿列）被苍白花园替换。
     *  对应 addHighSlice 里 pickPlateauBiome 生成的两组（大陆度, 侵蚀度）参数点。 */
    @Unique
    private static final List<Pair<Climate.Parameter, Climate.Parameter>> PLATEAU_COMBOS = List.of(
        Pair.of(Climate.Parameter.span(0.03F, 1.0F), Climate.Parameter.span(-0.375F, -0.2225F)),
        Pair.of(Climate.Parameter.span(0.3F, 1.0F), Climate.Parameter.span(-0.2225F, 0.05F))
    );
    /** addHighSlice 被调用的 4 个怪诞度切片（高原群系只在这 4 个切片出现）。 */
    @Unique
    private static final List<Climate.Parameter> WEIRDNESS_SLICES = List.of(
        Climate.Parameter.span(-0.93333334F, -0.7666667F),
        Climate.Parameter.span(-0.56666666F, -0.4F),
        Climate.Parameter.span(0.4F, 0.56666666F),
        Climate.Parameter.span(0.7666667F, 0.93333334F)
    );

    /** 在生成完主世界群系表后，把高原黑森林参数点替换为苍白花园。
     *  原版 addSurfaceBiome 对每个点生成 depth=0（地表）与 depth=1（地底）两个版本，
     *  这里同样处理两个 depth，确保苍白花园在地表和地底都接管原黑森林的位置。 */
    @Inject(method = "generateOverworldBiomes", at = @At("RETURN"), cancellable = true)
    private static <T> void futuremc$addPaleGarden(Function<ResourceKey<Biome>, T> valueGetter, CallbackInfoReturnable<Climate.ParameterList<T>> cir) {
        Climate.ParameterList<T> original = cir.getReturnValue();
        List<Pair<Climate.ParameterPoint, T>> points = new ArrayList<>(original.values());
        for (Climate.Parameter weirdness : WEIRDNESS_SLICES) {
            for (Pair<Climate.Parameter, Climate.Parameter> combo : PLATEAU_COMBOS) {
                for (Climate.Parameter depth : List.of(Climate.Parameter.point(0.0F), Climate.Parameter.point(1.0F))) {
                    Climate.ParameterPoint point = Climate.parameters(TEMP_COOL, HUMID_WET, combo.getFirst(), combo.getSecond(),
                        depth, weirdness, 0.0F);
                    // 该参数点已被其他非黑森林群系占用时跳过，避免覆盖别人的群系。
                    boolean occupiedByOther = points.stream().anyMatch(p -> p.getFirst().equals(point) && !futuremc$isDarkForest(p.getSecond()));
                    if (occupiedByOther) {
                        continue;
                    }
                    // 移除同参数点的黑森林，换成苍白花园。
                    points.removeIf(p -> p.getFirst().equals(point) && futuremc$isDarkForest(p.getSecond()));
                    points.add(Pair.of(point, valueGetter.apply(FUTUREMC_PALE_GARDEN)));
                }
            }
        }
        cir.setReturnValue(new Climate.ParameterList<>(points));
    }

    /** 判断群系值是否为黑森林（支持 Holder 与 ResourceKey 两种形态）。 */
    @Unique
    private static boolean futuremc$isDarkForest(Object value) {
        if (value instanceof Holder<?> holder) {
            return holder.unwrapKey().map(key -> key.equals(Biomes.DARK_FOREST)).orElse(false);
        }
        return value instanceof ResourceKey<?> key && key.equals(Biomes.DARK_FOREST);
    }
}
