package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlockEntities;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModEntities;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModSoundEvents;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.CreakingHeartBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.CreakingHeartState;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.Creaking;

/**
 * 嘎吱之心的方块实体。
 * <p>
 * 负责嘎吱的完整生命周期：夜晚苏醒时在附近生成嘎吱（最多一只）、
 * 记录嘎吱的绑定关系（直接引用或 UUID，跨存档可恢复）、嘎吱受伤时
 * 向树皮蔓延树脂并播放心疼音效、输出距离映射的模拟信号，以及嘎吱
 * 死亡/被移除时的清理。所有服务端行为集中在 serverTick 里。
 */
public class CreakingHeartBlockEntity extends BlockEntity {
    /** 检测玩家的范围（32 格）。 */
    private static final int PLAYER_DETECTION_RANGE = 32;
    /** 嘎吱可游荡的半径（围绕心的 32 格）。 */
    public static final int CREAKING_ROAMING_RADIUS = 32;
    /** 嘎吱离心超过 34 格就判定太远并解散。 */
    private static final int DISTANCE_CREAKING_TOO_FAR = 34;
    /** 生成嘎吱时的水平尝试范围（16 格）。 */
    private static final int SPAWN_RANGE_XZ = 16;
    /** 生成嘎吱时的垂直尝试范围（8 格）。 */
    private static final int SPAWN_RANGE_Y = 8;
    /** 每次生成尝试的次数（5 次）。 */
    private static final int ATTEMPTS_PER_SPAWN = 5;
    /** 状态刷新间隔的基础刻数（20 刻）。 */
    private static final int UPDATE_TICKS = 20;
    /** 状态刷新间隔的随机浮动（0-5 刻）。 */
    private static final int UPDATE_TICKS_VARIANCE = 5;
    /** 心疼音效总时长（100 刻）。 */
    private static final int HURT_CALL_TOTAL_TICKS = 100;
    /** 心疼音效的重复间隔（10 刻）。 */
    private static final int HURT_CALL_INTERVAL = 10;
    /** 心疼粒子持续的前 50 刻。 */
    private static final int HURT_CALL_PARTICLE_TICKS = 50;
    /** 树脂蔓延搜索的最大深度（2 层原木）。 */
    private static final int MAX_DEPTH = 2;
    /** 树脂蔓延搜索的最大访问方块数（64）。 */
    private static final int MAX_COUNT = 64;
    /** UUID 重解析的宽限期（30 刻）。 */
    private static final int TICKS_GRACE_PERIOD = 30;
    /** 无嘎吱时的空 Optional。 */
    private static final Optional<Creaking> NO_CREAKING = Optional.empty();

    /** 绑定的嘎吱：left 为直接引用，right 为存档里的 UUID。 */
    @Nullable
    private Either<Creaking, UUID> creakingInfo;
    /** 方块实体存在的总刻数。 */
    private long ticksExisted;
    /** 状态刷新倒计时。 */
    private int ticker;
    /** 心疼粒子/音效倒计时。 */
    private int emitter;
    /** 心疼音效的目标位置（嘎吱当前位置）。 */
    @Nullable
    private Vec3 emitterTarget;
    /** 当前模拟信号强度。 */
    private int outputSignal;

    /** 生成策略：不能生成在树叶上，其余按碰撞箱顶部生成。 */
    private static final SpawnUtil.Strategy ON_TOP_OF_COLLIDER_NO_LEAVES = (level, targetPos, targetState, attemptedPos, attemptedState) ->
        !targetState.is(BlockTags.LEAVES) && SpawnUtil.Strategy.ON_TOP_OF_COLLIDER.canSpawnOn(level, targetPos, targetState, attemptedPos, attemptedState);

