package org.dpdns.seien.futuremc_neoforge;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.CreativeModeTabs;
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
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModRecipes;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.entity.FutureMCEntityData;

// 模组入口类。这里的模组 ID 必须与 META-INF/neoforge.mods.toml 中的条目保持一致，
// FML 会按照注解里的 ID 找到这个类并加载它。
@Mod(Futuremc_neoforge.MODID)
public class Futuremc_neoforge {

    // 模组 ID，在整个项目里统一使用，所有注册表都挂在它下面。
    public static final String MODID = "futuremc_neoforge";

    // 在模组类被加载时就提前准备实体数据访问器。
    // 幼年标志的访问器是 FutureMCAgeableMixin 合并进 LivingEntity 的静态字段，
    // 由 JVM 保证在父类初始化阶段完成分配，不需要在这里处理。
    // 这里只负责让鲑鱼大小变种的访问器尽早初始化，避免运行时懒加载带来的
    // 数据槽编号不确定性。
    static {
        FutureMCEntityData.ensureEarly();
    }

    // MDK 脚手架自带的示例内容（示例方块、示例物品、示例创造标签页）已经整体注释掉，
    // 项目里不再需要这些示例。示例内容原本包括示例方块注册表 BLOCKS、
    // 示例物品注册表 ITEMS、示例标签页注册表 CREATIVE_MODE_TABS，
    // 以及 example_block、example_block_item、example_item、example_tab 这几项。
    // 以后想恢复它们，把这段注释打开，并把构造方法里对应的注册调用一并打开即可。

    // 构造方法在模组加载时最先执行，FML 会自动把事件总线和容器等参数注入进来。
    public Futuremc_neoforge(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 commonSetup 方法，模组加载阶段会调用它做一次性的通用初始化。
        modEventBus.addListener(this::commonSetup);

        // MDK 脚手架的示例注册表注册（与上面被注释的示例内容配套，同样已注释）。
        // BLOCKS.register(modEventBus);
        // ITEMS.register(modEventBus);
        // CREATIVE_MODE_TABS.register(modEventBus);

        // 注册 FutureMC 移植内容用到的三个注册表。
        // 刷怪笼的五个独立音效事件，分别是破坏、脚步、放置、敲击和摔落。
        org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModSoundEvents.SOUND_EVENTS.register(modEventBus);
        // 两个新旗帜图案物品，分别是棱纹旗帜图案和锯齿边旗帜图案。
        org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.ITEMS.register(modEventBus);
        // 收纳袋染色这个特殊合成配方所使用的序列化器。
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        // 把本类注册到 NeoForge 的游戏事件总线上，这样下面的 onServerStarting 才会被调用。
        NeoForge.EVENT_BUS.register(this);

        // 注册 addCreative 方法，创造模式标签页构建时会回调它，把新内容放进对应标签页。
        modEventBus.addListener(this::addCreative);

        // 注册本模组的配置规范，FML 会据此创建并加载配置文件（对应 Config.java）。
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 注册版本隔离的移植内容子包：1.21.2 的数据/资产分别放在
        // jar 的 datapacks/21_2 与 resourcepacks/21_2 里，由这里在数据包/资源包
        // 仓库创建时挂进去，alwaysActive 为 true 表示默认启用。
        modEventBus.addListener((AddPackFindersEvent event) -> {
            if (event.getPackType() == PackType.SERVER_DATA) {
                // 1.21.2 子数据包：收纳袋/旗帜图案配方、刷怪笼与鲑鱼相关的
                // damage_type、配方序列化器数据与标签（原主包 data 整体迁入）。
                event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(MODID, "datapacks/21_2"),
                    PackType.SERVER_DATA,
                    Component.literal("FutureMC 1.21.2 内容"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP
                );
            }
            // 客户端的版本隔离资源子包同理：1.21.2 的语言文件与刷怪笼音效放在
            // resourcepacks/21_2，在资源包仓库创建时挂进去。
            if (event.getPackType() == PackType.CLIENT_RESOURCES) {
                event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(MODID, "resourcepacks/21_2"),
                    PackType.CLIENT_RESOURCES,
                    Component.literal("FutureMC 1.21.2 资产"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP
                );
            }
        });
    }

    // 模组加载阶段的通用初始化。示例日志已经注释掉，当前没有需要在这里执行的内容。
    private void commonSetup(final FMLCommonSetupEvent event) {
        // MDK 脚手架示例日志（已注释）。
        // LOGGER.info("HELLO FROM COMMON SETUP");
        // if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        // LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        // Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // 把 1.21.2 移植的内容放进对应的创造模式标签页。
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // MDK 脚手架示例：把示例方块物品放进建筑方块标签页（已注释）。
        // if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
        // 收纳袋在 1.21.2 正式脱离实验玩法，而 1.21.1 的创造栏还受实验性开关门控，
        // 所以在这里直接把它加进工具与实用物品标签页，让玩家可以正常拿到。
        // 整个收纳袋体系（普通 + 16 色变种）都使用 futuremc 命名空间，不占用原版
        // minecraft:bundle；1.21.2 的 16 种染色收纳袋变种也放在同一页，排在一起。
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.BLACK_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.BLUE_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.BROWN_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.CYAN_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.GRAY_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.GREEN_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.LIGHT_BLUE_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.LIGHT_GRAY_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.LIME_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.MAGENTA_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.ORANGE_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.PINK_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.PURPLE_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.RED_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.WHITE_BUNDLE);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.YELLOW_BUNDLE);
        }
        // 棱纹旗帜图案和锯齿边旗帜图案都是材料类物品，放进材料标签页，
        // 与原版的其他旗帜图案物品放在同一页。
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.FIELD_MASONED_BANNER_PATTERN);
            event.accept(org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModItems.BORDURE_INDENTED_BANNER_PATTERN);
        }
    }

    // 服务器启动时会回调这个方法。示例日志已经注释掉，当前没有需要执行的内容。
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // MDK 脚手架示例日志（已注释）。
        // LOGGER.info("HELLO from server starting");
    }

    // 客户端专用的模组事件订阅类，里面所有带 SubscribeEvent 注解的静态方法都会被自动注册。
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        // 客户端设置阶段会回调这个方法。示例日志已经注释掉，当前没有需要执行的内容。
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // MDK 脚手架示例日志（已注释）。
            // LOGGER.info("HELLO FROM CLIENT SETUP");
            // LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
