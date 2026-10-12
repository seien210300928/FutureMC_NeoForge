package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Garden Awakens（1.21.4 苍白花园系列）的物品注册表。
 * <p>
 * 大多数物品是对应方块的方块物品；告示牌、悬挂告示牌、船和刷怪蛋属于
 * 需要特殊构造的独立物品。全部挂载在 futuremc 命名空间。
 */
public final class ModItems {
    /** 物品注册表，使用 NeoForge 的 DeferredRegister.Items 便捷封装。 */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("futuremc");

    // —— 方块物品（对应 ModBlocks 里的方块） ——
    /** 苍白橡木木板。 */
    public static final DeferredItem<Item> PALE_OAK_PLANKS = registerBlockItem("pale_oak_planks", ModBlocks.PALE_OAK_PLANKS);
    /** 苍白橡木原木。 */
    public static final DeferredItem<Item> PALE_OAK_LOG = registerBlockItem("pale_oak_log", ModBlocks.PALE_OAK_LOG);
    /** 苍白橡木（六面树皮）。 */
    public static final DeferredItem<Item> PALE_OAK_WOOD = registerBlockItem("pale_oak_wood", ModBlocks.PALE_OAK_WOOD);
    /** 去皮苍白橡木原木。 */
    public static final DeferredItem<Item> STRIPPED_PALE_OAK_LOG = registerBlockItem("stripped_pale_oak_log", ModBlocks.STRIPPED_PALE_OAK_LOG);
    /** 去皮苍白橡木。 */
    public static final DeferredItem<Item> STRIPPED_PALE_OAK_WOOD = registerBlockItem("stripped_pale_oak_wood", ModBlocks.STRIPPED_PALE_OAK_WOOD);
    /** 苍白橡木树叶。 */
    public static final DeferredItem<Item> PALE_OAK_LEAVES = registerBlockItem("pale_oak_leaves", ModBlocks.PALE_OAK_LEAVES);
    /** 苍白橡木树苗。 */
    public static final DeferredItem<Item> PALE_OAK_SAPLING = registerBlockItem("pale_oak_sapling", ModBlocks.PALE_OAK_SAPLING);
    /** 苍白橡木楼梯。 */
    public static final DeferredItem<Item> PALE_OAK_STAIRS = registerBlockItem("pale_oak_stairs", ModBlocks.PALE_OAK_STAIRS);
    /** 苍白橡木台阶。 */
    public static final DeferredItem<Item> PALE_OAK_SLAB = registerBlockItem("pale_oak_slab", ModBlocks.PALE_OAK_SLAB);
    /** 苍白橡木栅栏。 */
    public static final DeferredItem<Item> PALE_OAK_FENCE = registerBlockItem("pale_oak_fence", ModBlocks.PALE_OAK_FENCE);
    /** 苍白橡木栅栏门。 */
    public static final DeferredItem<Item> PALE_OAK_FENCE_GATE = registerBlockItem("pale_oak_fence_gate", ModBlocks.PALE_OAK_FENCE_GATE);
    /** 苍白橡木门。 */
    public static final DeferredItem<Item> PALE_OAK_DOOR = registerBlockItem("pale_oak_door", ModBlocks.PALE_OAK_DOOR);
    /** 苍白橡木活板门。 */
    public static final DeferredItem<Item> PALE_OAK_TRAPDOOR = registerBlockItem("pale_oak_trapdoor", ModBlocks.PALE_OAK_TRAPDOOR);
    /** 苍白橡木压力板。 */
    public static final DeferredItem<Item> PALE_OAK_PRESSURE_PLATE = registerBlockItem("pale_oak_pressure_plate", ModBlocks.PALE_OAK_PRESSURE_PLATE);
    /** 苍白橡木按钮。 */
    public static final DeferredItem<Item> PALE_OAK_BUTTON = registerBlockItem("pale_oak_button", ModBlocks.PALE_OAK_BUTTON);
    /** 苍白苔藓方块。 */
    public static final DeferredItem<Item> PALE_MOSS_BLOCK = registerBlockItem("pale_moss_block", ModBlocks.PALE_MOSS_BLOCK);
    /** 苍白苔藓地毯。 */
    public static final DeferredItem<Item> PALE_MOSS_CARPET = registerBlockItem("pale_moss_carpet", ModBlocks.PALE_MOSS_CARPET);
    /** 苍白悬挂苔藓。 */
    public static final DeferredItem<Item> PALE_HANGING_MOSS = registerBlockItem("pale_hanging_moss", ModBlocks.PALE_HANGING_MOSS);
    /** 树脂簇。 */
    public static final DeferredItem<Item> RESIN_CLUMP = registerBlockItem("resin_clump", ModBlocks.RESIN_CLUMP);
    /** 树脂块。 */
    public static final DeferredItem<Item> RESIN_BLOCK = registerBlockItem("resin_block", ModBlocks.RESIN_BLOCK);
    /** 树脂砖。 */
    public static final DeferredItem<Item> RESIN_BRICKS = registerBlockItem("resin_bricks", ModBlocks.RESIN_BRICKS);
    /** 雕纹树脂砖。 */
    public static final DeferredItem<Item> CHISELED_RESIN_BRICKS = registerBlockItem("chiseled_resin_bricks", ModBlocks.CHISELED_RESIN_BRICKS);
    /** 树脂砖楼梯。 */
    public static final DeferredItem<Item> RESIN_BRICK_STAIRS = registerBlockItem("resin_brick_stairs", ModBlocks.RESIN_BRICK_STAIRS);
    /** 树脂砖台阶。 */
    public static final DeferredItem<Item> RESIN_BRICK_SLAB = registerBlockItem("resin_brick_slab", ModBlocks.RESIN_BRICK_SLAB);
    /** 树脂砖墙。 */
    public static final DeferredItem<Item> RESIN_BRICK_WALL = registerBlockItem("resin_brick_wall", ModBlocks.RESIN_BRICK_WALL);
    /** 嘎吱之心。 */
    public static final DeferredItem<Item> CREAKING_HEART = registerBlockItem("creaking_heart", ModBlocks.CREAKING_HEART);
    /** 开放眼眸花。 */
    public static final DeferredItem<Item> OPEN_EYEBLOSSOM = registerBlockItem("open_eyeblossom", ModBlocks.OPEN_EYEBLOSSOM);
    /** 闭合眼眸花。 */
    public static final DeferredItem<Item> CLOSED_EYEBLOSSOM = registerBlockItem("closed_eyeblossom", ModBlocks.CLOSED_EYEBLOSSOM);

