package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Salmon;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCAgeable;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCEntityData;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCSalmonVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 幼年水生生物与鲑鱼大小变种的核心状态注入。
// 这个 Mixin 把 1.21.2 的年龄状态搬进 1.21.1 的 LivingEntity：
// 给 LivingEntity 加上年龄字段、强制成长字段和对应的计时器，
// 并在实体数据定义、存档读写和每刻更新这些环节里补上对应的逻辑。
// 海豚、鱿鱼和发光鱿鱼通过它获得 AgeableMob 式的幼年状态，
// 鲑鱼通过它获得大小变种的同步与存档，逻辑都对照 1.21.1 原版 AgeableMob 的写法。
@Mixin(LivingEntity.class)
public abstract class FutureMCAgeableMixin implements FutureMCAgeable {

    // 幼年标志的同步数据访问器。
    // 它作为 LivingEntity 类的静态字段合并进去，初始化发生在 LivingEntity 的
    // 类初始化阶段。JVM 保证父类先于子类初始化，所以这个槽位的编号一定排在
    // 原版 LivingEntity 槽位之后、任何子类（包括 Player）之前，不会发生冲突。
    // 不能把它放在 FutureMCEntityData 里：那个类的初始化时机不受类加载顺序保护，
    // 运行时才定义槽位会撞上已被 Player 占用的编号，导致玩家进档崩溃。
    @Unique
    private static final EntityDataAccessor<Boolean> futuremc$DATA_BABY = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    // 当前年龄，负数表示幼年，正数表示成年，语义和原版 AgeableMob 一样。
    @Unique
    private int futuremc$age;

    // 强制成长量，原版用骨粉催熟时会把多出的成长量记在这里，长到成年后再补上。
    @Unique
    private int futuremc$forcedAge;

    // 强制成长的计时器，计到零时才会应用强制成长，和原版行为一致。
    @Unique
    private int futuremc$forcedAgeTimer;

    // 返回当前年龄。客户端上不能直接读年龄字段，改为读同步过来的幼年标志；
    // 服务端直接读年龄字段。这是原版 AgeableMob 的写法。
    @Override
    public int futuremc$getAge() {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide) {
            return self.getEntityData().get(futuremc$DATA_BABY) ? -1 : 1;
        }
        return this.futuremc$age;
    }

    // 设置年龄，并同步幼年标志。只有幼年状态发生翻转（幼年变成年或相反）时
    // 才更新同步数据，和原版 AgeableMob 的行为一致。
    @Override
    public void futuremc$setAge(int age) {
        LivingEntity self = (LivingEntity) (Object) this;
        int i = this.futuremc$getAge();
        this.futuremc$age = age;
        if ((i < 0 && age >= 0) || (i >= 0 && age < 0)) {
            self.getEntityData().set(futuremc$DATA_BABY, age < 0);
        }
    }

    // 判断是否为幼年个体，年龄为负数就是幼年。
    @Override
    public boolean futuremc$isBaby() {
        return this.futuremc$getAge() < 0;
    }

    // 让生物成长指定的天数，一天按二十刻算，成长量可以指定是否计入强制成长。
    // 逻辑照搬 1.21.1 原版 AgeableMob 的 ageUp。当前没有外部调用者，保留以对齐
    // 26.3 原版 AgeableMob 结构；下面几条“恒为 0”的提示是原版写法的固有结果
    // （i - i 恒 0，强制成长分支因此只重置计时器），压制 IDE 提示但不改行为。
    @Override
    @SuppressWarnings("all")
    public void futuremc$ageUp(int amount, boolean forced) {
        int i = this.futuremc$getAge();
        // 把天数换算成刻数并加到年龄上，超过成年（0）就按成年处理。
        i += amount * 20;
        if (i > 0) {
            i = 0;
        }
        // 这是 1.21.1 原版 AgeableMob 的原样写法：j 实际恒为 0，
        // 下面的强制成长分支因此只会重置计时器，不会真的累加差额。
        int j = i - i;
        this.futuremc$setAge(i);
        // 如果是强制成长，把差额记下来并启动计时器。
        if (forced) {
            this.futuremc$forcedAge += j;
            if (this.futuremc$forcedAgeTimer == 0) {
                this.futuremc$forcedAgeTimer = 40;
            }
        }
        // 长到成年后，如果曾经被强制成长过，把之前多出的成长量补回来。
        if (this.futuremc$getAge() == 0) {
            this.futuremc$setAge(this.futuremc$forcedAge);
        }
    }

    // 在实体数据定义流程的末尾补上我们新增的数据槽。
    // 幼年标志定义在 LivingEntity 这个层级上，所有活体实体的数据构建器
    // 都会包含这个槽位，所以必须对每个活体实体都定义，否则构建会抛异常；
    // 非目标实体不会读写它，只是多占一个布尔位。鲑鱼的大小变种只存在于
    // 鲑鱼自己的数据链上，所以只对鲑鱼定义。
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void futuremc$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(futuremc$DATA_BABY, false);
        if ((Object) this instanceof Salmon) {
            builder.define(FutureMCSalmonVariant.DATA_TYPE, FutureMCSalmonVariant.DEFAULT.id());
        }
    }

    // 在存档写入流程的末尾补上我们新增的数据。
    // 三种水生生物写入年龄和强制成长，鲑鱼额外写入大小变种的名字。
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void futuremc$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (FutureMCEntityData.isAgeableWaterCreature(self)) {
            tag.putInt("Age", this.futuremc$getAge());
            tag.putInt("ForcedAge", this.futuremc$forcedAge);
        }
        if (self instanceof Salmon salmon) {
            tag.putString(
                "type",
                FutureMCSalmonVariant.byId(salmon.getEntityData().get(FutureMCSalmonVariant.DATA_TYPE)).getSerializedName()
            );
        }
    }

    // 在存档读取流程的末尾恢复我们新增的数据，键名和写入时保持一致。
    // 旧存档没有这些键时，年龄默认为 0（成年），鲑鱼变种默认为中型。
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void futuremc$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (FutureMCEntityData.isAgeableWaterCreature(self)) {
            this.futuremc$setAge(tag.getInt("Age"));
            this.futuremc$forcedAge = tag.getInt("ForcedAge");
        }
        if (self instanceof Salmon salmon) {
            salmon.getEntityData().set(
                FutureMCSalmonVariant.DATA_TYPE,
                FutureMCSalmonVariant.fromName(tag.getString("type")).id()
            );
        }
    }

    // 在每刻更新流程的末尾推进年龄：幼年个体年龄递增，成年个体年龄递减，
    // 直到归零，逻辑和原版 AgeableMob 的 aiStep 一致，只在服务端执行。
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void futuremc$aiStep(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide && FutureMCEntityData.isAgeableWaterCreature(self) && self.isAlive()) {
            int i = this.futuremc$getAge();
            if (i < 0) {
                this.futuremc$setAge(++i);
            } else if (i > 0) {
                this.futuremc$setAge(--i);
            }
        }
    }

    // 在同步数据更新的末尾监听幼年标志和鲑鱼变种的变化，变了就刷新碰撞箱，
    // 这样幼年个体和不同大小的鲑鱼在客户端上也能正确显示体型。
    @Inject(method = "onSyncedDataUpdated", at = @At("TAIL"))
    private void futuremc$onSyncedDataUpdated(EntityDataAccessor<?> key, CallbackInfo ci) {
        if (futuremc$DATA_BABY.equals(key) || FutureMCSalmonVariant.DATA_TYPE.equals(key)) {
            ((LivingEntity) (Object) this).refreshDimensions();
        }
    }
}
