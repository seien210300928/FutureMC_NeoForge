package org.dpdns.seien.futuremc_neoforge.mixin.v21_4;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 注册表构建错误过滤 mixin。
 * <p>
 * 群系注入 mixin（FutureMCPaleGardenBiomeMixin）在
 * {@code MultiNoiseBiomeSourceParameterList.Preset} 静态初始化后才替换
 * 群系表，此时主世界群系注册表构建阶段（RegistrySetBuilder 的 BuildState）
 * 会先校验 futuremc:pale_garden 未被任何地物/标签引用而报
 * "unreferenced" 错误，导致启动失败。这里在 throwOnError 前过滤掉
 * 包含 futuremc:pale_garden 的这类错误，让注入在后续阶段正常生效。
 */
@Mixin(targets = "net.minecraft.core.RegistrySetBuilder$BuildState")
public abstract class FutureMCBootstrapRegistryMixin {
    /** 影子方法：获取构建阶段积累的错误列表。 */
    @Shadow
    public abstract List<RuntimeException> errors();

    /** 在抛出注册表错误前，移除苍白花园未被引用的错误。 */
    @Inject(method = "throwOnError", at = @At("HEAD"))
    private void futuremc$removePaleGardenUnreferencedError(CallbackInfo ci) {
        this.errors().removeIf(error -> error.getMessage() != null && error.getMessage().contains("futuremc:pale_garden"));
    }
}
