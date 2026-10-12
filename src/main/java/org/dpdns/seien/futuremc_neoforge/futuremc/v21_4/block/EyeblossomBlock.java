package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModSoundEvents;

/**
 * 眼眸花方块。
 * <p>
 * 苍白花园的双态植物：白天开放（开放眼眸花）、夜晚闭合（闭合眼眸花），
 * 通过随机刻与计划刻在两种形态间切换，切换时播放粒子与音效。蜜蜂接触
 * 开放眼眸花会中毒。花盆里也有对应变体（见 PottedEyeblossomBlock）。
 */
public class EyeblossomBlock extends FlowerBlock {
    /** 形态切换时影响附近同类的水平范围（3 格）。 */
    private static final int EYEBLOSSOM_XZ_RANGE = 3;
    /** 形态切换时影响附近同类的垂直范围（2 格）。 */
    private static final int EYEBLOSSOM_Y_RANGE = 2;

    /** 当前形态（开放或闭合）。 */
    private final Type type;

    public EyeblossomBlock(Type type, BlockBehaviour.Properties properties) {
        super(type.effect, type.effectDuration, properties);
        this.type = type;
    }

    /** 客户端环境音：开放形态且下方是苍白苔藓方块时随机播放生长音。 */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.type.emitSounds() && random.nextInt(700) == 0) {
            BlockState below = level.getBlockState(pos.below());
            if (below.is(ModBlocks.PALE_MOSS_BLOCK.get())) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), ModSoundEvents.EYEBLOSSOM_IDLE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
            }
        }
    }

    /** 随机刻：昼夜更替触发的长音切换（开放↔闭合）。 */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.tryChangingState(state, level, pos, random)) {
            level.playSound(null, pos, this.type.transform().longSwitchSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /** 计划刻：由附近同类传播来的短音切换。 */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.tryChangingState(state, level, pos, random)) {
            level.playSound(null, pos, this.type.transform().shortSwitchSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /**
     * 尝试切换形态：当前时间需要的形态与自身形态不一致时，
     * 更新方块并让半径 3x2x3 内的同类按距离错开计划刻跟随切换。
     */
    private boolean tryChangingState(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean shouldBeOpen = shouldBeOpen(level);
        if (shouldBeOpen == this.type.open) {
            return false;
        }
        Type newType = this.type.transform();
        level.setBlockAndUpdate(pos, newType.state());
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
        newType.spawnTransformParticle(level, pos, random);
        // 让附近同种眼眸花延迟跟随切换，形成波浪效果。
        BlockPos.betweenClosedStream(pos.offset(-EYEBLOSSOM_XZ_RANGE, -EYEBLOSSOM_Y_RANGE, -EYEBLOSSOM_XZ_RANGE),
                pos.offset(EYEBLOSSOM_XZ_RANGE, EYEBLOSSOM_Y_RANGE, EYEBLOSSOM_XZ_RANGE))
            .forEach(nearbyPos -> {
                if (level.getBlockState(nearbyPos).is(state.getBlock())) {
                    double distance = Math.sqrt(pos.distSqr(nearbyPos));
                    int delay = random.nextIntBetweenInclusive((int) (distance * 5.0), (int) (distance * 10.0));
                    level.scheduleTick(nearbyPos, state.getBlock(), delay);
                }
            });
        return true;
    }

    /** 判断当前时间眼眸花应该开放还是闭合：白天（12600-23400 刻之间）开放。 */
    public static boolean shouldBeOpen(Level level) {
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= 12600L && dayTime < 23401L;
    }

    /** 蜜蜂踩上开放眼眸花会获得中毒效果（非和平难度下）。 */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && level.getDifficulty() != Difficulty.PEACEFUL && entity instanceof Bee bee) {
            if (state.is(BlockTags.FLOWERS) && !bee.hasEffect(MobEffects.POISON)) {
                bee.addEffect(new MobEffectInstance(MobEffects.POISON, 25));
            }
        }
    }

    /**
     * 眼眸花的形态枚举。
     * <p>
     * 每种形态绑定花的效果（开放=失明、闭合=反胃）、效果时长、两套切换
     * 音效与粒子颜色。transform() 返回相反的形态。
     */
    public enum Type {
        /** 开放形态：白天，失明效果 11 秒，橙色粒子。 */
        OPEN(true, MobEffects.BLINDNESS, 11.0F, ModSoundEvents.EYEBLOSSOM_OPEN_LONG, ModSoundEvents.EYEBLOSSOM_OPEN, 16545810),
        /** 闭合形态：夜晚，反胃效果 7 秒，灰色粒子。 */
        CLOSED(false, MobEffects.CONFUSION, 7.0F, ModSoundEvents.EYEBLOSSOM_CLOSE_LONG, ModSoundEvents.EYEBLOSSOM_CLOSE, 6250335);

        /** 是否为开放形态。 */
        private final boolean open;
        /** 触碰花朵时施加的效果。 */
        private final Holder<MobEffect> effect;
        /** 效果持续秒数。 */
        private final float effectDuration;
        /** 随机刻触发的长切换音效。 */
        private final Holder<SoundEvent> longSwitchSound;
        /** 计划刻触发的短切换音效。 */
        private final Holder<SoundEvent> shortSwitchSound;
        /** 切换粒子的颜色。 */
        private final int particleColor;

        Type(boolean open, Holder<MobEffect> effect, float effectDuration, Holder<SoundEvent> longSwitchSound, Holder<SoundEvent> shortSwitchSound, int particleColor) {
            this.open = open;
            this.effect = effect;
            this.effectDuration = effectDuration;
            this.longSwitchSound = longSwitchSound;
            this.shortSwitchSound = shortSwitchSound;
            this.particleColor = particleColor;
        }

        /** 返回该形态对应的方块。 */
        public Block block() {
            return this.open ? ModBlocks.OPEN_EYEBLOSSOM.get() : ModBlocks.CLOSED_EYEBLOSSOM.get();
        }

        /** 返回该形态对应的默认方块状态。 */
        public BlockState state() {
            return this.block().defaultBlockState();
        }

        /** 返回相反的形态。 */
        public Type transform() {
            return fromBoolean(!this.open);
        }

        /** 开放形态才会播放空闲环境音。 */
        public boolean emitSounds() {
            return this.open;
        }

        /** 按开放标志取形态。 */
        public static Type fromBoolean(boolean open) {
            return open ? OPEN : CLOSED;
        }

        /** 切换形态时在方块中心生成彩色粒子。 */
        public void spawnTransformParticle(ServerLevel level, BlockPos pos, RandomSource random) {
            Vec3 center = Vec3.atCenterOf(pos);
            ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, this.particleColor);
            level.sendParticles(particle, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        /** 返回长切换音效。 */
        public SoundEvent longSwitchSound() {
            return this.longSwitchSound.value();
        }

        /** 返回短切换音效。 */
        public SoundEvent shortSwitchSound() {
            return this.shortSwitchSound.value();
        }
    }
}
