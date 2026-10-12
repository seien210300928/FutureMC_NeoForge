package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModTreeDecorators;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.PaleHangingMossBlock;

/**
 * 苍白苔藓树装饰器。
 * <p>
 * 苍白橡木树地物放置完成后，在树根周围铺苍白苔藓方块、在树干与
 * 树叶下方挂苍白悬挂苔藓。三个概率参数（树叶/树干/地面）由已配置
 * 地物传入，序列化为 leaves_probability、trunk_probability、
 * ground_probability 三个字段。
 */
public class PaleMossDecorator extends TreeDecorator {
    /** 编解码器：三档概率参数。 */
    public static final MapCodec<PaleMossDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.floatRange(0.0F, 1.0F).fieldOf("leaves_probability").forGetter(decorator -> decorator.leavesProbability),
            com.mojang.serialization.Codec.floatRange(0.0F, 1.0F).fieldOf("trunk_probability").forGetter(decorator -> decorator.trunkProbability),
            com.mojang.serialization.Codec.floatRange(0.0F, 1.0F).fieldOf("ground_probability").forGetter(decorator -> decorator.groundProbability)
        ).apply(instance, PaleMossDecorator::new));

    /** 在树叶下挂苔藓的概率。 */
    private final float leavesProbability;
    /** 在树干上挂苔藓的概率。 */
    private final float trunkProbability;
    /** 在树根周围铺苔藓方块的概率。 */
    private final float groundProbability;

    public PaleMossDecorator(float leavesProbability, float trunkProbability, float groundProbability) {
        this.leavesProbability = leavesProbability;
        this.trunkProbability = trunkProbability;
        this.groundProbability = groundProbability;
    }

    /** 返回本装饰器对应的注册类型。 */
    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.PALE_MOSS.get();
    }

    /** 执行装饰：铺地面苔藓 + 在树干/树叶下方挂悬挂苔藓。 */
    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource random = context.random();
        List<BlockPos> logs = Util.shuffledCopy(context.logs(), random);
        if (logs.isEmpty()) {
            return;
        }
        // 以最低的树干节点为基准铺地面苔藓。
        BlockPos origin = Collections.min(logs, Comparator.comparingInt(Vec3i::getY));
        if (random.nextFloat() < this.groundProbability) {
            this.placeGroundMoss(context, origin, random);
        }
        // 树干下方挂苔藓。
        for (BlockPos pos : context.logs()) {
            BlockPos down = pos.below();
            if (random.nextFloat() < this.trunkProbability && context.isAir(down)) {
                addMossHanger(down, context);
            }
        }
        // 树叶下方挂苔藓。
        for (BlockPos pos : context.leaves()) {
            BlockPos down = pos.below();
            if (random.nextFloat() < this.leavesProbability && context.isAir(down)) {
                addMossHanger(down, context);
            }
        }
    }

    /** 在最低树干节点周围铺一小片苍白苔藓方块。 */
    private void placeGroundMoss(TreeDecorator.Context context, BlockPos lowestLog, RandomSource random) {
        BlockPos ground = lowestLog.below();
        if (context.isAir(ground)) {
            context.setBlock(ground, ModBlocks.PALE_MOSS_BLOCK.get().defaultBlockState());
        }
        for (int i = 0; i < 4; i++) {
            BlockPos spread = ground.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            if (context.isAir(spread)) {
                context.setBlock(spread, ModBlocks.PALE_MOSS_BLOCK.get().defaultBlockState());
            }
        }
    }

    /**
     * 从指定位置向下挂一串悬挂苔藓：约 50% 概率每段继续向下生长，
     * 最后一段标记为 tip=true（末端）。
     */
    private static void addMossHanger(BlockPos pos, TreeDecorator.Context context) {
        while (context.isAir(pos.below()) && !(context.random().nextFloat() < 0.5)) {
            context.setBlock(pos, ModBlocks.PALE_HANGING_MOSS.get().defaultBlockState().setValue(PaleHangingMossBlock.TIP, false));
            pos = pos.below();
        }
        context.setBlock(pos, ModBlocks.PALE_HANGING_MOSS.get().defaultBlockState().setValue(PaleHangingMossBlock.TIP, true));
    }
}
