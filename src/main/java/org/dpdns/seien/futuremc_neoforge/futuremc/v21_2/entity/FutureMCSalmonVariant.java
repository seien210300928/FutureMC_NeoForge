package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.animal.Salmon;

// 鲑鱼大小变种的枚举，对应 1.21.2 原版新增的功能。
// 从 1.21.2 开始，自然生成的鲑鱼会随机出现三种大小：小型、中型和大型，
// 其中中型最普通。各变种的体型缩放、序列化名字和自然生成权重都
// 与 1.21.2 原版数据保持一致，生成权重是小型 30、中型 50、大型 15。
// 1.21.1 的鲑鱼没有这个字段，由 Mixin 在 LivingEntity 层补上同步数据和保存逻辑。
public enum FutureMCSalmonVariant implements StringRepresentable {

    // 小型鲑鱼，体型是普通鲑鱼的 0.5 倍。
    SMALL("small", 0, 0.5F),
    // 中型鲑鱼，体型是普通大小，也是默认变种。
    MEDIUM("medium", 1, 1.0F),
    // 大型鲑鱼，体型是普通鲑鱼的 1.5 倍。
    LARGE("large", 2, 1.5F);

    // 默认变种，存档里没有记录大小或者读到未知名字时都按中型处理。
    public static final FutureMCSalmonVariant DEFAULT = MEDIUM;

    // 变种的同步数据访问器，整数，对应 1.21.2 原版鲑鱼的变种数据。
    public static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(Salmon.class, EntityDataSerializers.INT);

    // 所有枚举值按声明顺序排列，方便用数字下标查找。
    private static final FutureMCSalmonVariant[] BY_ID = values();

    // 变种的序列化名字，写进存档时用的就是它。
    private final String name;
    // 变种的数字编号，同步给客户端时用的就是它。
    private final int id;
    // 变种的体型缩放系数。
    private final float scale;

    // 枚举构造，三个字段分别是名字、编号和缩放系数。
    FutureMCSalmonVariant(String name, int id, float scale) {
        this.name = name;
        this.id = id;
        this.scale = scale;
    }

    // 返回序列化名字，用于写入存档。
    @Override
    public String getSerializedName() {
        return this.name;
    }

    // 返回数字编号，用于同步给客户端。
    public int id() {
        return this.id;
    }

    // 返回体型缩放系数。
    public float scale() {
        return this.scale;
    }

    // 根据数字编号找到对应的变种，越界的编号按小型或大型兜底处理。
    public static FutureMCSalmonVariant byId(int id) {
        if (id <= 0) {
            return SMALL;
        }
        return id >= BY_ID.length ? LARGE : BY_ID[id];
    }

    // 根据存档里的名字找到对应的变种，找不到就返回默认的中型。
    public static FutureMCSalmonVariant fromName(String name) {
        for (FutureMCSalmonVariant variant : BY_ID) {
            if (variant.name.equals(name)) {
                return variant;
            }
        }
        return DEFAULT;
    }

    // 读取指定鲑鱼当前变种的缩放系数，供渲染和碰撞箱使用。
    // 这个方法只能在实体构造完成后调用，因为那时实体的数据访问器才定义好。
    public static float scaleOf(Salmon salmon) {
        return byId(salmon.getEntityData().get(DATA_TYPE)).scale();
    }
}
