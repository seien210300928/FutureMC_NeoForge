package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModSoundEvents;

/**
 * 苍白悬挂苔藓方块。
 * <p>
 * 从方块底面垂下的一串苔藓：借助 tip 属性区分末端（末端的碰撞箱
 * 稍短，视觉上是尖的）。只能附着在方块底面或另一段悬挂苔藓下方，
 * 可用骨粉向下方继续生长，也是骨粉目标方块。
 */
public class PaleHangingMossBlock extends Block implements BonemealableBlock {
    /** 非末端的碰撞箱（完整 16 格高）。 */
    private static final VoxelShape SHAPE_BASE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    /** 末端的碰撞箱（底部收窄）。 */
    private static final VoxelShape SHAPE_TIP = Block.box(1.0, 2.0, 1.0, 15.0, 16.0, 15.0);
    /** 是否为一段苔藓的末端。 */
    public static final BooleanProperty TIP = BooleanProperty.create("tip");

    public PaleHangingMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TIP, true));
    }

    /** 末端使用收窄的碰撞箱。 */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TIP) ? SHAPE_TIP : SHAPE_BASE;
    }

    /** 客户端环境音：上方是苍白橡木原木或树叶时随机播放。 */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(500) == 0) {
            BlockState above = level.getBlockState(pos.above());
            if (above.is(ModBlocks.PALE_OAK_LOGS) || above.is(ModBlocks.PALE_OAK_LEAVES.get())) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), ModSoundEvents.PALE_HANGING_MOSS_IDLE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
            }
        }
    }

    /** 悬挂苔藓不阻挡天光。 */
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    /** 生存条件：上方能附着（方块底面或另一段苔藓）。 */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.canStayAtPosition(level, pos);
    }

    /** 判断当前位置是否可停留：上方是完整方块底面或同种苔藓。 */
    private boolean canStayAtPosition(BlockGetter level, BlockPos pos) {
        BlockPos neighborPos = pos.relative(Direction.UP);
        BlockState neighborState = level.getBlockState(neighborPos);
        return MultifaceBlock.canAttachTo(level, Direction.UP, neighborPos, neighborState) || neighborState.is(this);
    }

    /** 邻居变化：无法停留时计划破坏，并刷新 tip 状态（下方是否还有苔藓）。 */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!this.canStayAtPosition(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return state.setValue(TIP, !level.getBlockState(pos.below()).is(this));
    }

    /** 刻处理：无法停留则整段破坏。 */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canStayAtPosition(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP);
    }

    /** 骨粉目标有效条件：末端的下方是空气且在建造高度内。 */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos growPos = this.getTip(level, pos).below();
        return this.canGrowInto(level.getBlockState(growPos)) && !level.isOutsideBuildHeight(growPos);
    }

    private boolean canGrowInto(BlockState state) {
        return state.isAir();
    }

    /** 向下走到一串苔藓的最末端（下方不再是苔藓的位置的上一格）。 */
    public BlockPos getTip(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos forwardPos = pos.mutable();
        BlockState forwardState;
        do {
            forwardPos.move(Direction.DOWN);
            forwardState = level.getBlockState(forwardPos);
        } while (forwardState.is(this));
        return forwardPos.relative(Direction.UP).immutable();
    }

    /** 骨粉必定成功。 */
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    /** 执行骨粉：在末端下方生长一段新的苔藓（新的一段默认是末端）。 */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos tipPos = this.getTip(level, pos).below();
        if (this.canGrowInto(level.getBlockState(tipPos))) {
            level.setBlockAndUpdate(tipPos, state.setValue(TIP, true));
        }
    }
}
