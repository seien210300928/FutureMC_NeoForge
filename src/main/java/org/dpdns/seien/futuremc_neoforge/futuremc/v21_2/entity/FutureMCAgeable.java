package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity;

// 幼年水生生物的年龄接口。
// 这个接口由 FutureMCAgeableMixin 注入到原版的 LivingEntity 类上，
// 让海豚、鱿鱼和发光鱿鱼拥有 1.21.2 才有的 AgeableMob 式幼年状态。
// 方法名以 futuremc$ 开头，避免以后和原版可能出现的方法重名冲突。
public interface FutureMCAgeable {

    // 返回当前年龄，负数表示幼年，正数表示成年。
    int futuremc$getAge();

    // 设置年龄，同时会更新同步给客户端的幼年标志。
    void futuremc$setAge(int age);

    // 判断是否为幼年个体。
    boolean futuremc$isBaby();

    // 让生物成长指定的游戏天数，第二个参数表示是否计入强制成长（用骨粉催熟那样的效果）。
    // 当前 mod 内没有调用者（这三种生物没有繁殖 AI，只有生成时按概率产生幼体），
    // 保留它是为了对齐 26.3 原版 AgeableMob 的完整年龄结构（26.3 里海豚、鱿鱼
    // 直接继承 AgeableMob，天然拥有这个方法），以后接繁殖/催熟逻辑时直接可用。
    @SuppressWarnings("unused")
    void futuremc$ageUp(int amount, boolean forced);
}
