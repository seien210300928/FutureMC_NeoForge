package org.dpdns.seien.futuremc_neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Salmon;
import org.dpdns.seien.futuremc_neoforge.futuremc.entity.FutureMCEntityData;
import org.dpdns.seien.futuremc_neoforge.futuremc.entity.FutureMCSalmonVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 幼年水生生物与鲑鱼大小变种的渲染缩放。
// 1.21.1 的实体渲染器不会读取 getAgeScale，只有原版的 AgeableMob 系列
// 才在各自渲染器里手动缩放，所以这里在通用的实体渲染缩放流程开头补上：
// 幼年的海豚、鱿鱼和发光鱿鱼按年龄缩放系数缩小，鲑鱼按大小变种缩放。
// 这个 Mixin 只对客户端生效，只影响目标生物，其他实体保持原样。
@Mixin(LivingEntityRenderer.class)
public abstract class FutureMCBabyRendererMixin {

    // 在渲染器的缩放流程开头应用我们新增的缩放。
    @Inject(method = "scale", at = @At("HEAD"))
    private void futuremc$scale(LivingEntity entity, PoseStack poseStack, float partialTick, CallbackInfo ci) {
        // 幼年的三种水生生物按年龄缩放系数缩小，海豚是 0.65，鱿鱼类是 0.5。
        if (FutureMCEntityData.isAgeableWaterCreature(entity)) {
            float scale = entity.getAgeScale();
            poseStack.scale(scale, scale, scale);
        } else if (entity instanceof Salmon salmon) {
            // 鲑鱼按大小变种缩放，小型 0.5 倍，大型 1.5 倍。
            float scale = FutureMCSalmonVariant.scaleOf(salmon);
            poseStack.scale(scale, scale, scale);
        }
    }
}
