package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModDamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// 重锤粉碎攻击的死亡提示替换。
// 从 1.21.2 开始，玩家手持重锤从高处下落猛击敌人时，游戏会使用独立的
// 伤害类型 mace_smash，死亡提示显示为被猛击粉碎，而不是普通的击杀文案。
// 1.21.1 的玩家攻击流程固定使用 playerAttack 伤害源，所以在这个调用点
// 做重定向：满足粉碎条件时换成 futuremc:mace_smash，否则保持原样。
@Mixin(Player.class)
public abstract class FutureMCMaceMixin {

    // 在玩家攻击流程里调用 playerAttack 的地方做重定向。
    @Redirect(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSources;playerAttack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/damagesource/DamageSource;"
        )
    )
    private DamageSource futuremc$maceSmashSource(DamageSources damagesources, Player target) {
        Player self = (Player) (Object) this;
        // 主手持重锤并且正处于下落粉碎状态时，使用 mace_smash 伤害类型。
        if (self.getMainHandItem().getItem() instanceof MaceItem && MaceItem.canSmashAttack(self)) {
            return damagesources.source(ModDamageTypes.MACE_SMASH, self);
        }
        // 其他情况保持原来的玩家攻击伤害源。
        return damagesources.playerAttack(target);
    }
}
