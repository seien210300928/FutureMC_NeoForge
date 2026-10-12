package org.dpdns.seien.futuremc_neoforge.mixin.v21_4;

import com.mojang.datafixers.util.Pair;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * /locate 群系定位修复 mixin。
 * <p>
 * 背景：1.21.1 的 {@code BiomeSource.findClosestBiome3d} 在做垂直扫描时，
 * 用 {@code Mth.outFromOrigin(玩家高度, minY+1, maxY, 64)} 从玩家所在高度
 * 开始向下按 64 步采样。而 1.21.1 的噪声深度（depth）函数带强 y 梯度
 * （y 越高 depth 越低），导致同 xz 不同 y 会落到不同参数域：执行者在
 * 高空（y≈200）时，垂直扫描会先命中地底的 3D 群系变体，把苍白花园
 * 报成"同 xz 的地底黑森林"（y=-34）。26.3 的 depth 由偏移样条直接推导，
 * 对 y 不敏感，因此 26.3 同 xz 只会有一个群系，定位正常。
 * <p>
 * 修复：在进入原版 3D 扫描前，先做一遍"表面优先"的二维扫描——对已加载
 * 区块取 MOTION_BLOCKING 高度图的真实地表 y，未加载区块回退到执行者
 * 高度，只采样该 y 层的群系。地表群系（苍白花园等）命中后直接报在地表
 * y 上，对齐 26.3 的定位体验；若第一遍没命中（说明目标群系是钟乳石
 * 洞窟这类地底群系），就让原版 3D 扫描兜底，保留地底群系的定位能力。
 */
@Mixin(BiomeSource.class)
public abstract class FutureMCLocateBiomeMixin {
    /** 影子方法：获取该群系源所有可能的群系。 */
    @Shadow
    public abstract Set<Holder<Biome>> possibleBiomes();

    /** 影子方法：按方块四分之一坐标采样噪声群系。 */
    @Shadow
    public abstract Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler);

    /** 在进入原版 3D 扫描前，先执行表面优先扫描。 */
    @Inject(method = "findClosestBiome3d", at = @At("HEAD"), cancellable = true)
    private void futuremc$surfaceFirstScan(BlockPos pos, int radius, int horizontalStep, int verticalStep,
                                           Predicate<Holder<Biome>> biomePredicate, Climate.Sampler sampler, LevelReader level,
                                           CallbackInfoReturnable<Pair<BlockPos, Holder<Biome>>> cir) {
        Pair<BlockPos, Holder<Biome>> surfaceResult = this.futuremc$scanSurface(pos, radius, horizontalStep, biomePredicate, sampler, level);
        if (surfaceResult != null) {
            cir.setReturnValue(surfaceResult);
        }
        // 未命中则继续执行原方法（原版 3D 扫描兜底，保留地底群系定位）。
    }

    /**
     * 表面优先扫描：在水平半径内按步长螺旋扫描，每个采样点只取
     * 地表 y（已加载区块用高度图真实地表，未加载区块回退执行者高度），
     * 采样该层的群系。这样地表群系会稳定报在真实地表高度上。
     */
    private Pair<BlockPos, Holder<Biome>> futuremc$scanSurface(BlockPos pos, int radius, int horizontalStep,
                                                               Predicate<Holder<Biome>> biomePredicate, Climate.Sampler sampler, LevelReader level) {
        Set<Holder<Biome>> set = this.possibleBiomes().stream().filter(biomePredicate).collect(Collectors.toUnmodifiableSet());
        if (set.isEmpty()) {
            return null;
        }
        // 执行者高度作为未加载区块时的回退采样高度。
        int fallbackY = pos.getY();
        int minBuild = level.getMinBuildHeight();
        int horizontalRadius = Math.floorDiv(radius, horizontalStep);
        for (BlockPos.MutableBlockPos step : BlockPos.spiralAround(BlockPos.ZERO, horizontalRadius, Direction.EAST, Direction.SOUTH)) {
            int x = pos.getX() + step.getX() * horizontalStep;
            int z = pos.getZ() + step.getZ() * horizontalStep;
            // 已加载区块返回 MOTION_BLOCKING 地表高度；未加载返回最小建造高度，此时回退执行者高度。
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (surfaceY == minBuild) {
                surfaceY = fallbackY;
            }
            int quartX = QuartPos.fromBlock(x);
            int quartY = QuartPos.fromBlock(surfaceY);
            int quartZ = QuartPos.fromBlock(z);
            Holder<Biome> holder = this.getNoiseBiome(quartX, quartY, quartZ, sampler);
            if (set.contains(holder)) {
                return Pair.of(new BlockPos(x, surfaceY, z), holder);
            }
        }
        return null;
    }
}
