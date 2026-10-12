package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModSoundEvents;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.CreakingHeartBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.entity.CreakingHeartBlockEntity;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.state.properties.CreakingHeartState;

/**
 * 嘎吱实体。
 * <p>
 * 苍白花园的守卫生物：只在夜晚由嘎吱之心召唤，平时无法移动（冻结），
 * 玩家直视它时会激活并开始追击攻击；玩家移开视线或它离开嘎吱之心
 * 一定距离后回归冻结状态。所有行为用 1.21.1 的目标系统（Goal）实现，
 * 语义对照 26.3 原版（原版在 1.21.4+ 用大脑系统，这里翻译为 Goal 等价物）。
 * <p>
 * 嘎吱只有 1 点生命，免疫火焰，被嘎吱之心绑定时无法通过传送门，
 * 嘎吱之心被破坏后随之解体。攻击、受击、死亡均有对应动画状态。
 */
public class Creaking extends Monster {
    /** 数据同步：当前能否移动（玩家未直视时冻结）。 */
    private static final EntityDataAccessor<Boolean> CAN_MOVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    /** 数据同步：当前是否处于激活（攻击）状态。 */
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    /** 数据同步：是否正在解体（死亡动画）。 */
    private static final EntityDataAccessor<Boolean> IS_TEARING_DOWN = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    /** 数据同步：绑定的嘎吱之心位置（回家点）。 */
    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    /** 攻击动画持续时间（15 刻）。 */
    private static final int ATTACK_ANIMATION_DURATION = 15;
    /** 嘎吱的最大生命值（1 点）。 */
    private static final int MAX_HEALTH = 1;
    /** 攻击伤害（3 点）。 */
    private static final float ATTACK_DAMAGE = 3.0F;
    /** 追随范围（32 格）。 */
    private static final float FOLLOW_RANGE = 32.0F;
    /** 激活嘎吱所需的玩家距离平方（12 格内）。 */
    private static final float ACTIVATION_RANGE_SQ = 144.0F;
    /** 攻击间隔（40 刻）。 */
    public static final int ATTACK_INTERVAL = 40;
    /** 战斗时的移动速度。 */
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.4F;
    /** 空闲时的速度倍率。 */
    public static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.3F;
    /** 嘎吱身上的橙色（心疼粒子朝向嘎吱的颜色）。 */
    public static final int CREAKING_ORANGE = 16545810;
    /** 嘎吱身上的灰色（心疼粒子朝向心的颜色）。 */
    public static final int CREAKING_GRAY = 0x5F5F5F;
    /** 无敌动画持续时间（8 刻）。 */
    public static final int INVULNERABILITY_ANIMATION_DURATION = 8;
    /** 解体动画总时长（45 刻）。 */
    public static final int TWITCH_DEATH_DURATION = 45;
    /** 玩家卡在嘎吱体内的最大计数（超过 4 次判定解散）。 */
    private static final int MAX_PLAYER_STUCK_COUNTER = 4;

    /** 攻击动画剩余刻数。 */
    private int attackAnimationRemainingTicks;
    /** 攻击动画状态。 */
    public final AnimationState attackAnimationState = new AnimationState();
    /** 受击无敌动画状态。 */
    public final AnimationState invulnerabilityAnimationState = new AnimationState();
    /** 死亡解体动画状态。 */
    public final AnimationState deathAnimationState = new AnimationState();
    /** 无敌动画剩余刻数。 */
    private int invulnerabilityAnimationRemainingTicks;
    /** 眼睛是否发光（解体时闪烁）。 */
    private boolean eyesGlowing;
    /** 下一次眼睛闪烁的刻数。 */
    private int nextFlickerTime;
    /** 玩家卡在体内的计数。 */
    private int playerStuckCounter;

    public Creaking(EntityType<? extends Creaking> type, Level level) {
        super(type, level);
        // 嘎吱的移动/跳跃/视线控制全部套上 canMove 门控，冻结时无法行动。
        this.lookControl = new CreakingLookControl(this);
        this.moveControl = new CreakingMoveControl(this);
        this.jumpControl = new CreakingJumpControl(this);
        ((GroundPathNavigation) this.getNavigation()).setCanFloat(true);
        // 嘎吱不掉经验。
        this.xpReward = 0;
    }

