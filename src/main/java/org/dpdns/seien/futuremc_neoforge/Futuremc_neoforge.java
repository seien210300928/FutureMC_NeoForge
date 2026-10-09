package org.dpdns.seien.futuremc_neoforge;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// 此处的值应与 META-INF/neoforge.mods.toml 文件中的条目保持一致
@Mod(Futuremc_neoforge.MODID)
public class Futuremc_neoforge {
    // 在公共位置定义模组 ID（mod id），供所有代码统一引用
    public static final String MODID = "futuremc_neoforge";
    // 直接引用一个 slf4j 日志记录器
    private static final Logger LOGGER = LogUtils.getLogger();
    // 创建延迟注册表（DeferredRegister）以存放方块，所有方块都将注册在 "futuremc_neoforge" 命名空间下
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // 创建延迟注册表（DeferredRegister）以存放物品，所有物品都将注册在 "futuremc_neoforge" 命名空间下
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // 创建延迟注册表（DeferredRegister）以存放创造模式标签页，所有标签页都将注册在 "futuremc_neoforge" 命名空间下
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 创建一个 ID 为 "futuremc_neoforge:example_block" 的新方块，由命名空间与路径组合而成
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // 创建一个 ID 为 "futuremc_neoforge:example_block" 的方块物品（BlockItem），由命名空间与路径组合而成
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // 创建一个 ID 为 "futuremc_neoforge:example_id" 的食物物品，营养值为 1，饱和度修正值为 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // 为示例物品创建一个 ID 为 "futuremc_neoforge:example_tab" 的创造模式标签页，位于战斗标签页之后
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.futuremc_neoforge")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(EXAMPLE_ITEM.get()); // 将示例物品添加到该标签页。对于你自己的标签页，此方法优先于事件
    }).build());

    // 模组类的构造方法是模组被加载时最先运行的代码。
    // FML 会自动识别 IEventBus、ModContainer 等参数类型并自动注入。
    public Futuremc_neoforge(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 commonSetup 方法，用于模组加载
        modEventBus.addListener(this::commonSetup);

        // 将延迟注册表注册到模组事件总线，以便方块完成注册
        BLOCKS.register(modEventBus);
        // 将延迟注册表注册到模组事件总线，以便物品完成注册
        ITEMS.register(modEventBus);
        // 将延迟注册表注册到模组事件总线，以便标签页完成注册
        CREATIVE_MODE_TABS.register(modEventBus);

        // 注册本类以监听我们感兴趣的服务器及其他游戏事件。
        // 注意：仅当我们希望本类（Futuremc_neoforge）直接响应事件时才需要这一行。
        // 如果本类中没有带 @SubscribeEvent 注解的方法（如下面的 onServerStarting()），请不要添加这一行。
        NeoForge.EVENT_BUS.register(this);

        // 将物品注册到创造模式标签页
        modEventBus.addListener(this::addCreative);

        // 注册本模组的配置规范（ModConfigSpec），让 FML 为我们创建并加载配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 一些通用设置代码
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // 将示例方块物品添加到建筑方块标签页
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // 你可以使用 SubscribeEvent，让事件总线自动发现需要调用的方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 在服务器启动时执行某些操作
        LOGGER.info("HELLO from server starting");
    }

    // 你可以使用 EventBusSubscriber 自动注册该类中所有带 @SubscribeEvent 注解的静态方法
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 一些客户端设置代码
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
