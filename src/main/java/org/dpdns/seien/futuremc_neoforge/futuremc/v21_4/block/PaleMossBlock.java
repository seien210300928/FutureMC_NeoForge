package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * 苍白苔藓方块。
 * <p>
 * 与普通苔藓方块一样可被骨粉催熟：对上方空气位放置
 * futuremc:pale_moss_patch_bonemeal 的已配置地物来蔓延苔藓，
 * 属于邻居蔓延型骨粉目标。
 */
public class PaleMossBlock extends Block implements BonemealableBlock {
    /** 骨粉催熟时放置的苍白苔藓斑块地物键。 */
    private static final ResourceKey<ConfiguredFeature<?, ?>> PALE_MOSS_PATCH_BONEMEAL = ResourceKey.create(Registries.CONFIGURED_FEATURE,
        ResourceLocation.fromNamespaceAndPath("futuremc", "pale_moss_patch_bonemeal"));

    public PaleMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** 骨粉目标有效条件：上方是空气。 */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    /** 骨粉必定成功。 */
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    /** 执行骨粉：在上方放置苍白苔藓斑块地物。 */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        Registry<ConfiguredFeature<?, ?>> configuredFeatures = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> feature = configuredFeatures.getHolderOrThrow(PALE_MOSS_PATCH_BONEMEAL);
        feature.value().place(level, level.getChunkSource().getGenerator(), random, pos.above());
    }

    /** 骨粉类型：向邻居蔓延。 */
    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}
