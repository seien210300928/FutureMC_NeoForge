package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.enderpearl;

import net.minecraft.world.entity.projectile.ThrownEnderpearl;

// 末影珍珠 mixin 与玩家 mixin 之间的访问桥。
// ServerPlayer 的 Mixin 实现了这几个方法，珍珠在 tick 和移除时通过它
// 维护自己的加载票和所有者列表，逻辑照搬 1.21.2 原版 ServerPlayer。
// 接口不能放在 mixin 包内：mixin 包中的类被 mixin 系统管理，
// 外部直接引用会触发 IllegalClassLoadError，故放在普通包中。
public interface FutureMCEnderPearlPlayerAccessor {

    // 把珍珠登记到玩家的飞行珍珠集合里。
    void futuremc$registerEnderPearl(ThrownEnderpearl enderPearl);

    // 把珍珠从玩家的飞行珍珠集合里移除。
    void futuremc$deregisterEnderPearl(ThrownEnderpearl enderPearl);

    // 登记珍珠并更新它所在区块的加载票，返回下次更新前经过的刻数。
    long futuremc$registerAndUpdateEnderPearlTicket(ThrownEnderpearl enderPearl);
}
