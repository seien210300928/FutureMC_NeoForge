package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;

/**
 * 花盆里的眼眸花方块。
 * <p>
 * 与普通花盆不同的是，种在花盆里的眼眸花仍然会随昼夜切换开放/闭合
 * 形态（普通花朵放入花盆后是静态的），所以这里重写了随机刻逻辑，
 * 让两种花盆变体之间互相切换。
 */
public class PottedEyeblossomBlock extends FlowerPotBlock {

    public PottedEyeblossomBlock(Block potted, BlockBehaviour.Properties properties) {
        super(potted, properties);
    }

    /** 只有两种眼眸花花盆才有随机刻行为。 */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.is(ModBlocks.POTTED_OPEN_EYEBLOSSOM.get()) || state.is(ModBlocks.POTTED_CLOSED_EYEBLOSSOM.get());
    }

    /** 随机刻：当前形态与昼夜所需形态不符时切换到另一形态并播放粒子/音效。 */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.isRandomlyTicking(state)) {
            boolean isOpen = state.is(ModBlocks.POTTED_OPEN_EYEBLOSSOM.get());
            boolean shouldBeOpen = EyeblossomBlock.shouldBeOpen(level);
            if (isOpen != shouldBeOpen) {
                BlockState opposite = isOpen
                    ? ModBlocks.POTTED_CLOSED_EYEBLOSSOM.get().defaultBlockState()
                    : ModBlocks.POTTED_OPEN_EYEBLOSSOM.get().defaultBlockState();
                level.setBlockAndUpdate(pos, opposite);
                EyeblossomBlock.Type newType = EyeblossomBlock.Type.fromBoolean(isOpen).transform();
                newType.spawnTransformParticle(level, pos, random);
                level.playSound(null, pos, newType.longSwitchSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        super.randomTick(state, level, pos, random);
    }
}
