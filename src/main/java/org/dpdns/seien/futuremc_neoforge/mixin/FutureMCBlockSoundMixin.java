package org.dpdns.seien.futuremc_neoforge.mixin;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.dpdns.seien.futuremc_neoforge.futuremc.ModSoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 刷怪笼独立音效的替换实现。
// 从 1.21.2 开始，刷怪笼不再使用默认的方块音效，而是使用自己单独的
// 破坏、脚步、放置、敲击和摔落五个声音。游戏里所有方块的声音查询，
// 包括 NeoForge 提供的更精细的重载，最终都会走到 BlockBehaviour 的
// getSoundType(BlockState) 这个方法，所以在这里统一把刷怪笼的
// 声音类型替换成我们注册的专用声音类型即可。
@Mixin(BlockBehaviour.class)
public abstract class FutureMCBlockSoundMixin {

    // 在声音类型查询的开头拦截，识别出刷怪笼就返回专用声音类型。
    @Inject(
        method = "getSoundType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/SoundType;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void futuremc$getSoundType(BlockState state, CallbackInfoReturnable<SoundType> cir) {
        // 只有刷怪笼会被替换，其他方块保持原版声音类型。
        if (state.getBlock() instanceof SpawnerBlock) {
            cir.setReturnValue(ModSoundEvents.spawnerSoundType());
        }
    }
}
