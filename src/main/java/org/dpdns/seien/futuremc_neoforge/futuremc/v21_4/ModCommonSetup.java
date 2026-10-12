package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.entity.Creaking;

/**
 * Garden Awakens（1.21.4）内容的通用初始化。
 * <p>
 * 在模组事件总线上把实体属性（生命值、移动速度、攻击伤害等）注册给
 * 对应实体类型，属性集合来自 Creaking.createAttributes()。
 */
@EventBusSubscriber(modid = "futuremc_neoforge", bus = EventBusSubscriber.Bus.MOD)
public final class ModCommonSetup {

    /** 注册嘎吱的实体属性。 */
    @SubscribeEvent
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CREAKING.get(), Creaking.createAttributes().build());
    }

    private ModCommonSetup() {
    }
}
