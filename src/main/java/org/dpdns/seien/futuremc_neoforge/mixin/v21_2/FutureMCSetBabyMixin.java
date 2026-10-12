package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCAgeable;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.aquatic.FutureMCEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 幼年水生生物的幼年状态设置入口。
// 1.21.1 里 Mob.setBaby 是一个空方法，只有 AgeableMob 的子类才有真正的幼年逻辑，
// 所以刷怪蛋右击海豚、鱿鱼这类普通水生生物时不会生成幼体。
// 1.21.2 里它们继承 AgeableMob，setBaby 会把年龄设为幼年起始值。
// 这个 Mixin 给三种目标生物补上同样的行为，让原版刷怪蛋的整个流程
// （生成新个体、调用 setBaby、检查 isBaby、放行）对它们也能生效，
// 但并不会给它们添加繁殖逻辑，只会让刷怪蛋生成幼体这件事成立。
@Mixin(Mob.class)
public abstract class FutureMCSetBabyMixin {

    // 拦下 setBaby 的调用，目标生物直接改成设置年龄，和原版 AgeableMob.setBaby 一致。
    @Inject(method = "setBaby", at = @At("HEAD"), cancellable = true)
    private void futuremc$setBaby(boolean baby, CallbackInfo ci) {
        // 只有我们移植的三种水生生物才处理，其他生物保持原版空实现。
        if (FutureMCEntityData.isAgeableWaterCreature((Mob) (Object) this)) {
            // 原版 AgeableMob.setBaby 的写法：幼年设为负的起始年龄，成年归零。
            ((FutureMCAgeable) this).futuremc$setAge(baby ? AgeableMob.BABY_START_AGE : 0);
            ci.cancel();
        }
    }
}
