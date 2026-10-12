package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModTreeDecorators;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.CreakingHeartBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.state.properties.CreakingHeartState;

/**
 * 嘎吱之心树装饰器。
 * <p>
 * 苍白橡木树地物放置完成后，按概率从树干节点中挑一个被六个方向
 * 原木完全包围的位置放上沉睡的嘎吱之心（natural=true，表示自然生成，
 * 破坏时会掉经验）。编解码器只含一个概率参数。
 */
public class CreakingHeartDecorator extends TreeDecorator {
    /** 编解码器：概率参数（0-1），序列化为 "probability" 字段。 */
    public static final MapCodec<CreakingHeartDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
        .fieldOf("probability")
        .xmap(CreakingHeartDecorator::new, decorator -> decorator.probability);

    /** 生成嘎吱之心的概率。 */
    private final float probability;

    public CreakingHeartDecorator(float probability) {
        this.probability = probability;
    }

    /** 返回本装饰器对应的注册类型。 */
    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.CREAKING_HEART.get();
    }

    /** 执行装饰：随机挑一个被原木完全包围的树干节点放置嘎吱之心。 */
    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource random = context.random();
        ObjectArrayList<BlockPos> logs = context.logs();
        if (logs.isEmpty() || random.nextFloat() >= this.probability) {
            return;
        }
        // 打乱树干节点顺序，优先选被六面原木完全包围的位置。
        ArrayList<BlockPos> heartPlacements = new ArrayList<>(logs);
        Util.shuffle(heartPlacements, random);
        Optional<BlockPos> targetPos = heartPlacements.stream().filter(pos -> {
            for (Direction direction : Direction.values()) {
                if (!context.level().isStateAtPosition(pos.relative(direction), state -> state.is(BlockTags.LOGS))) {
                    return false;
                }
            }
            return true;
        }).findFirst();
        // 放上沉睡状态的嘎吱之心并标记为自然生成。
        if (targetPos.isPresent()) {
            context.setBlock(targetPos.get(), ModBlocks.CREAKING_HEART.get().defaultBlockState()
                .setValue(CreakingHeartBlock.STATE, CreakingHeartState.DORMANT)
                .setValue(CreakingHeartBlock.NATURAL, true));
        }
    }
}
