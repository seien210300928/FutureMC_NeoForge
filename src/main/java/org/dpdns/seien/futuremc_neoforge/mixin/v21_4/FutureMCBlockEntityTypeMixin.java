package org.dpdns.seien.futuremc_neoforge.mixin.v21_4;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 方块实体类型校验 mixin。
 * <p>
 * 1.21.1 原版的告示牌方块实体类型（BlockEntityType.SIGN / HANGING_SIGN）
 * 校验方块时会走方块到方块实体的映射表，我们移植的苍白橡木告示牌
 * 不在原版映射里，导致放置后不生成方块实体、无法编辑文字。这里在
 * {@code BlockEntityType.isValid} 头部直接放行苍白橡木的两种告示牌，
 * 让它们复用原版告示牌方块实体（含文字渲染逻辑）。
 */
@Mixin(BlockEntityType.class)
public abstract class FutureMCBlockEntityTypeMixin {

    /** 校验方块归属时，接受苍白橡木的告示牌与悬挂告示牌。 */
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void futuremc$acceptPaleSigns(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // 用 Object 比较绕开 mixin 类的泛型实例类型与静态字段类型的差异。
        if ((Object) this == BlockEntityType.SIGN) {
            if (state.is(ModBlocks.PALE_OAK_SIGN.get()) || state.is(ModBlocks.PALE_OAK_WALL_SIGN.get())) {
                cir.setReturnValue(true);
            }
        } else if ((Object) this == BlockEntityType.HANGING_SIGN
                && (state.is(ModBlocks.PALE_OAK_HANGING_SIGN.get()) || state.is(ModBlocks.PALE_OAK_WALL_HANGING_SIGN.get()))) {
            cir.setReturnValue(true);
        }
    }
}
