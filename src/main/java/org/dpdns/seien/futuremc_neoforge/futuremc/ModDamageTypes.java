package org.dpdns.seien.futuremc_neoforge.futuremc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

// 1.21.2 新增伤害类型的注册类。
// 从 1.21.2 开始，玩家手持重锤下落猛击敌人时会使用独立的伤害类型 mace_smash，
// 死亡提示显示为被猛击粉碎。这个类型本身是数据包内容，定义文件放在
// data/futuremc/damage_type/mace_smash.json，字段与 1.21.2 原版保持一致。
public final class ModDamageTypes {

    // mace_smash 伤害类型的键，指向数据包里的同名字段。
    public static final ResourceKey<DamageType> MACE_SMASH = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(FutureMC.NAMESPACE, "mace_smash")
    );

    // 工具类不允许被实例化。
    private ModDamageTypes() {
    }
}
