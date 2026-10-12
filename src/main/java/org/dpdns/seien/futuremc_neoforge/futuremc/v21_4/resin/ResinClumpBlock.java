package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.resin;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 树脂簇方块。
 * <p>
 * 多面附着方块（像发光地衣一样能贴在任意面上），由嘎吱之心受伤时
 * 向周围苍白橡木原木蔓延生成。可含水（waterlogged），用来在树皮上
 * 呈现树脂渗出效果。
 */
public class ResinClumpBlock extends MultifaceBlock {
    /** 蔓延器：控制树脂簇向相邻方块面生长。 */
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public ResinClumpBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** 方块编解码器：按构造函数注册。 */
    @Override
    protected MapCodec<? extends ResinClumpBlock> codec() {
        return BlockBehaviour.simpleCodec(ResinClumpBlock::new);
    }

    /** 返回蔓延器，供地物与蔓延逻辑使用。 */
    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }
}
