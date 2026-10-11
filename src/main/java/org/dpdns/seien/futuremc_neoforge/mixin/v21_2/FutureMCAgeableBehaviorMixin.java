package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Salmon;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity.FutureMCAgeable;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity.FutureMCEntityData;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity.FutureMCSalmonVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 幼年水生生物与鲑鱼大小变种的体型相关方法覆盖。
// 原版的 LivingEntity 用 isBaby 判断幼年、用 getAgeScale 得到幼年缩放，
// 用 getDefaultDimensions 计算碰撞箱尺寸。1.21.1 里这三种水生生物不是
// AgeableMob，原版方法返回的都是成年值，所以在这里按目标实体覆盖它们：
// 海豚幼年缩放 0.65，鱿鱼和发光鱿鱼幼年缩放 0.5，
// 鲑鱼则按大小变种缩放（0.5、1.0、1.5）。
@Mixin(LivingEntity.class)
public abstract class FutureMCAgeableBehaviorMixin {

    // 覆盖幼年判断：三种水生生物按我们注入的年龄状态回答，
    // 其余生物保持原版行为。
    @Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
    private void futuremc$isBaby(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (FutureMCEntityData.isAgeableWaterCreature(self)) {
            cir.setReturnValue(((FutureMCAgeable) self).futuremc$isBaby());
        }
    }

    // 覆盖幼年缩放：海豚幼年是成年体型的 0.65 倍，
    // 鱿鱼和发光鱿鱼是 0.5 倍；非幼年或非目标生物保持原版数值。
    @Inject(method = "getAgeScale", at = @At("HEAD"), cancellable = true)
    private void futuremc$getAgeScale(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (FutureMCEntityData.isAgeableWaterCreature(self) && ((FutureMCAgeable) self).futuremc$isBaby()) {
            cir.setReturnValue(self instanceof Dolphin ? FutureMCEntityData.DOLPHIN_BABY_SCALE : FutureMCEntityData.SQUID_BABY_SCALE);
        }
    }

    // 覆盖碰撞箱尺寸：鲑鱼按当前大小变种缩放体型。
    // 原版的 LivingEntity 计算尺寸时本来就调用 getAgeScale，
    // 但鲑鱼的大小和幼年无关，所以这里直接按变种缩放，其他生物不受影响。
    @Inject(method = "getDefaultDimensions", at = @At("HEAD"), cancellable = true)
    private void futuremc$getDefaultDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object) this instanceof Salmon salmon) {
            cir.setReturnValue(salmon.getType().getDimensions().scale(FutureMCSalmonVariant.scaleOf(salmon)));
        }
    }
}
