package org.dpdns.seien.futuremc_neoforge.futuremc;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 1.21.2 新增的旗帜图案物品与收纳袋染色变种的注册类。
// 两个旗帜图案物品都对应原版已经存在的旗帜图案：棱纹旗帜图案对应砖纹图案
// （minecraft:bricks），锯齿边旗帜图案对应卷曲边图案（minecraft:curly_border），
// 所以不需要新增图案条目或图案贴图，只需要物品本身和指向原版图案的标签。
// 收纳袋染色对齐 1.21.2 正式版：收纳袋一共有 17 个物品，普通收纳袋加上
// 16 种染料颜色的独立变种（black_bundle、red_bundle 等），染色不是往普通
// 收纳袋上写颜色组件，而是直接变成对应颜色的物品，包内物品由配方保留。
public final class ModItems {

    // 物品的延迟注册表，所有物品都注册在 futuremc 命名空间下。
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FutureMC.NAMESPACE);

    // 棱纹旗帜图案物品，使用它纺织旗帜时会产生砖纹图案（原版已有的 bricks 图案）。
    public static final DeferredItem<BannerPatternItem> FIELD_MASONED_BANNER_PATTERN = ITEMS.register(
        "field_masoned_banner_pattern",
        () -> new BannerPatternItem(patternItemTag("field_masoned"), new Item.Properties())
    );

    // 锯齿边旗帜图案物品，使用它纺织旗帜时会产生卷曲边图案（原版已有的 curly_border 图案）。
    public static final DeferredItem<BannerPatternItem> BORDURE_INDENTED_BANNER_PATTERN = ITEMS.register(
        "bordure_indented_banner_pattern",
        () -> new BannerPatternItem(patternItemTag("bordure_indented"), new Item.Properties())
    );

    // 普通收纳袋（futuremc 命名空间下的基础收纳袋物品）。
    // 整个收纳袋体系（普通 + 16 色变种）都注册在 futuremc 命名空间下，
    // 不占用原版 minecraft:bundle，避免与原版内容或未来版本产生命名冲突。
    public static final DeferredItem<BundleItem> BUNDLE = ITEMS.register("bundle", () -> new BundleItem(bundleProperties()));

    // 黑色收纳袋变种，物品行为与普通收纳袋完全一致（BundleItem）。
    public static final DeferredItem<BundleItem> BLACK_BUNDLE = ITEMS.register("black_bundle", () -> new BundleItem(bundleProperties()));

    // 蓝色收纳袋变种。
    public static final DeferredItem<BundleItem> BLUE_BUNDLE = ITEMS.register("blue_bundle", () -> new BundleItem(bundleProperties()));

    // 棕色收纳袋变种。
    public static final DeferredItem<BundleItem> BROWN_BUNDLE = ITEMS.register("brown_bundle", () -> new BundleItem(bundleProperties()));

    // 青色收纳袋变种。
    public static final DeferredItem<BundleItem> CYAN_BUNDLE = ITEMS.register("cyan_bundle", () -> new BundleItem(bundleProperties()));

    // 灰色收纳袋变种。
    public static final DeferredItem<BundleItem> GRAY_BUNDLE = ITEMS.register("gray_bundle", () -> new BundleItem(bundleProperties()));

    // 绿色收纳袋变种。
    public static final DeferredItem<BundleItem> GREEN_BUNDLE = ITEMS.register("green_bundle", () -> new BundleItem(bundleProperties()));

    // 淡蓝色收纳袋变种。
    public static final DeferredItem<BundleItem> LIGHT_BLUE_BUNDLE = ITEMS.register("light_blue_bundle", () -> new BundleItem(bundleProperties()));

    // 淡灰色收纳袋变种。
    public static final DeferredItem<BundleItem> LIGHT_GRAY_BUNDLE = ITEMS.register("light_gray_bundle", () -> new BundleItem(bundleProperties()));

    // 黄绿色收纳袋变种。
    public static final DeferredItem<BundleItem> LIME_BUNDLE = ITEMS.register("lime_bundle", () -> new BundleItem(bundleProperties()));

    // 品红色收纳袋变种。
    public static final DeferredItem<BundleItem> MAGENTA_BUNDLE = ITEMS.register("magenta_bundle", () -> new BundleItem(bundleProperties()));

    // 橙色收纳袋变种。
    public static final DeferredItem<BundleItem> ORANGE_BUNDLE = ITEMS.register("orange_bundle", () -> new BundleItem(bundleProperties()));

    // 粉红色收纳袋变种。
    public static final DeferredItem<BundleItem> PINK_BUNDLE = ITEMS.register("pink_bundle", () -> new BundleItem(bundleProperties()));

    // 紫色收纳袋变种。
    public static final DeferredItem<BundleItem> PURPLE_BUNDLE = ITEMS.register("purple_bundle", () -> new BundleItem(bundleProperties()));

    // 红色收纳袋变种。
    public static final DeferredItem<BundleItem> RED_BUNDLE = ITEMS.register("red_bundle", () -> new BundleItem(bundleProperties()));

    // 白色收纳袋变种。
    public static final DeferredItem<BundleItem> WHITE_BUNDLE = ITEMS.register("white_bundle", () -> new BundleItem(bundleProperties()));

    // 黄色收纳袋变种。
    public static final DeferredItem<BundleItem> YELLOW_BUNDLE = ITEMS.register("yellow_bundle", () -> new BundleItem(bundleProperties()));

    // 收纳袋变种与原版普通收纳袋一致的物品属性：最多叠 1 个，
    // 并且自带一个空的包内物品组件（BundleItem 读写包内物品全靠它）。
    private static Item.Properties bundleProperties() {
        return new Item.Properties().stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
    }

    // 为某个旗帜图案物品构造指向原版图案的标签键，
    // 标签路径形如 futuremc:pattern_item/field_masoned，标签内容由数据包文件提供。
    private static TagKey<BannerPattern> patternItemTag(String path) {
        return TagKey.create(
            Registries.BANNER_PATTERN,
            ResourceLocation.fromNamespaceAndPath(FutureMC.NAMESPACE, "pattern_item/" + path)
        );
    }

    // 工具类不允许被实例化。
    private ModItems() {
    }
}
