package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2;

import java.util.Comparator;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

// 1.21.2 原版在 TicketType 中新增了末影珍珠加载票常量。
// TicketType 是原版 final 类，mod 无法直接修改，这里用普通类持有等价常量，
// 定义与 1.21.2 原版完全一致：按区块坐标排序，四十刻后过期。
public final class FutureMCTicketTypes {
    // 末影珍珠加载票。
    public static final TicketType<ChunkPos> ENDER_PEARL = TicketType.create("ender_pearl", Comparator.comparingLong(ChunkPos::toLong), 40);

    private FutureMCTicketTypes() {
    }
}
