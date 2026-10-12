package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlockEntities;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModSoundEvents;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.CreakingHeartBlockEntity;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.CreakingHeartState;

/**
 * 嘎吱之心方块。
 * <p>
 * 苍白花园的核心机制方块：沿轴线放置，周围六个面必须被同轴向的
 * 苍白橡木原木包围才能正常工作。夜晚自动苏醒并召唤嘎吱，白天或
 * 失去原木后回到沉睡/被拔除状态。带方块实体管理嘎吱行为，并且
 * 能输出模拟信号（距离越近信号越强）。
 */
public class CreakingHeartBlock extends BaseEntityBlock {
    /** 轴向属性：与方块轮换共享，Y 轴为默认。 */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    /** 状态属性：被拔除/沉睡/苏醒（属性名与 26.3 原版一致，供方块状态 JSON 使用）。 */
    public static final EnumProperty<CreakingHeartState> STATE = EnumProperty.create("creaking_heart_state", CreakingHeartState.class);
    /** 是否为自然生成的嘎吱之心（自然生成被破坏时掉落经验）。 */
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    public CreakingHeartBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(AXIS, Direction.Axis.Y)
            .setValue(STATE, CreakingHeartState.UPROOTED)
            .setValue(NATURAL, false));
    }

    /** 方块编解码器：按构造函数注册。 */
    @Override
    protected MapCodec<? extends CreakingHeartBlock> codec() {
        return BlockBehaviour.simpleCodec(CreakingHeartBlock::new);
    }

    /** 创建嘎吱之心方块实体。 */
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreakingHeartBlockEntity(pos, state);
    }

    /**
     * 渲染形状：嘎吱之心用普通方块模型渲染（模型/纹理在资源包里），
     * 方块实体只负责逻辑（苏醒/召唤嘎吱/红石信号）。
     * <p>
     * 注意：1.21.1 的 {@link BaseEntityBlock#getRenderShape} 默认返回
     * {@link RenderShape#INVISIBLE}（不渲染），26.3 原版嘎吱之心继承自
     * {@code Block} 的默认 {@code MODEL}。若此处不覆盖，方块会完全透明，
     * 必须显式返回 {@link RenderShape#MODEL} 让区块按方块模型绘制。
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** 服务端才挂 ticker：被拔除时不需要任何行为。 */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return state.getValue(STATE) != CreakingHeartState.UPROOTED
            ? createTickerHelper(type, ModBlockEntities.CREAKING_HEART.get(), CreakingHeartBlockEntity::serverTick)
            : null;
    }

    /** 客户端粒子/音效：夜晚、未拔除、被原木包围时随机播放空闲音。 */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.isNight() && state.getValue(STATE) != CreakingHeartState.UPROOTED
                && random.nextInt(16) == 0 && isSurroundedByLogs(level, pos)) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), ModSoundEvents.CREAKING_HEART_IDLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    /** 邻居变化时安排一次刻，用来重新评估原木包围情况。 */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, this, 1);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /** 刻处理：根据原木与昼夜刷新状态。 */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState newState = updateState(state, level, pos);
        if (newState != state) {
            level.setBlockAndUpdate(pos, newState);
        }
    }

    /**
     * 计算嘎吱之心的最新状态：被拔除时若原木齐备则尝试苏醒/沉睡，
     * 否则保持现状等待下一次评估。
     */
    private static BlockState updateState(BlockState state, Level level, BlockPos pos) {
        boolean hasLogs = hasRequiredLogs(state, level, pos);
        boolean disabled = state.getValue(STATE) == CreakingHeartState.UPROOTED;
        return hasLogs && disabled
            ? state.setValue(STATE, level.isNight() ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT)
            : state;
    }

    /**
     * 检查嘎吱之心是否被足够多的苍白橡木原木包围：
     * 与自身轴向垂直的四个方向上各有一个同轴向的苍白橡木原木。
     * （语义对齐 26.3：26.3 用 {@code Direction.Axis.getDirections()}，
     * 1.21.1 无此方法，这里手写等价的方向集合——Y 轴查水平四向。）
     */
    public static boolean hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos) {
        Direction.Axis axis = state.getValue(AXIS);
        for (Direction direction : directionsOf(axis)) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (!neighbor.is(ModBlocks.PALE_OAK_LOGS) || neighbor.getValue(AXIS) != axis) {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回与指定轴向垂直的四个方向（等价于 26.3 的 {@code Axis.getDirections()}）：
     * X 轴查上下前后，Y 轴查水平四向，Z 轴查上下左右。
     */
    private static Direction[] directionsOf(Direction.Axis axis) {
        return switch (axis) {
            case X -> new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
            case Y -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
            case Z -> new Direction[]{Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST};
        };
    }

    /** 检查嘎吱之心是否被六个方向的原木完全包围（用于环境音判定）。 */
    private static boolean isSurroundedByLogs(LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (!neighbor.is(ModBlocks.PALE_OAK_LOGS)) {
                return false;
            }
        }
        return true;
    }

    /** 放置时按点击的面确定轴向，并立即评估状态。 */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateState(this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis()), context.getLevel(), context.getClickedPos());
    }

    /** 旋转：按轴向方块的标准旋转处理。 */
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return RotatedPillarBlock.rotatePillar(state, rotation);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, STATE, NATURAL);
    }

    /** 方块被替换时通知邻居与红石信号刷新。 */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) {
            for (Direction direction : Direction.values()) {
                level.neighborChanged(pos.relative(direction), state.getBlock(), pos);
            }
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    /** 玩家破坏方块前：让嘎吱执行死亡效果并发放经验。 */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CreakingHeartBlockEntity creakingHeartBlockEntity) {
            creakingHeartBlockEntity.removeProtector(player.damageSources().playerAttack(player));
            this.tryAwardExperience(player, state, level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * 爆炸命中时：若爆炸会破坏方块实体（DESTROY / DESTROY_WITH_DECAY），
     * 让保护者嘎吱执行死亡效果，并由爆炸间接来源（玩家）发放经验。
     * <p>
     * 语义对齐 26.3 的 {@code onExplosionHit}（26.3 用 {@code ServerExplosion.getDamageSource()}
     * 与 {@code shouldAffectBlocklikeEntities()}，1.21.1 没有这两个 API，这里改用
     * {@link Explosion#getDefaultDamageSource} 与 {@link Explosion.BlockInteraction} 等价判定）。
     */
    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CreakingHeartBlockEntity creakingHeartBlockEntity) {
                Explosion.BlockInteraction interaction = explosion.getBlockInteraction();
                if (interaction == Explosion.BlockInteraction.DESTROY || interaction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                    creakingHeartBlockEntity.removeProtector(Explosion.getDefaultDamageSource(level, explosion.getIndirectSourceEntity()));
                    if (explosion.getIndirectSourceEntity() instanceof Player player) {
                        this.tryAwardExperience(player, state, level, pos);
                    }
                }
            }
        }
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    /** 自然生成的嘎吱之心被破坏时掉落 20-24 点经验。 */
    private void tryAwardExperience(Player player, BlockState state, Level level, BlockPos pos) {
        if (!player.isSpectator() && state.getValue(NATURAL) && level instanceof ServerLevel serverLevel) {
            this.popExperience(serverLevel, pos, level.getRandom().nextIntBetweenInclusive(20, 24));
        }
    }

    /** 嘎吱之心支持红石模拟信号输出。 */
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** 模拟信号强度：被拔除为 0，否则按嘎吱距离映射到 1-15。 */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (state.getValue(STATE) == CreakingHeartState.UPROOTED) {
            return 0;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CreakingHeartBlockEntity creakingHeartBlockEntity) {
            return creakingHeartBlockEntity.getAnalogOutputSignal();
        }
        return 0;
    }
}
