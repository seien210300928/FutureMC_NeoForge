package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.CreakingHeartBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.EyeblossomBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.PaleHangingMossBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.PaleMossBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.PottedEyeblossomBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.ResinClumpBlock;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.block.StrippableLogBlock;

/**
 * Garden Awakens（1.21.4 苍白花园系列）的方块注册表。
 * <p>
 * 全部方块使用 futuremc 命名空间注册，方块属性（硬度、地图颜色、音效等）参照
 * 26.3 原版的对应方块逐项移植；1.21.1 没有的原版机制（如 DeferredSoundType、
 * 可剥皮原木的工具修改状态）通过 NeoForge 的 API 等价实现。
 */
public final class ModBlocks {
    /** 方块注册表，所有方块都挂载在 futuremc 命名空间下。 */
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "futuremc");

    /** 苍白橡木的方块组类型，注册后供门、压力板、按钮等方块共享行为。 */
    private static final BlockSetType PALE_OAK_BLOCK_SET = BlockSetType.register(new BlockSetType("pale_oak"));
    /** 苍白橡木的木头类型，注册后供告示牌、栅栏门等方块绑定对应纹理与声音。 */
    public static final WoodType PALE_OAK_WOOD_TYPE = WoodType.register(new WoodType("pale_oak", PALE_OAK_BLOCK_SET));

    /** 苍白橡木原木的方块标签，嘎吱之心检测周围原木时使用该标签判断。 */
    public static final TagKey<Block> PALE_OAK_LOGS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("futuremc", "pale_oak_logs"));

    // —— 苍白橡木木制方块家族 ——
    /** 苍白橡木木板：石英色地图颜色、贝斯乐器音、2 点硬度，可燃。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_PLANKS = register("pale_oak_planks",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F, 3.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()));

    /** 去皮苍白橡木原木：与木板同色的纵向纹理方块。 */
    public static final DeferredHolder<Block, Block> STRIPPED_PALE_OAK_LOG = register("stripped_pale_oak_log",
        () -> new RotatedPillarBlock(logProperties(PALE_OAK_PLANKS.get().defaultMapColor(), PALE_OAK_PLANKS.get().defaultMapColor(), SoundType.WOOD)));

    /** 去皮苍白橡木：六个面都是去皮纹理的方块。 */
    public static final DeferredHolder<Block, Block> STRIPPED_PALE_OAK_WOOD = register("stripped_pale_oak_wood",
        () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()));

    /** 苍白橡木：六面树皮纹理，可剥皮。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_WOOD = register("pale_oak_wood",
        () -> new StrippableLogBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava(), STRIPPED_PALE_OAK_WOOD));

    /** 苍白橡木原木：树皮面是石头色，使用可剥皮方块实现斧头去皮。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_LOG = register("pale_oak_log",
        () -> new StrippableLogBlock(
            logProperties(PALE_OAK_PLANKS.get().defaultMapColor(), PALE_OAK_WOOD.get().defaultMapColor(), SoundType.WOOD),
            STRIPPED_PALE_OAK_LOG));

    /** 苍白橡木树叶：金属色地图颜色、0.2 硬度、随机刻，允许豹猫与鹦鹉生成。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_LEAVES = register("pale_oak_leaves",
        () -> new LeavesBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn(ModBlocks::ocelotOrParrot)
            .isSuffocating(ModBlocks::never)
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY)
            .isRedstoneConductor(ModBlocks::never)));

    /** 苍白橡木树苗：使用苍白橡木的树生长器，无碰撞箱、随机刻。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_SAPLING = register("pale_oak_sapling",
        () -> new SaplingBlock(ModTreeGrowers.PALE_OAK, BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.GRASS)
            .pushReaction(PushReaction.DESTROY)));

    /** 苍白橡木楼梯：材质继承木板方块。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_STAIRS = register("pale_oak_stairs",
        () -> new StairBlock(PALE_OAK_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(PALE_OAK_PLANKS.get())));

    /** 苍白橡木台阶。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_SLAB = register("pale_oak_slab",
        () -> new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(PALE_OAK_PLANKS.get())));

    /** 苍白橡木栅栏。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_FENCE = register("pale_oak_fence",
        () -> new FenceBlock(BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F, 3.0F)
            .ignitedByLava()
            .sound(SoundType.WOOD)));

    /** 苍白橡木栅栏门：绑定苍白橡木的木头类型。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_FENCE_GATE = register("pale_oak_fence_gate",
        () -> new FenceGateBlock(PALE_OAK_WOOD_TYPE, BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
            .forceSolidOn()
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F, 3.0F)
            .ignitedByLava()));

    /** 苍白橡木门：使用苍白橡木方块组类型，3 点硬度。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_DOOR = register("pale_oak_door",
        () -> new DoorBlock(PALE_OAK_BLOCK_SET, BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
            .instrument(NoteBlockInstrument.BASS)
            .strength(3.0F)
            .noOcclusion()
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY)));

    /** 苍白橡木活板门。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_TRAPDOOR = register("pale_oak_trapdoor",
        () -> new TrapDoorBlock(PALE_OAK_BLOCK_SET, BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
            .instrument(NoteBlockInstrument.BASS)
            .strength(3.0F)
            .noOcclusion()
            .isValidSpawn(ModBlocks::neverSpawn)
            .ignitedByLava()));

    /** 苍白橡木压力板。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_PRESSURE_PLATE = register("pale_oak_pressure_plate",
        () -> new PressurePlateBlock(PALE_OAK_BLOCK_SET, BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
            .forceSolidOn()
            .instrument(NoteBlockInstrument.BASS)
            .noCollission()
            .strength(0.5F)
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY)));

    /** 苍白橡木按钮：按住 30 刻后弹起。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_BUTTON = register("pale_oak_button",
        () -> new ButtonBlock(PALE_OAK_BLOCK_SET, 30, BlockBehaviour.Properties.of()
            .noCollission()
            .strength(0.5F)
            .pushReaction(PushReaction.DESTROY)));

    // —— 告示牌（含墙牌与悬挂牌变体） ——
    /** 苍白橡木告示牌（立在地上的那一种）。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_SIGN = register("pale_oak_sign",
        () -> new StandingSignBlock(PALE_OAK_WOOD_TYPE, signProperties()));

    /** 苍白橡木墙告示牌，贴在墙上时使用。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_WALL_SIGN = register("pale_oak_wall_sign",
        () -> new WallSignBlock(PALE_OAK_WOOD_TYPE, signProperties()));

    /** 苍白橡木悬挂告示牌（挂在方块下方的那种）。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_HANGING_SIGN = register("pale_oak_hanging_sign",
        () -> new CeilingHangingSignBlock(PALE_OAK_WOOD_TYPE, signProperties()));

    /** 苍白橡木墙挂告示牌，挂在墙面侧下方时使用。 */
    public static final DeferredHolder<Block, Block> PALE_OAK_WALL_HANGING_SIGN = register("pale_oak_wall_hanging_sign",
        () -> new WallHangingSignBlock(PALE_OAK_WOOD_TYPE, signProperties()));

    // —— 苍白苔藓与悬挂苔藓 ——
    /** 苍白苔藓方块：浅灰色地图颜色、0.1 硬度，可被骨粉催熟。 */
    public static final DeferredHolder<Block, Block> PALE_MOSS_BLOCK = register("pale_moss_block",
        () -> new PaleMossBlock(BlockBehaviour.Properties.of()
            .ignitedByLava()
            .mapColor(MapColor.COLOR_LIGHT_GRAY)
            .strength(0.1F)
            .sound(SoundType.MOSS)
            .pushReaction(PushReaction.DESTROY)));

    /** 苍白苔藓地毯：地毯类方块，不可遮挡。 */
    public static final DeferredHolder<Block, Block> PALE_MOSS_CARPET = register("pale_moss_carpet",
        () -> new CarpetBlock(BlockBehaviour.Properties.of()
            .ignitedByLava()
            .mapColor(PALE_MOSS_BLOCK.get().defaultMapColor())
            .strength(0.1F)
            .sound(SoundType.MOSS_CARPET)
            .pushReaction(PushReaction.DESTROY)
            .noOcclusion()));

    /** 苍白悬挂苔藓：从方块底面垂下的苔藓，使用 tip 属性区分末端。 */
    public static final DeferredHolder<Block, Block> PALE_HANGING_MOSS = register("pale_hanging_moss",
        () -> new PaleHangingMossBlock(BlockBehaviour.Properties.of()
            .ignitedByLava()
            .mapColor(PALE_MOSS_BLOCK.get().defaultMapColor())
            .noCollission()
            .sound(SoundType.MOSS_CARPET)
            .pushReaction(PushReaction.DESTROY)));

    // —— 树脂系列 ——
    /** 树脂簇：橙色可替换的多面附着方块，附着在树皮上生长。 */
    public static final DeferredHolder<Block, Block> RESIN_CLUMP = register("resin_clump",
        () -> new ResinClumpBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .replaceable()
            .noCollission()
            .sound(ModSoundEvents.resinSoundType())
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY)));

    /** 树脂块：橙色完整方块，使用树脂音效组。 */
    public static final DeferredHolder<Block, Block> RESIN_BLOCK = register("resin_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(ModSoundEvents.resinSoundType())));

    /** 树脂砖：需要正确工具挖掘（镐），使用树脂砖音效组。 */
    public static final DeferredHolder<Block, Block> RESIN_BRICKS = register("resin_bricks",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .sound(ModSoundEvents.resinBricksSoundType())
            .strength(1.5F, 6.0F)));

    /** 雕纹树脂砖。 */
    public static final DeferredHolder<Block, Block> CHISELED_RESIN_BRICKS = register("chiseled_resin_bricks",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .sound(ModSoundEvents.resinBricksSoundType())
            .strength(1.5F, 6.0F)));

    /** 树脂砖楼梯。 */
    public static final DeferredHolder<Block, Block> RESIN_BRICK_STAIRS = register("resin_brick_stairs",
        () -> new StairBlock(RESIN_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(RESIN_BRICKS.get())));

    /** 树脂砖台阶。 */
    public static final DeferredHolder<Block, Block> RESIN_BRICK_SLAB = register("resin_brick_slab",
        () -> new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(RESIN_BRICKS.get())));

    /** 树脂砖墙。 */
    public static final DeferredHolder<Block, Block> RESIN_BRICK_WALL = register("resin_brick_wall",
        () -> new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(RESIN_BRICKS.get())));

    // —— 嘎吱之心与眼眸花 ——
    /** 嘎吱之心：苍白花园的核心方块，具备沉睡/苏醒/被拔除三种状态。 */
    public static final DeferredHolder<Block, Block> CREAKING_HEART = register("creaking_heart",
        () -> new CreakingHeartBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .strength(10.0F)
            .sound(ModSoundEvents.creakingHeartSoundType())));

    /** 开放眼眸花：盛开状态的眼眸花，给蜜蜂上中毒效果。 */
    public static final DeferredHolder<Block, Block> OPEN_EYEBLOSSOM = register("open_eyeblossom",
        () -> new EyeblossomBlock(EyeblossomBlock.Type.OPEN, BlockBehaviour.Properties.of()
            .mapColor(CREAKING_HEART.get().defaultMapColor())
            .noCollission()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .pushReaction(PushReaction.DESTROY)
            .randomTicks()));

    /** 闭合眼眸花：夜晚闭合状态的眼眸花。 */
    public static final DeferredHolder<Block, Block> CLOSED_EYEBLOSSOM = register("closed_eyeblossom",
        () -> new EyeblossomBlock(EyeblossomBlock.Type.CLOSED, BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_LEAVES.get().defaultMapColor())
            .noCollission()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .pushReaction(PushReaction.DESTROY)
            .randomTicks()));

    // —— 花盆变体 ——
    /** 花盆里的苍白橡木树苗。 */
    public static final DeferredHolder<Block, Block> POTTED_PALE_OAK_SAPLING = register("potted_pale_oak_sapling",
        () -> new FlowerPotBlock(PALE_OAK_SAPLING.get(), flowerPotProperties()));

    /** 花盆里的开放眼眸花：带随机刻，随昼夜切换开放/闭合。 */
    public static final DeferredHolder<Block, Block> POTTED_OPEN_EYEBLOSSOM = register("potted_open_eyeblossom",
        () -> new PottedEyeblossomBlock(OPEN_EYEBLOSSOM.get(), flowerPotProperties().randomTicks()));

    /** 花盆里的闭合眼眸花。 */
    public static final DeferredHolder<Block, Block> POTTED_CLOSED_EYEBLOSSOM = register("potted_closed_eyeblossom",
        () -> new PottedEyeblossomBlock(CLOSED_EYEBLOSSOM.get(), flowerPotProperties().randomTicks()));

    /**
     * 原木/去皮原木共用的方块属性：轴向上的地图颜色按方块状态动态取色，
     * 竖直放置显示顶面色，水平放置显示树皮色。
     */
    private static BlockBehaviour.Properties logProperties(MapColor topColor, MapColor barkColor, SoundType soundType) {
        return BlockBehaviour.Properties.of()
            .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topColor : barkColor)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F)
            .sound(soundType)
            .ignitedByLava();
    }

    /** 告示牌方块共用的属性：无碰撞箱、1 点硬度、可燃。 */
    private static BlockBehaviour.Properties signProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
            .forceSolidOn()
            .instrument(NoteBlockInstrument.BASS)
            .noCollission()
            .strength(1.0F)
            .ignitedByLava();
    }

    /** 花盆方块共用的属性：瞬间破坏、无碰撞箱。 */
    private static BlockBehaviour.Properties flowerPotProperties() {
        return BlockBehaviour.Properties.of().instabreak().noCollission();
    }

    /** 叶子方块禁止充当窒息方块（让玩家能透过树叶呼吸）。 */
    private static Boolean never(BlockState state, BlockGetter getter, BlockPos pos) {
        return false;
    }

    /** 活板门禁止作为生物生成点。 */
    private static Boolean neverSpawn(BlockState state, BlockGetter getter, BlockPos pos, EntityType<?> type) {
        return false;
    }

    /** 树叶允许豹猫与鹦鹉在上面生成（与原版树叶行为一致）。 */
    private static Boolean ocelotOrParrot(BlockState state, BlockGetter getter, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }

    /** 注册一个方块，返回带类型的注册句柄。 */
    private static <T extends Block> DeferredHolder<Block, T> register(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private ModBlocks() {
    }
}
