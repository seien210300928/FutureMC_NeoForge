package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.2 起潜行是阻止黏液块弹跳的预期方式：潜行摔落在黏液块上不再受伤。
// 1.21.1 的 fallOn 在潜行时转调父类结算普通摔落伤害（对应 MC-54532），
// 1.21.2 改为完全跳过伤害结算（对应 MC-275834 的修复方案）。
// 这里在方法开头拦截潜行情况并取消原逻辑，其余行为与 1.21.2 原版一致。
@Mixin(SlimeBlock.class)
public abstract class FutureMCSlimeBlockMixin {

    // 潜行落地时不结算任何摔落伤害，照搬 1.21.2 原版 fallOn 的语义。
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void futuremc$skipFallDamageWhenSneaking(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (entity.isSuppressingBounce()) {
            ci.cancel();
        }
    }
}
