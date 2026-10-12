package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.pale_oak;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.function.Supplier;

/**
 * 可剥皮原木方块。
 * <p>
 * NeoForge 实现斧头去皮的原木：用斧头右键时返回去皮变体的方块状态
 * （保持当前轴向）。苍白橡木原木与苍白橡木都使用这个方块。
 */
public class StrippableLogBlock extends RotatedPillarBlock {
    /** 去皮后要替换成的方块（延迟解析，避免注册顺序问题）。 */
    private final Supplier<Block> stripped;

    public StrippableLogBlock(BlockBehaviour.Properties properties, Supplier<Block> stripped) {
        super(properties);
        this.stripped = stripped;
    }

    /** 斧头剥皮：返回去皮方块状态并保留轴向；其他工具走原逻辑。 */
    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility == ItemAbilities.AXE_STRIP) {
            return this.stripped.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