    // —— 独立物品 ——
    /** 苍白橡木告示牌：可同时放置在地上与墙上的双状态物品。 */
    public static final DeferredItem<Item> PALE_OAK_SIGN = ITEMS.register("pale_oak_sign",
        () -> new SignItem(new Item.Properties().stacksTo(16), ModBlocks.PALE_OAK_SIGN.get(), ModBlocks.PALE_OAK_WALL_SIGN.get()));

    /** 苍白橡木悬挂告示牌：可同时挂在方块下方与墙面侧下方的双状态物品。 */
    public static final DeferredItem<Item> PALE_OAK_HANGING_SIGN = ITEMS.register("pale_oak_hanging_sign",
        () -> new HangingSignItem(ModBlocks.PALE_OAK_HANGING_SIGN.get(), ModBlocks.PALE_OAK_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));

    /** 树脂砖材料：合成树脂砖系列方块用的原材料。 */
    public static final DeferredItem<Item> RESIN_BRICK = ITEMS.register("resin_brick", () -> new Item(new Item.Properties()));

    /** 苍白橡木船。 */
    public static final DeferredItem<Item> PALE_OAK_BOAT = ITEMS.register("pale_oak_boat",
        () -> new PaleOakBoatItem(ModEntities.PALE_OAK_BOAT.get(), false, new Item.Properties().stacksTo(1)));

    /** 苍白橡木箱子船。 */
    public static final DeferredItem<Item> PALE_OAK_CHEST_BOAT = ITEMS.register("pale_oak_chest_boat",
        () -> new PaleOakBoatItem(ModEntities.PALE_OAK_CHEST_BOAT.get(), true, new Item.Properties().stacksTo(1)));

    /** 嘎吱刷怪蛋：灰（0x5F5F5F）与橙（16545810）双色。 */
    public static final DeferredItem<Item> CREAKING_SPAWN_EGG = ITEMS.register("creaking_spawn_egg",
        () -> new SpawnEggItem(ModEntities.CREAKING.get(), 0x5F5F5F, 16545810, new Item.Properties()));

    /** 注册一个方块物品，直接把方块放入物品栏。 */
    private static DeferredItem<Item> registerBlockItem(String name, DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModItems() {
    }
}