    /** 设置嘎吱的临时（心脏绑定）状态：绑定回家点并调整路径判定。 */
    public void setTransient(BlockPos pos) {
        this.setHomePos(pos);
        this.setPathfindingMalus(PathType.DANGER_OTHER, 8.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0F);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    /** 嘎吱是否被嘎吱之心绑定（有回家点）。 */
    public boolean isHeartBound() {
        return this.getHomePos() != null;
    }

    /** 身体旋转控制：冻结时不旋转身体。 */
    @Override
    protected BodyRotationControl createBodyControl() {
        return new CreakingBodyRotationControl(this);
    }

    /** 注册嘎吱的目标（Goal）集合。 */
    @Override
    protected void registerGoals() {
        // 0 优先级：游泳浮水（冻结时不生效）。
        this.goalSelector.addGoal(0, new CreakingSwimGoal(this));
        // 1 优先级：空闲时看向附近玩家。
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        // 2 优先级：空闲漫游。
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.12F));
        // 3 优先级：近战攻击（战斗速度 0.4）。
        // followingTargetEvenIfNotSeen=true：锁定玩家后即使短暂失去视线
        // （玩家背对跑、绕墙）也继续追击，配合 mustSee=false 的目标保持，
        // 完整对齐 26.3 嘎吱"玩家背对时持续追击"的背刺行为。
        this.goalSelector.addGoal(3, new CreakingMeleeAttackGoal(this, MOVEMENT_SPEED_WHEN_FIGHTING, true));
        // 4 优先级：目标不再可攻击时停止攻击。
        this.goalSelector.addGoal(4, new CreakingStopAttackingGoal(this));
        // 目标选择：激活状态下选择最近的玩家。
        // mustSee=false：初始锁定仍要求能看见（Monster.canAttack 含视线），
        // 但锁定后失去视线（玩家跑进树林/躲墙后）也会保持目标继续追击，
        // 对齐 26.3 原版"背刺"语义——玩家不看嘎吱时它反而持续追击，
        // 距离上限由 TargetGoal.canContinueToUse（FOLLOW_RANGE²）与
        // HomeNodeEvaluator（离家 32 格）双重兜底。
        this.targetSelector.addGoal(0, new CreakingAttackableTargetGoal(this, Player.class, false));
    }

    /** 定义数据同步字段。 */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAN_MOVE, true);
        builder.define(IS_ACTIVE, false);
        builder.define(IS_TEARING_DOWN, false);
        builder.define(HOME_POS, Optional.empty());
    }

    /** 嘎吱的属性：1 点生命、0.4 移速、3 点攻击、32 格追随、高步幅。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 1.0)
            .add(Attributes.MOVEMENT_SPEED, 0.4F)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.STEP_HEIGHT, 1.0625);
    }

    /** 嘎吱当前能否移动。 */
    public boolean canMove() {
        return this.entityData.get(CAN_MOVE);
    }

    /** 攻击目标：播放攻击动画并广播事件。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        this.attackAnimationRemainingTicks = ATTACK_ANIMATION_DURATION;
        this.level().broadcastEntityEvent(this, (byte) 4);
        return super.doHurtTarget(target);
    }

    /**
     * 受击处理：被嘎吱之心绑定且伤害不是无视无敌时，不扣血而是触发
     * 无敌动画，并让嘎吱之心反馈心疼效果（玩家造成的伤害才反馈）。
     */
    @Override
    public boolean hurt(DamageSource source, float damage) {
        BlockPos homePos = this.getHomePos();
        if (homePos == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, damage);
        }
        if (!this.isInvulnerableTo(source) && this.invulnerabilityAnimationRemainingTicks <= 0 && !this.isDeadOrDying()) {
            Player responsiblePlayer = this.blameSourceForDamage(source);
            Entity directEntity = source.getDirectEntity();
            // 只有生物、弹射物或玩家造成的伤害才有效（环境伤害不生效）。
            if (!(directEntity instanceof LivingEntity) && !(directEntity instanceof Projectile) && responsiblePlayer == null) {
                return false;
            }
            this.invulnerabilityAnimationRemainingTicks = INVULNERABILITY_ANIMATION_DURATION;
            this.level().broadcastEntityEvent(this, (byte) 66);
            this.gameEvent(GameEvent.ENTITY_ACTION);
            BlockEntity blockEntity = this.level().getBlockEntity(homePos);
            if (blockEntity instanceof CreakingHeartBlockEntity creakingHeartBlockEntity && creakingHeartBlockEntity.isProtector(this)) {
                if (responsiblePlayer != null) {
                    creakingHeartBlockEntity.creakingHurt();
                }
                this.playHurtSound(source);
            }
            return true;
        }
        return false;
    }

    /** 追溯伤害来源：玩家本人或玩家发射的弹射物。 */
    public Player blameSourceForDamage(DamageSource source) {
        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof Player player) {
            return player;
        }
        Entity causingEntity = source.getEntity();
        if (causingEntity instanceof Player player) {
            return player;
        }
        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof Player player) {
            return player;
        }
        return null;
    }

    /** 冻结时不可被推动。 */
    @Override
    public boolean isPushable() {
        return super.isPushable() && this.canMove();
    }

    /** 冻结时忽略外力推动。 */
    @Override
    public void push(double xa, double ya, double za) {
        if (this.canMove()) {
            super.push(xa, ya, za);
        }
    }

    @Override
    protected void customServerAiStep() {
    }

    /** 每刻 AI：维护动画倒计时并评估冻结/激活状态。 */
    @Override
    public void aiStep() {
        if (this.invulnerabilityAnimationRemainingTicks > 0) {
            this.invulnerabilityAnimationRemainingTicks--;
        }
        if (this.attackAnimationRemainingTicks > 0) {
            this.attackAnimationRemainingTicks--;
        }
        if (!this.level().isClientSide()) {
            boolean canMove = this.entityData.get(CAN_MOVE);
            boolean nowCanMove = this.checkCanMove();
            if (nowCanMove != canMove) {
                this.gameEvent(GameEvent.ENTITY_ACTION);
                if (nowCanMove) {
                    this.makeSound(ModSoundEvents.CREAKING_UNFREEZE.get());
                } else {
                    this.stopInPlace();
                    this.makeSound(ModSoundEvents.CREAKING_FREEZE.get());
                }
            }
            this.entityData.set(CAN_MOVE, nowCanMove);
        }
        super.aiStep();
    }

    /** 每刻更新：心跳绑定失效时嘎吱直接死亡；客户端维护动画与眼睛闪烁。 */
    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            BlockPos homePos = this.getHomePos();
            if (homePos != null) {
                boolean hasProtectionFromCreakingHeart = this.level().getBlockEntity(homePos) instanceof CreakingHeartBlockEntity creakingHeartBlockEntity
                    && creakingHeartBlockEntity.isProtector(this);
                if (!hasProtectionFromCreakingHeart) {
                    this.setHealth(0.0F);
                }
            }
        }
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
            this.checkEyeBlink();
        }
    }

    /** 死亡 tick：绑定心脏且正在解体时播放 45 刻解体动画再移除。 */
    @Override
    protected void tickDeath() {
        if (this.isHeartBound() && this.isTearingDown()) {
            this.deathTime++;
            if (!this.level().isClientSide() && this.deathTime > TWITCH_DEATH_DURATION && !this.isRemoved()) {
                this.tearDown();
            }
        } else {
            super.tickDeath();
        }
    }

    /** 行走动画：按距离缩放摆动速度。 */
    @Override
    protected void updateWalkAnimation(float distance) {
        float targetSpeed = Math.min(distance * 25.0F, 3.0F);
        this.walkAnimation.update(targetSpeed, 1.0F);
    }

    /** 客户端动画状态：攻击/无敌/解体三个动画各自启停。 */
    private void setupAnimationStates() {
        this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
        this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
        this.deathAnimationState.animateWhen(this.isTearingDown(), this.tickCount);
    }

    /** 解体：喷出苍白橡木与嘎吱之心的方块粒子后直接移除实体。 */
    public void tearDown() {
        if (this.level() instanceof ServerLevel serverLevel) {
            AABB box = this.getBoundingBox();
            Vec3 center = box.getCenter();
            double xSpread = box.getXsize() * 0.3;
            double ySpread = box.getYsize() * 0.3;
            double zSpread = box.getZsize() * 0.3;
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.PALE_OAK_WOOD.get().defaultBlockState()),
                center.x, center.y, center.z, 100, xSpread, ySpread, zSpread, 0.0);
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,
                    ModBlocks.CREAKING_HEART.get().defaultBlockState().setValue(CreakingHeartBlock.STATE, CreakingHeartState.AWAKE)),
                center.x, center.y, center.z, 10, xSpread, ySpread, zSpread, 0.0);
        }
        this.makeSound(this.getDeathSound());
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    /** 嘎吱的死亡效果：追溯伤害源、触发死亡并播放抽搐音效。 */
    public void creakingDeathEffects(DamageSource source) {
        this.blameSourceForDamage(source);
        this.die(source);
        this.makeSound(ModSoundEvents.CREAKING_TWITCH.get());
    }

    /** 实体事件：66=受击无敌动画，4=攻击动画。 */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 66) {
            this.invulnerabilityAnimationRemainingTicks = INVULNERABILITY_ANIMATION_DURATION;
            this.playHurtSound(this.damageSources().generic());
        } else if (id == 4) {
            this.attackAnimationRemainingTicks = ATTACK_ANIMATION_DURATION;
            this.playAttackSound();
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** 被心脏绑定的嘎吱免疫火焰。 */
    @Override
    public boolean fireImmune() {
        return this.isHeartBound() || super.fireImmune();
    }

    /** 被心脏绑定的嘎吱不能使用传送门。 */
    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return !this.isHeartBound() && super.canUsePortal(ignorePassenger);
    }

    /** 导航：使用嘎吱专用导航（冻结时停摆 + 回家点限制）。 */
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new CreakingPathNavigation(this, level);
    }

    /**
     * 玩家是否卡在嘎吱体内：嘎吱的碰撞箱包含玩家眼睛位置时开始计数，
     * 连续超过 4 次判定为卡住（嘎吱之心会据此解散嘎吱）。
     */
    public boolean playerIsStuckInYou() {
        List<Player> players = this.nearbyPlayers();
        if (players.isEmpty()) {
            this.playerStuckCounter = 0;
            return false;
        }
        AABB ownBox = this.getBoundingBox();
        for (Player player : players) {
            if (ownBox.contains(player.getEyePosition())) {
                this.playerStuckCounter++;
                return this.playerStuckCounter > MAX_PLAYER_STUCK_COUNTER;
            }
        }
        this.playerStuckCounter = 0;
        return false;
    }

    /** 读取存档：恢复回家点。 */
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("home_pos")) {
            this.setTransient(BlockPos.of(tag.getLong("home_pos")));
        }
    }

    /** 写入存档：保存回家点。 */
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        BlockPos homePos = this.getHomePos();
        if (homePos != null) {
            tag.putLong("home_pos", homePos.asLong());
        }
    }

    /** 设置回家点（嘎吱之心的位置）。 */
    public void setHomePos(BlockPos pos) {
        this.entityData.set(HOME_POS, Optional.of(pos));
    }

    /** 获取回家点。 */
    @Nullable
    public BlockPos getHomePos() {
        return this.entityData.get(HOME_POS).orElse(null);
    }

    /** 标记嘎吱开始解体。 */
    public void setTearingDown() {
        this.entityData.set(IS_TEARING_DOWN, true);
    }

    /** 嘎吱是否正在解体。 */
    public boolean isTearingDown() {
        return this.entityData.get(IS_TEARING_DOWN);
    }

    /** 眼睛是否发光。 */
    public boolean hasGlowingEyes() {
        return this.eyesGlowing;
    }

    /** 客户端眼睛闪烁：按死亡进度随机切换眼睛发光状态。 */
    public void checkEyeBlink() {
        if (this.deathTime > this.nextFlickerTime) {
            this.nextFlickerTime = this.deathTime + this.getRandom().nextIntBetweenInclusive(
                this.eyesGlowing ? 2 : this.deathTime / 4, this.eyesGlowing ? 8 : this.deathTime / 2);
            this.eyesGlowing = !this.eyesGlowing;
        }
    }

    /** 播放攻击音效。 */
    public void playAttackSound() {
        this.makeSound(ModSoundEvents.CREAKING_ATTACK.get());
    }

    /** 空闲音效：激活状态下不播放环境音。 */
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isActive() ? null : ModSoundEvents.CREAKING_AMBIENT.get();
    }

    /** 受击音效：被心脏绑定时播放摇晃音。 */
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isHeartBound() ? ModSoundEvents.CREAKING_SWAY.get() : super.getHurtSound(source);
    }

    /** 死亡音效。 */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.CREAKING_DEATH.get();
    }

    /** 脚步声：播放嘎吱脚步音（音量较小）。 */
    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(ModSoundEvents.CREAKING_STEP.get(), 0.15F, 1.0F);
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return super.getTarget();
    }

    /** 击退：冻结时不受击退。 */
    @Override
    public void knockback(double power, double xd, double zd) {
        if (this.canMove()) {
            super.knockback(power, xd, zd);
        }
    }

    /**
     * 评估嘎吱能否移动：附近有可攻击的玩家且玩家直视嘎吱时激活并冻结，
     * 玩家离开或移开视线后解冻。这是"盯住它就不动"的核心逻辑。
     */
    public boolean checkCanMove() {
        List<Player> players = this.nearbyPlayers();
        boolean active = this.isActive();
        if (players.isEmpty()) {
            if (active) {
                this.deactivate();
            }
            return true;
        }
        boolean hasPotentialTarget = false;
        for (Player player : players) {
            if (!this.canAttack(player) || this.isAlliedTo(player)) {
                continue;
            }
            hasPotentialTarget = true;
            if (!this.isLookingAtMe(player)) {
                continue;
            }
            if (active) {
                return false;
            }
            if (player.distanceToSqr(this) < ACTIVATION_RANGE_SQ) {
                this.activate(player);
                return false;
            }
        }
        if (!hasPotentialTarget && active) {
            this.deactivate();
        }
        return true;
    }

    /** 激活嘎吱：锁定目标、播放激活音效。 */
    public void activate(Player player) {
        this.setTarget(player);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(ModSoundEvents.CREAKING_ACTIVATE.get());
        this.setIsActive(true);
    }

    /** 解除激活：清空目标、播放解除音效。 */
    public void deactivate() {
        this.setTarget(null);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(ModSoundEvents.CREAKING_DEACTIVATE.get());
        this.setIsActive(false);
    }

    /** 设置激活状态（同步到客户端）。 */
    public void setIsActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
    }

    /** 嘎吱是否处于激活状态。 */
    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }

    /** 路径价值：嘎吱不偏好任何地形。 */
    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    /** 收集 32 格（平方 1024）内的所有玩家（对齐嘎吱的追随范围）。 */
    private List<Player> nearbyPlayers() {
        List<Player> players = new ArrayList<>();
        for (Player player : this.level().players()) {
            if (player.distanceToSqr(this) <= 1024.0) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * 玩家是否直视嘎吱：取玩家视线方向，与嘎吱三个高度（眼睛、
     * 半高、平均）的连线做点积，角度足够小且玩家能看到嘎吱才算。
     */
    private boolean isLookingAtMe(Player player) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        double[] gazeHeights = new double[]{this.getEyeY(), this.getY() + 0.5, (this.getEyeY() + this.getY()) / 2.0};
        for (double gazeHeight : gazeHeights) {
            Vec3 dir = new Vec3(this.getX() - player.getX(), gazeHeight - player.getEyeY(), this.getZ() - player.getZ());
            double dist = dir.length();
            double dot = look.dot(dir.normalize());
            if (dot > 1.0 - 0.5 / dist && player.hasLineOfSight(this)) {
                return true;
            }
        }
        return false;
    }

    // ============ 内部类：嘎吱专用的控制与目标 ============

    /** 身体旋转控制：冻结时不旋转身体朝向。 */
    private class CreakingBodyRotationControl extends BodyRotationControl {
        public CreakingBodyRotationControl(Creaking creaking) {
            super(creaking);
        }

        @Override
        public void clientTick() {
            if (Creaking.this.canMove()) {
                super.clientTick();
            }
        }
    }

    /** 跳跃控制：冻结时禁止跳跃。 */
    private class CreakingJumpControl extends JumpControl {
        public CreakingJumpControl(Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            } else {
                Creaking.this.setJumping(false);
            }
        }
    }

    /** 视线控制：冻结时无法转头。 */
    private class CreakingLookControl extends LookControl {
        public CreakingLookControl(Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }
    }

    /** 移动控制：冻结时无法移动。 */
    private class CreakingMoveControl extends MoveControl {
        public CreakingMoveControl(Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }
    }

    /**
     * 嘎吱专用地面导航：冻结时导航不推进；寻路时使用回家点限制的
     * 节点评估器，让嘎吱不会离嘎吱之心太远。
     */
    private class CreakingPathNavigation extends GroundPathNavigation {
        public CreakingPathNavigation(Creaking mob, Level level) {
            super(mob, level);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }

        /** 用回家点限制的节点评估器替换默认的行走评估器。 */
        @Override
        protected PathFinder createPathFinder(int maxVisitedNodes) {
            this.nodeEvaluator = Creaking.this.new HomeNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
        }
    }

    /**
     * 回家点限制的节点评估器：距离嘎吱之心超过 32 格（平方 1024）的
     * 路径节点标记为不可通行，确保嘎吱只在心周围游荡。
     */
    private class HomeNodeEvaluator extends WalkNodeEvaluator {
        private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

        private HomeNodeEvaluator() {
            super();
        }

        @Override
        public PathType getPathType(PathfindingContext context, int x, int y, int z) {
            BlockPos homePos = Creaking.this.getHomePos();
            if (homePos == null) {
                return super.getPathType(context, x, y, z);
            }
            double homeDistance = homePos.distSqr(new Vec3i(x, y, z));
            // 超过最大距离且比嘎吱当前位置更远离心时禁止通行（允许原路返回）。
            return homeDistance > MAX_DISTANCE_TO_HOME_SQ && homeDistance >= homePos.distSqr(context.mobPosition())
                ? PathType.BLOCKED
                : super.getPathType(context, x, y, z);
        }
    }

    /** 游泳目标：冻结时不游泳，其余行为与浮水目标一致。 */
    private class CreakingSwimGoal extends Goal {
        private final Creaking creaking;

        public CreakingSwimGoal(Creaking creaking) {
            this.creaking = creaking;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.JUMP));
            creaking.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return creaking.canMove() && (creaking.isInWater() && creaking.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > creaking.getFluidJumpThreshold()
                || creaking.isInLava() || creaking.isInFluidType((fluidType, height) -> creaking.canSwimInFluidType(fluidType) && height > creaking.getFluidJumpThreshold()));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (creaking.getRandom().nextFloat() < 0.8F) {
                creaking.getJumpControl().jump();
            }
        }
    }

    /** 近战攻击目标：冻结时无法发起攻击，攻击间隔 40 刻。
     *  followingTargetEvenIfNotSeen=true：追击中短暂看不见目标也继续追
     *  （背刺追击语义），距离由目标保持 Goal 与 HomeNodeEvaluator 兜底。 */
    private class CreakingMeleeAttackGoal extends MeleeAttackGoal {
        private final Creaking creaking;

        public CreakingMeleeAttackGoal(Creaking creaking, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(creaking, speedModifier, followingTargetEvenIfNotSeen);
            this.creaking = creaking;
        }

        @Override
        public boolean canUse() {
            return creaking.canMove() && super.canUse();
        }

        @Override
        protected int getAttackInterval() {
            return this.adjustedTickDelay(ATTACK_INTERVAL);
        }
    }

    /**
     * 停止攻击目标：当攻击目标死亡、不再可攻击或离开嘎吱的追随范围时
     * 停止追击并解除激活。注意：玩家移开视线不算"目标不可达"——
     * 按 26.3 原版语义，玩家不看嘎吱时它反而会继续追击（背刺机制），
     * 只有玩家彻底离开或死亡才放弃目标。
     */
    private class CreakingStopAttackingGoal extends Goal {
        private final Creaking creaking;

        public CreakingStopAttackingGoal(Creaking creaking) {
            this.creaking = creaking;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = creaking.getTarget();
            if (target == null) {
                return false;
            }
            return !isTargetStillReachable(target);
        }

        /** 目标是否仍然可达：玩家存活、仍可被攻击且未离开追随范围（32 格）。 */
        private boolean isTargetStillReachable(LivingEntity target) {
            if (!(target instanceof Player player)) {
                return false;
            }
            return player.isAlive() && Creaking.this.canAttack(player)
                && player.distanceToSqr(Creaking.this) <= 1024.0;
        }

        @Override
        public void start() {
            creaking.deactivate();
        }
    }

    /**
     * 目标选择：嘎吱可以锁定最近的玩家。26.3 原版里嘎吱会主动追击
     * 附近的玩家（玩家不看它时接近攻击），因此这里不要求已激活；
     * 玩家直视时的"冻结"由 checkCanMove 在移动层处理。
     */
    private class CreakingAttackableTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final Creaking creaking;

        public CreakingAttackableTargetGoal(Creaking creaking, Class<Player> targetType, boolean mustSee) {
            super(creaking, targetType, mustSee);
            this.creaking = creaking;
        }

        @Override
        public boolean canUse() {
            return creaking.canMove() && super.canUse();
        }
    }
}
