package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.dpdns.seien.futuremc_neoforge.futuremc.FutureMC;

// 刷怪笼独立音效的注册类。
// 从 1.21.2 开始，刷怪笼不再使用普通的方块音效，而是有自己单独的一组声音事件，
// 对应原版 1.21.2 里的 block.spawner.break、block.spawner.step、block.spawner.place、
// block.spawner.hit 和 block.spawner.fall 五个事件。按项目规范，这些事件统一
// 注册在 futuremc 命名空间下，也就是 futuremc:block.spawner.break 这样的键。
public final class ModSoundEvents {

    // 音效事件的延迟注册表，所有事件都注册在 futuremc 命名空间下。
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, FutureMC.NAMESPACE);

    // 刷怪笼被破坏时的声音事件。
    public static final DeferredHolder<SoundEvent, SoundEvent> SPAWNER_BREAK = register("block.spawner.break");
    // 刷怪笼被踩踏（脚步）时的声音事件。
    public static final DeferredHolder<SoundEvent, SoundEvent> SPAWNER_STEP = register("block.spawner.step");
    // 刷怪笼被放置时的声音事件。
    public static final DeferredHolder<SoundEvent, SoundEvent> SPAWNER_PLACE = register("block.spawner.place");
    // 刷怪笼被敲击时的声音事件。
    public static final DeferredHolder<SoundEvent, SoundEvent> SPAWNER_HIT = register("block.spawner.hit");
    // 刷怪笼摔落时的声音事件。
    public static final DeferredHolder<SoundEvent, SoundEvent> SPAWNER_FALL = register("block.spawner.fall");

    // 刷怪笼专用的声音类型，第一次用到时才创建。
    private static SoundType spawnerSoundType;

    // 返回刷怪笼专用的声音类型。音量和音调都取 1.0，与 1.21.2 原版的刷怪笼声音类型一致，
    // 五个事件依次对应破坏、脚步、放置、敲击和摔落。
    // 声音类型只能在注册完成之后创建，所以这里延迟到第一次调用时再构建；
    // 用 NeoForge 的 DeferredSoundType 延迟解析声音事件，避开原版 SoundType
    // 构造器的弃用警告。
    public static SoundType spawnerSoundType() {
        if (spawnerSoundType == null) {
            // DeferredHolder 本身实现了 Supplier，直接把注册句柄传给
            // DeferredSoundType，它会在首次调用 get*Sound() 时才解析事件。
            spawnerSoundType = new DeferredSoundType(
                1.0F, 1.0F,
                SPAWNER_BREAK, SPAWNER_STEP, SPAWNER_PLACE, SPAWNER_HIT, SPAWNER_FALL
            );
        }
        return spawnerSoundType;
    }

    // 用给定的名字在 futuremc 命名空间下注册一个可变范围的音效事件。
    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(
            name,
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FutureMC.NAMESPACE, name))
        );
    }

    // 工具类不允许被实例化。
    private ModSoundEvents() {
    }
}
