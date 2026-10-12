package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 查找最近指定方块的调试指令。
 * <p>
 * 独立指令，不侵入原版 {@code /locate}。扫描逻辑仿照
 * {@code ChunkGenerator.findNearestMapStructure}（原版 /locate structure
 * 的底层实现）：以玩家所在区块为中心，按方形环逐层向外扩展，某层一旦
 * 命中就直接返回该层内最近的目标方块——与结构定位的"最近优先"一致，
 * 只是把"结构"换成了"单个方块"。
 * <p>
 * 用法：
 * <ul>
 *   <li>{@code /futuremc locate_block <方块> [半径]}——半径默认 128 格，最大 4096。</li>
 * </ul>
 * 方块参数与原版 /setblock 相同（可用完整方块 ID，属性会被忽略，只按方块本体匹配）。
 */
@EventBusSubscriber(modid = "futuremc_neoforge", bus = EventBusSubscriber.Bus.GAME)
public final class LocateBlockCommand {

    /** 默认搜索半径（格）。 */
    private static final int DEFAULT_RADIUS = 128;
    /** 半径上限（格），避免一次性扫描过大卡住服务端线程。 */
    private static final int MAX_RADIUS = 4096;
    /** 方块参数名。 */
    private static final String ARG_BLOCK = "block";
    /** 半径参数名。 */
    private static final String ARG_RADIUS = "radius";

    /** 方块参数自动补全（给客户端按注册表提示方块 ID）。 */
    private static final SuggestionProvider<CommandSourceStack> BLOCK_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(
                    context.getSource().registryAccess().registryOrThrow(Registries.BLOCK).keySet().stream(),
                    builder,
                    "");

    /** 游戏总线事件：注册 /futuremc 指令树。 */
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("futuremc")
                .then(Commands.literal("locate_block")
                        .then(Commands.argument(ARG_BLOCK, BlockStateArgument.block(event.getBuildContext()))
                                .suggests(BLOCK_SUGGESTIONS)
                                // 不带半径：走默认半径。
                                .executes(context -> run(context, DEFAULT_RADIUS))
                                // 带半径：走用户指定半径。
                                .then(Commands.argument(ARG_RADIUS, IntegerArgumentType.integer(1, MAX_RADIUS))
                                        .executes(context -> run(context, IntegerArgumentType.getInteger(context, ARG_RADIUS)))))));
    }

    /**
     * 指令执行：解析方块参数并做最近方块扫描。
     * <p>
     * 方块匹配忽略属性（state.is(block)），因此 {@code futuremc:creaking_heart}
     * 会命中所有状态变体（axis/awake/natural）。
     */
    private static int run(CommandContext<CommandSourceStack> context, int radius) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockState target = BlockStateArgument.getBlock(context, ARG_BLOCK).getState();
        Block targetBlock = target.getBlock();

        // 中心取指令执行者所在位置（通常就是玩家）。
        BlockPos center = BlockPos.containing(source.getPosition());

        BlockPos found = findNearestBlock(level, targetBlock, center, radius);
        if (found == null) {
            source.sendFailure(Component.literal("半径 " + radius + " 格内未找到 " + describeBlock(targetBlock)));
            return 0;
        }

        double distance = Math.sqrt(center.distSqr(found));
        source.sendSuccess(
                () -> Component.literal("找到最近的 ")
                        .append(Component.literal(describeBlock(targetBlock)).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" 位于 "))
                        .append(Component.literal("[" + found.getX() + ", " + found.getY() + ", " + found.getZ() + "]")
                                .withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("，距离 " + String.format("%.1f", distance) + " 格"))
                        .append(Component.literal("。传送：/tp @s " + found.getX() + " " + found.getY() + " " + found.getZ())
                                .withStyle(ChatFormatting.GRAY)),
                true);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 最近方块扫描。
     * <p>
     * 与 {@code findNearestMapStructure} 相同的环形策略：从中心区块起按
     * 方形环半径逐层（0, 1, 2, …）向外扫，某一层一旦命中就不再扫更远的层，
     * 保证返回的是全局最近的目标。已加载区块才扫描（不触发区块生成）。
     */
    private static BlockPos findNearestBlock(ServerLevel level, Block target, BlockPos center, int radiusBlocks) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        int centerX = SectionPos.blockToSectionCoord(center.getX());
        int centerZ = SectionPos.blockToSectionCoord(center.getZ());
        int radiusChunks = (radiusBlocks + 15) / 16;

        // 逐层外扩，层 k 是到中心区块的 max(|dx|,|dz|) 距离。
        for (int k = 0; k <= radiusChunks; k++) {
            BlockPos layerBest = null;
            double layerBestDist = Double.MAX_VALUE;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            // 遍历第 k 层方形环上的所有区块（只有外边缘，避免重复扫内层）。
            for (int dx = -k; dx <= k; dx++) {
                for (int dz = -k; dz <= k; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != k) {
                        continue;
                    }
                    int chunkX = centerX + dx;
                    int chunkZ = centerZ + dz;
                    if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
                        continue;
                    }
                    LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                    ChunkPos chunkPos = chunk.getPos();
                    int baseX = chunkPos.getMinBlockX();
                    int baseZ = chunkPos.getMinBlockZ();

                    // 逐方块扫描该区块内部（复用 MutableBlockPos，避免频繁分配）。
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = minY; y < maxY; y++) {
                                mutable.set(baseX + x, y, baseZ + z);
                                if (chunk.getBlockState(mutable).is(target)) {
                                    double dist = mutable.distSqr(center);
                                    if (dist < layerBestDist) {
                                        layerBestDist = dist;
                                        layerBest = mutable.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (layerBest != null) {
                return layerBest;
            }
        }
        return null;
    }

    /** 方块的人类可读描述（注册 ID，如 futuremc:creaking_heart）。 */
    private static String describeBlock(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key != null ? key.toString() : block.toString();
    }

    private LocateBlockCommand() {
    }
}
