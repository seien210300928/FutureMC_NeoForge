package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.GameRules;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.enderpearl.FutureMCEnderPearlPlayerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.2 起投掷的末影珍珠会强制加载并 tick 其经过的区块：
// 珍珠每刻检查所在区块，周期性地为区块放置加载票，避免落地时区块未加载。
// 1.21.1 的末影珍珠没有这些逻辑，这里整体照搬 1.21.2 原版 ThrownEnderpearl。
// 1.21.2 的珍珠重写了 setOwner/onRemoval 来维护玩家飞行集合登记，
// 而 1.21.1 的珍珠类没有这两个重写（@Invoker 只能解析目标类自身的方法），
// 因此在珍珠本类的 tick/onHit 上按等价语义挂接所有者的登记与注销。
@Mixin(ThrownEnderpearl.class)
public abstract class FutureMCEnderPearlMixin {

    // 加载票的更新计时器：倒计时到零或珍珠跨入新区块时刷新加载票。
    @Unique
    private long futuremc$ticketTimer = 0L;

    // 上次检测到的所有者 ID，用于发现所有者变更后同步玩家的登记。
    @Unique
    private UUID futuremc$lastOwnerId = null;

    // 调用父类 ThrowableItemProjectile 的原始 tick。
    @Invoker("tick")
    abstract void futuremc$superTick();

    // 照搬 1.21.2 的 tick：玩家死亡按规则消失，存活时同步所有者登记并周期性更新区块加载票。
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void futuremc$tick(CallbackInfo ci) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        if (self.level() instanceof ServerLevel) {
            int i = SectionPos.blockToSectionCoord(self.position().x());
            int j = SectionPos.blockToSectionCoord(self.position().z());
            Entity owner = self.getOwner();
            if (owner instanceof ServerPlayer serverPlayer
                && !owner.isAlive()
                && !serverPlayer.wonGame
                && serverPlayer.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
                // 玩家死亡：先注销登记，再让珍珠消失。
                this.futuremc$deregisterFromCurrentOwner();
                self.discard();
            } else {
                this.futuremc$superTick();
            }
            if (self.isAlive()) {
                // 所有者变更时注销旧所有者的登记并登记新所有者，等价于 1.21.2 原版重写 setOwner。
                UUID ownerId = owner != null ? owner.getUUID() : null;
                if (!Objects.equals(ownerId, this.futuremc$lastOwnerId)) {
                    this.futuremc$syncOwnerRegistration(owner);
                    this.futuremc$lastOwnerId = ownerId;
                }
                BlockPos blockPos = BlockPos.containing(self.position());
                if ((--this.futuremc$ticketTimer <= 0L
                        || i != SectionPos.blockToSectionCoord(blockPos.getX())
                        || j != SectionPos.blockToSectionCoord(blockPos.getZ()))
                    && owner instanceof ServerPlayer serverPlayer) {
                    this.futuremc$ticketTimer =
                        ((FutureMCEnderPearlPlayerAccessor) serverPlayer).futuremc$registerAndUpdateEnderPearlTicket(self);
                }
            }
        } else {
            this.futuremc$superTick();
        }
        ci.cancel();
    }

    // 珍珠命中后传送并消失，从所有者列表注销登记，对应 1.21.2 原版 onRemoval 的注销逻辑。
    @Inject(method = "onHit", at = @At("TAIL"))
    private void futuremc$onHitTail(CallbackInfo ci) {
        this.futuremc$deregisterFromCurrentOwner();
    }

    // 按上次记录的所有者 ID 注销旧所有者的登记，再登记当前所有者。
    @Unique
    private void futuremc$syncOwnerRegistration(Entity owner) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        if (this.futuremc$lastOwnerId != null
            && self.level() instanceof ServerLevel serverLevel
            && serverLevel.getServer() != null) {
            ServerPlayer oldPlayer = serverLevel.getServer().getPlayerList().getPlayer(this.futuremc$lastOwnerId);
            if (oldPlayer != null) {
                ((FutureMCEnderPearlPlayerAccessor) oldPlayer).futuremc$deregisterEnderPearl(self);
            }
        }
        if (owner instanceof ServerPlayer serverPlayer) {
            ((FutureMCEnderPearlPlayerAccessor) serverPlayer).futuremc$registerEnderPearl(self);
        }
    }

    // 如果当前所有者是服务端玩家，把珍珠从它的飞行集合里移除。
    @Unique
    private void futuremc$deregisterFromCurrentOwner() {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        if (self.getOwner() instanceof ServerPlayer serverPlayer) {
            ((FutureMCEnderPearlPlayerAccessor) serverPlayer).futuremc$deregisterEnderPearl(self);
        }
    }
}
