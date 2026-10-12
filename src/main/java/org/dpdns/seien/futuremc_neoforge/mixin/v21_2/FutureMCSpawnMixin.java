package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import javax.annotation.Nullable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.level.ServerLevelAccessor;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCAgeable;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCEntityData;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCSalmonVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 自然生成阶段的 1.21.2 内容应用。
// 这个 Mixin 拦在每只生物生成流程的开头，补上 1.21.2 的两项行为：
// 海豚、鱿鱼和发光鱿鱼会按概率生成幼年个体，鲑鱼会随机获得大小变种。
// 生成概率和大小权重都对照 1.21.2 原版的数据：海豚幼年概率 10%，
// 鱿鱼和发光鱿鱼 5%，鲑鱼大小权重为小型 30、中型 50、大型 15。
@Mixin(Mob.class)
public abstract class FutureMCSpawnMixin {

    // 在生成流程开头处理幼年判定和鲑鱼大小。
    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void futuremc$finalizeSpawn(
        ServerLevelAccessor level,
        DifficultyInstance difficulty,
        MobSpawnType spawnType,
        @Nullable SpawnGroupData groupData,
        CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        Mob self = (Mob) (Object) this;
        // 三种水生生物按 1.21.2 的方式参与幼年判定。
        // 沿用原版 AgeableMob 的组数据机制：这一批生成中第一只必定成年，
        // 从第二只开始按概率出现幼年，逻辑和原版的一致。
        if (FutureMCEntityData.isAgeableWaterCreature(self)) {
            // 海豚的幼年生成概率是 10%，鱿鱼和发光鱿鱼是 5%。
            float chance = self instanceof Dolphin ? 0.1F : 0.05F;
            // 取出已有的生成组数据，没有就按我们的概率新建一份。
            AgeableMob.AgeableMobGroupData ageableData = groupData instanceof AgeableMob.AgeableMobGroupData group
                ? group
                : new AgeableMob.AgeableMobGroupData(chance);
            // 满足原版幼年判定条件时，把这只生物设置成幼年。
            if (ageableData.isShouldSpawnBaby()
                && ageableData.getGroupSize() > 0
                && level.getRandom().nextFloat() <= ageableData.getBabySpawnChance()) {
                ((FutureMCAgeable) self).futuremc$setAge(AgeableMob.BABY_START_AGE);
            }
            // 记录这一批里已经生成的个体数。
            ageableData.increaseGroupSizeByOne();
        }
        // 鲑鱼按大小权重随机选择变种。
        if (self instanceof Salmon salmon) {
            // 1.21.1 里没有 WeightedList 工具类，所以按 1.21.2 的权重手写随机：
            // 取 0 到 94 的随机数，落在 0 到 29 是小型、30 到 79 是中型、80 到 94 是大型。
            int roll = level.getRandom().nextInt(95);
            FutureMCSalmonVariant variant = roll < 30
                ? FutureMCSalmonVariant.SMALL
                : (roll < 80 ? FutureMCSalmonVariant.MEDIUM : FutureMCSalmonVariant.LARGE);
            // 把选中的变种写进同步数据。
            salmon.getEntityData().set(FutureMCSalmonVariant.DATA_TYPE, variant.id());
        }
    }
}