    public CreakingHeartBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.CREAKING_HEART.get(), worldPosition, blockState);
    }

    /**
     * 服务端每刻执行：维护模拟信号、心疼音效进度、状态评估与嘎吱生成/解散。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, CreakingHeartBlockEntity entity) {
        entity.ticksExisted++;
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 模拟信号变化时通知红石邻居。
        int computedOutputSignal = entity.computeAnalogOutputSignal();
        if (entity.outputSignal != computedOutputSignal) {
            entity.outputSignal = computedOutputSignal;
            level.updateNeighbourForOutputSignal(pos, ModBlocks.CREAKING_HEART.get());
        }

        // 心疼音效与粒子：前 50 刻双向喷粒子，之后每 10 刻沿嘎吱位置播放渐弱音效。
        if (entity.emitter > 0) {
            if (entity.emitter > HURT_CALL_PARTICLE_TICKS) {
                entity.emitParticles(serverLevel, 1, true);
                entity.emitParticles(serverLevel, 1, false);
            }
            if (entity.emitter % HURT_CALL_INTERVAL == 0 && entity.emitterTarget != null) {
                entity.getCreakingProtector().ifPresent(creaking -> entity.emitterTarget = creaking.getBoundingBox().getCenter());
                Vec3 heartPosition = Vec3.atCenterOf(pos);
                float progress = 0.2F + 0.8F * (float) (HURT_CALL_TOTAL_TICKS - entity.emitter) / HURT_CALL_TOTAL_TICKS;
                Vec3 soundLocation = heartPosition.subtract(entity.emitterTarget).scale(progress).add(entity.emitterTarget);
                BlockPos soundPos = BlockPos.containing(soundLocation);
                float volume = (float) entity.emitter / 2.0F / HURT_CALL_TOTAL_TICKS + 0.5F;
                serverLevel.playSound(null, soundPos, ModSoundEvents.CREAKING_HEART_HURT.get(), SoundSource.BLOCKS, volume, 1.0F);
            }
            entity.emitter--;
        }

        // 周期性评估状态：刷新方块状态、必要时生成或解散嘎吱。
        if (entity.ticker-- < 0) {
            entity.ticker = entity.level == null ? UPDATE_TICKS : entity.level.getRandom().nextInt(UPDATE_TICKS_VARIANCE) + UPDATE_TICKS;
            BlockState updatedState = updateCreakingState(level, state, pos, entity);
            if (updatedState != state) {
                level.setBlockAndUpdate(pos, updatedState);
                if (updatedState.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.UPROOTED) {
                    return;
                }
            }
            if (entity.creakingInfo == null) {
                // 苏醒、允许自然生成、非和平且 32 格内有玩家时召唤嘎吱。
                if (updatedState.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE
                        && serverLevel.isNaturalSpawningAllowed(pos)
                        && serverLevel.getLevelData().getDifficulty() != Difficulty.PEACEFUL
                        && level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), PLAYER_DETECTION_RANGE, false) != null) {
                    Creaking creaking = spawnProtector(serverLevel, entity);
                    if (creaking != null) {
                        entity.setCreakingInfo(creaking);
                        creaking.playSound(ModSoundEvents.CREAKING_SPAWN.get(), 1.0F, 1.0F);
                        level.playSound(null, entity.getBlockPos(), ModSoundEvents.CREAKING_HEART_SPAWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            } else {
                Optional<Creaking> optionalCreaking = entity.getCreakingProtector();
                if (optionalCreaking.isPresent()) {
                    Creaking creaking = optionalCreaking.get();
                    // 白天、嘎吱被要求持久存在或离心太远时解散；玩家卡在嘎吱体内也解散。
                    if (!level.isNight() && !creaking.isPersistenceRequired()
                            || entity.distanceToCreaking() > DISTANCE_CREAKING_TOO_FAR
                            || creaking.playerIsStuckInYou()) {
                        entity.removeProtector(null);
                    }
                }
            }
        }
    }

    /** 根据原木包围与昼夜刷新嘎吱之心的方块状态。 */
    private static BlockState updateCreakingState(Level level, BlockState state, BlockPos pos, CreakingHeartBlockEntity entity) {
        if (!CreakingHeartBlock.hasRequiredLogs(state, level, pos) && entity.creakingInfo == null) {
            return state.setValue(CreakingHeartBlock.STATE, CreakingHeartState.UPROOTED);
        }
        CreakingHeartState heartState = level.isNight() ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT;
        return state.setValue(CreakingHeartBlock.STATE, heartState);
    }

    /** 嘎吱到嘎吱之心的距离。 */
    private double distanceToCreaking() {
        return this.getCreakingProtector()
            .map(creaking -> Math.sqrt(creaking.distanceToSqr(Vec3.atBottomCenterOf(this.getBlockPos()))))
            .orElse(0.0);
    }

    /** 清除绑定的嘎吱信息。 */
    private void clearCreakingInfo() {
        this.creakingInfo = null;
        this.setChanged();
    }

    /** 绑定嘎吱（直接引用）。 */
    public void setCreakingInfo(Creaking creaking) {
        this.creakingInfo = Either.left(creaking);
        this.setChanged();
    }

    /** 绑定嘎吱（UUID，用于存档恢复）。 */
    public void setCreakingInfo(UUID uuid) {
        this.creakingInfo = Either.right(uuid);
        this.ticksExisted = 0L;
        this.setChanged();
    }

    /**
     * 解析当前绑定的嘎吱：优先直接引用；引用失效后按 UUID 从世界查找，
     * 超过宽限期仍找不到则清除绑定。
     */
    private Optional<Creaking> getCreakingProtector() {
        if (this.creakingInfo == null) {
            return NO_CREAKING;
        }
        if (this.creakingInfo.left().isPresent()) {
            Creaking creaking = this.creakingInfo.left().get();
            if (!creaking.isRemoved()) {
                return Optional.of(creaking);
            }
            this.setCreakingInfo(creaking.getUUID());
        }
        if (this.level instanceof ServerLevel serverLevel && this.creakingInfo.right().isPresent()) {
            UUID uuid = this.creakingInfo.right().get();
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof Creaking resolvedCreaking) {
                this.setCreakingInfo(resolvedCreaking);
                return Optional.of(resolvedCreaking);
            }
            if (this.ticksExisted >= TICKS_GRACE_PERIOD) {
                this.clearCreakingInfo();
            }
        }
        return NO_CREAKING;
    }

    /** 在嘎吱之心附近生成嘎吱，绑定回家位置并广播生成事件。 */
    @Nullable
    private static Creaking spawnProtector(ServerLevel level, CreakingHeartBlockEntity entity) {
        BlockPos pos = entity.getBlockPos();
        Optional<Creaking> spawnedMob = SpawnUtil.trySpawnMob(ModEntities.CREAKING.get(), MobSpawnType.SPAWNER, level, pos,
            ATTEMPTS_PER_SPAWN, SPAWN_RANGE_XZ, SPAWN_RANGE_Y, ON_TOP_OF_COLLIDER_NO_LEAVES);
        if (spawnedMob.isEmpty()) {
            return null;
        }
        Creaking spawnedCreaking = spawnedMob.get();
        level.gameEvent(GameEvent.ENTITY_PLACE, pos, GameEvent.Context.of(spawnedCreaking));
        level.broadcastEntityEvent(spawnedCreaking, (byte) 60);
        spawnedCreaking.setHomePos(pos);
        return spawnedCreaking;
    }

    /** 客户端同步用的更新包。 */
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** 客户端同步用的更新标签。 */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    /**
     * 嘎吱受伤时的心疼反馈：喷出心疼粒子（橙向嘎吱、灰向心）、
     * 在树皮上蔓延 2-3 处树脂簇，并启动 100 刻的心疼音效序列。
     */
    public void creakingHurt() {
        Optional<Creaking> creaking = this.getCreakingProtector();
        if (creaking.isEmpty() || !(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.emitter <= 0) {
            this.emitParticles(serverLevel, 50, false);
            if (this.getBlockState().getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE) {
                int numberOfClumps = this.level.getRandom().nextIntBetweenInclusive(2, 3);
                for (int i = 0; i < numberOfClumps; i++) {
                    this.spreadResin(serverLevel).ifPresent(blockPos -> {
                        this.level.playSound(null, blockPos, ModSoundEvents.RESIN_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        this.level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(this.getBlockState()));
                    });
                }
            }
            this.emitter = HURT_CALL_TOTAL_TICKS;
            this.emitterTarget = creaking.get().getBoundingBox().getCenter();
        }
    }

    /**
     * 从嘎吱之心出发，在苍白橡木原木表面蔓延一块树脂簇：
     * 广度优先搜索相邻原木（最多 64 个、深 2 层），随机找一块空气
     * 或水源面贴树脂簇。
     */
    private Optional<BlockPos> spreadResin(ServerLevel level) {
        RandomSource random = level.getRandom();
        MutableObject<BlockPos> placedResin = new MutableObject<>();
        ArrayDeque<Pair<BlockPos, Integer>> queue = new ArrayDeque<>();
        HashSet<BlockPos> visited = new HashSet<>();
        queue.add(Pair.of(this.worldPosition, 0));
        visited.add(this.worldPosition);

        while (!queue.isEmpty() && placedResin.getValue() == null && visited.size() <= MAX_COUNT) {
            Pair<BlockPos, Integer> current = queue.poll();
            BlockPos pos = current.getFirst();
            int depth = current.getSecond();
            if (level.getBlockState(pos).is(ModBlocks.PALE_OAK_LOGS)) {
                for (Direction dir : shuffledDirections(random)) {
                    BlockPos neighborPos = pos.relative(dir);
                    BlockState neighborState = level.getBlockState(neighborPos);
                    Direction opposite = dir.getOpposite();
                    BlockState resinState;
                    if (neighborState.isAir()) {
                        resinState = ModBlocks.RESIN_CLUMP.get().defaultBlockState();
                    } else {
                        // 只有水源才允许含水树脂簇。
                        if (!neighborState.is(Blocks.WATER) || !neighborState.getFluidState().isSource()) {
                            continue;
                        }
                        resinState = ModBlocks.RESIN_CLUMP.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
                    }
                    // 目标面还没被树脂占用且能附着才放置。
                    if (level.getBlockState(neighborPos).is(ModBlocks.RESIN_CLUMP.get())
                            || !MultifaceBlock.canAttachTo(level, opposite, neighborPos, level.getBlockState(pos))) {
                        continue;
                    }
                    level.setBlockAndUpdate(neighborPos, resinState.setValue(MultifaceBlock.getFaceProperty(opposite), true));
                    placedResin.setValue(neighborPos);
                    break;
                }
            }
            // 深度未满时继续扩展相邻原木。
            if (depth >= MAX_DEPTH) {
                continue;
            }
            for (Direction dir : shuffledDirections(random)) {
                BlockPos neighborPos = pos.relative(dir);
                if (!level.getBlockState(neighborPos).is(ModBlocks.PALE_OAK_LOGS) || !visited.add(neighborPos)) {
                    continue;
                }
                queue.add(Pair.of(neighborPos, depth + 1));
            }
        }
        return Optional.ofNullable(placedResin.getValue());
    }

    /** 打乱方向顺序，让蔓延路径随机。 */
    private static Direction[] shuffledDirections(RandomSource random) {
        Direction[] directions = Direction.values();
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = directions[i];
            directions[i] = directions[j];
            directions[j] = tmp;
        }
        return directions;
    }

    /**
     * 在嘎吱身上（或心上）喷彩色粒子：
     * 橙色调（16545810）表示心疼方向朝向嘎吱，灰色调（0x5F5F5F）表示朝向心。
     */
    private void emitParticles(ServerLevel serverLevel, int count, boolean towardsCreaking) {
        Optional<Creaking> creaking = this.getCreakingProtector();
        if (creaking.isEmpty()) {
            return;
        }
        int color = towardsCreaking ? Creaking.CREAKING_ORANGE : Creaking.CREAKING_GRAY;
        RandomSource random = serverLevel.getRandom();
        for (double i = 0.0; i < count; i += 1.0) {
            AABB box = creaking.get().getBoundingBox();
            Vec3 source = box.getMinPosition().add(
                random.nextDouble() * box.getXsize(), random.nextDouble() * box.getYsize(), random.nextDouble() * box.getZsize());
            Vec3 destination = Vec3.atLowerCornerOf(this.getBlockPos()).add(random.nextDouble(), random.nextDouble(), random.nextDouble());
            if (towardsCreaking) {
                Vec3 foo = source;
                source = destination;
                destination = foo;
            }
            ColorParticleOption particleOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color);
            serverLevel.sendParticles(particleOption, source.x, source.y, source.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * 解除嘎吱的绑定并处理其结局：
     * 无伤害来源时直接拆除（自然解散），有伤害来源时先执行死亡效果再归零生命。
     */
    public void removeProtector(@Nullable DamageSource damageSource) {
        Optional<Creaking> creakingProtector = this.getCreakingProtector();
        if (creakingProtector.isPresent()) {
            Creaking creaking = creakingProtector.get();
            if (damageSource == null) {
                creaking.tearDown();
            } else {
                creaking.creakingDeathEffects(damageSource);
                creaking.setTearingDown();
                creaking.setHealth(0.0F);
            }
            this.clearCreakingInfo();
        }
    }

    /** 判断指定嘎吱是否是这个心绑定的守护者。 */
    public boolean isProtector(Creaking creaking) {
        return this.getCreakingProtector().map(c -> c == creaking).orElse(false);
    }

    /** 返回当前模拟信号强度。 */
    public int getAnalogOutputSignal() {
        return this.outputSignal;
    }

    /** 计算模拟信号强度：嘎吱越近信号越强（1-15），无嘎吱为 0。 */
    public int computeAnalogOutputSignal() {
        if (this.creakingInfo != null && !this.getCreakingProtector().isEmpty()) {
            double distance = this.distanceToCreaking();
            double scaledDistance = Mth.clamp(distance, 0.0, CREAKING_ROAMING_RADIUS) / CREAKING_ROAMING_RADIUS;
            return 15 - (int) Math.floor(scaledDistance * 15.0);
        }
        return 0;
    }

    /** 读取存档：恢复绑定的嘎吱 UUID。 */
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("creaking")) {
            this.setCreakingInfo(tag.getUUID("creaking"));
        } else {
            this.clearCreakingInfo();
        }
    }

    /** 写入存档：保存绑定的嘎吱 UUID。 */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.creakingInfo != null) {
            UUID uuid = this.creakingInfo.map(Entity::getUUID, uuid2 -> uuid2);
            tag.putUUID("creaking", uuid);
        }
    }
}
