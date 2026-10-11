package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.GlowSquid;

// 幼年水生生物移植共用的实体判断与缩放数据。
// 从 1.21.2 开始，海豚、鱿鱼和发光鱿鱼有了 AgeableMob 式的年龄数据：
// 海豚幼年个体的体型是成年个体的 0.65 倍，鱿鱼和发光鱿鱼是 0.5 倍，
// 并且会按一定概率自然生成幼年个体。1.21.1 里这三种生物只是普通的水生动物，
// 所以由 FutureMCAgeableMixin 在 LivingEntity 层补上年龄字段、同步标志和成长逻辑。
// 幼年标志的同步数据访问器定义在 FutureMCAgeableMixin 里，作为 LivingEntity
// 类的静态字段存在，这样才能借助 JVM 的类加载顺序安全地分配数据槽编号。
public final class FutureMCEntityData {

    // 海豚幼年个体的缩放系数，对应 1.21.2 原版海豚的幼年比例 0.65。
    public static final float DOLPHIN_BABY_SCALE = 0.65F;

    // 鱿鱼和发光鱿鱼幼年个体的缩放系数，对应 1.21.2 原版的 0.5 比例。
    public static final float SQUID_BABY_SCALE = 0.5F;

    // 判断一个活体实体是不是我们移植的三种幼年水生生物。
    public static boolean isAgeableWaterCreature(LivingEntity entity) {
        return entity instanceof Dolphin || entity instanceof Squid || entity instanceof GlowSquid;
    }

    // 提前完成鲑鱼大小变种数据访问器的初始化。
    // 鲑鱼变种的访问器定义在 FutureMCSalmonVariant 里，属于懒加载，运行时才
    // 初始化也没关系（鲑鱼没有子类，新槽位不会与任何人冲突）。但为了稳妥，
    // 还是在模组加载的最早期先触发鲑鱼及其父类链的类初始化，让槽位编号
    // 在稳定的顺序里确定下来，避免任何加载顺序变化造成的问题。
    public static void ensureEarly() {
        ClassLoader classLoader = FutureMCEntityData.class.getClassLoader();
        try {
            // 触发鲑鱼及其父类链的类初始化，让原版槽位先按原版顺序分配。
            Class.forName("net.minecraft.world.entity.animal.Salmon", true, classLoader);
            // 再触发 FutureMCSalmonVariant 的类初始化（第二个参数 true 表示执行
            // 静态初始化），把大小槽位排到鲑鱼原版槽位之后。
            Class.forName("org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity.FutureMCSalmonVariant", true, classLoader);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("无法提前初始化未来内容的实体数据访问器", exception);
        }
    }

    // 工具类不允许被实例化。
    private FutureMCEntityData() {
    }
}
