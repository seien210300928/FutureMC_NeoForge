package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.state.properties;

import net.minecraft.util.StringRepresentable;

/**
 * 嘎吱之心的方块状态枚举。
 * <p>
 * 对应 26.3 原版的三个状态：被拔除（周围没有足够的苍白橡木原木）、
 * 沉睡（白天）与苏醒（夜晚且被原木包围）。字符串序列化名用于
 * 方块状态 JSON 与存档。
 */
public enum CreakingHeartState implements StringRepresentable {
    /** 被拔除：周围缺少原木，不再提供保护与召唤。 */
    UPROOTED("uprooted"),
    /** 沉睡：白天状态，不会召唤嘎吱。 */
    DORMANT("dormant"),
    /** 苏醒：夜晚状态，会召唤嘎吱守护。 */
    AWAKE("awake");

    private final String name;

    CreakingHeartState(String name) {
        this.name = name;
    }

    /** 返回序列化名称（用于方块状态与数据驱动）。 */
    @Override
    public String getSerializedName() {
        return this.name;
    }
}
