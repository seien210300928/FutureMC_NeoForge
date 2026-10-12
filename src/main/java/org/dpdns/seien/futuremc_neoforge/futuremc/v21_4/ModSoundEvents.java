package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Garden Awakens（1.21.4）的声音事件注册表。
 * <p>
 * 声音键与 26.3 原版保持一致（block.creaking_heart.*、entity.creaking.* 等），
 * 并通过 DeferredSoundType 把方块音效组（破坏/脚步/放置/敲击/摔落）绑定到
 * 对应方块上。这里只注册事件，声音文件本身在 resourcepacks/21_4 的
 * sounds.json 里引用原版声音。
 */
public final class ModSoundEvents {
    /** 声音事件注册表。 */
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, "futuremc");

    // —— 嘎吱之心的方块音效 ——
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_BREAK = register("block.creaking_heart.break");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_STEP = register("block.creaking_heart.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_PLACE = register("block.creaking_heart.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_HIT = register("block.creaking_heart.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_FALL = register("block.creaking_heart.fall");
    /** 嘎吱之心空闲时的环境音（夜晚且被原木包围时播放）。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_IDLE = register("block.creaking_heart.idle");
    /** 嘎吱之心召唤嘎吱时播放。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_SPAWN = register("block.creaking_heart.spawn");
    /** 嘎吱之心受击传导音（从嘎吱位置传到心）。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_HEART_HURT = register("block.creaking_heart.hurt");

    // —— 眼眸花的音效 ——
    public static final DeferredHolder<SoundEvent, SoundEvent> EYEBLOSSOM_OPEN = register("block.eyeblossom.open");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYEBLOSSOM_CLOSE = register("block.eyeblossom.close");
    /** 长时间切换（随机刻）用的变体。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> EYEBLOSSOM_OPEN_LONG = register("block.eyeblossom.open_long");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYEBLOSSOM_CLOSE_LONG = register("block.eyeblossom.close_long");
    /** 眼眸花生长的环境音。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> EYEBLOSSOM_IDLE = register("block.eyeblossom.idle");

    /** 苍白悬挂苔藓的环境音。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> PALE_HANGING_MOSS_IDLE = register("block.pale_hanging_moss.idle");

    // —— 树脂的音效 ——
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_BREAK = register("block.resin.break");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_STEP = register("block.resin.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_PLACE = register("block.resin.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_FALL = register("block.resin.fall");

    // —— 树脂砖的音效 ——
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_BRICKS_BREAK = register("block.resin_bricks.break");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_BRICKS_STEP = register("block.resin_bricks.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_BRICKS_PLACE = register("block.resin_bricks.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_BRICKS_HIT = register("block.resin_bricks.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESIN_BRICKS_FALL = register("block.resin_bricks.fall");

    // —— 嘎吱实体的音效 ——
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_ACTIVATE = register("entity.creaking.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_AMBIENT = register("entity.creaking.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_ATTACK = register("entity.creaking.attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_DEACTIVATE = register("entity.creaking.deactivate");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_DEATH = register("entity.creaking.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_FREEZE = register("entity.creaking.freeze");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_SPAWN = register("entity.creaking.spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_STEP = register("entity.creaking.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_SWAY = register("entity.creaking.sway");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_TWITCH = register("entity.creaking.twitch");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_UNFREEZE = register("entity.creaking.unfreeze");

    /** 嘎吱之心的方块音效组（懒加载，避免注册前就解析引用）。 */
    private static SoundType creakingHeartSoundType;
    /** 树脂的方块音效组。 */
    private static SoundType resinSoundType;
    /** 树脂砖的方块音效组。 */
    private static SoundType resinBricksSoundType;

    /** 返回嘎吱之心的方块音效组。 */
    public static SoundType creakingHeartSoundType() {
        if (creakingHeartSoundType == null) {
            creakingHeartSoundType = new DeferredSoundType(1.0F, 1.0F,
                CREAKING_HEART_BREAK, CREAKING_HEART_STEP, CREAKING_HEART_PLACE, CREAKING_HEART_HIT, CREAKING_HEART_FALL);
        }
        return creakingHeartSoundType;
    }

    /** 返回树脂的方块音效组（敲击音用空事件，树脂块被敲不出声）。 */
    public static SoundType resinSoundType() {
        if (resinSoundType == null) {
            resinSoundType = new DeferredSoundType(1.0F, 1.0F,
                RESIN_BREAK, RESIN_STEP, RESIN_PLACE, () -> SoundEvents.EMPTY, RESIN_FALL);
        }
        return resinSoundType;
    }

    /** 返回树脂砖的方块音效组。 */
    public static SoundType resinBricksSoundType() {
        if (resinBricksSoundType == null) {
            resinBricksSoundType = new DeferredSoundType(1.0F, 1.0F,
                RESIN_BRICKS_BREAK, RESIN_BRICKS_STEP, RESIN_BRICKS_PLACE, RESIN_BRICKS_HIT, RESIN_BRICKS_FALL);
        }
        return resinBricksSoundType;
    }

    /** 注册一个可变范围声音事件（键名即 futuremc 命名空间下的资源路径）。 */
    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("futuremc", name)));
    }

    private ModSoundEvents() {
    }
}
