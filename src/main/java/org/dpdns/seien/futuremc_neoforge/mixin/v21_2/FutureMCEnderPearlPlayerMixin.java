package org.dpdns.seien.futuremc_neoforge.mixin.v21_2;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.FutureMCEnderPearlPlayerAccessor;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.FutureMCTicketTypes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.2 起末影珍珠会强制加载飞行路径上的区块，避免落地时区块未加载。
// 相关状态由服务端玩家持有：一个飞行中的珍珠集合，配合加载票维护和存档。
// 1.21.1 的 ServerPlayer 完全没有这些逻辑，这里整体照搬 1.21.2 原版实现。
@Mixin(ServerPlayer.class)
public abstract class FutureMCEnderPearlPlayerMixin implements FutureMCEnderPearlPlayerAccessor {

    // 玩家飞行中的末影珍珠集合，用于存档与恢复，照搬 1.21.2 原版字段。
    @Unique
    private final Set<ThrownEnderpearl> futuremc$enderPearls = new HashSet<>();

    // 记录错误日志用的日志器，仅在存档恢复异常时使用。
    @Unique
    private static final Logger futuremc$LOGGER = LogUtils.getLogger();

    // 把珍珠登记进飞行珍珠集合。
    @Override
    @Unique
    public void futuremc$registerEnderPearl(ThrownEnderpearl enderPearl) {
        this.futuremc$enderPearls.add(enderPearl);
    }

    // 把珍珠从飞行珍珠集合里移除。
    @Override
    @Unique
    public void futuremc$deregisterEnderPearl(ThrownEnderpearl enderPearl) {
        this.futuremc$enderPearls.remove(enderPearl);
    }

    // 返回当前飞行中的珍珠集合，与 1.21.2 原版方法保持一致，暂未被外部调用。
    @SuppressWarnings("unused")
    @Unique
    public Set<ThrownEnderpearl> futuremc$getEnderPearls() {
        return this.futuremc$enderPearls;
    }

    // 登记珍珠，重置所在世界的空置计时，并为其区块放置加载票。
    // 返回的刻数减一后作为珍珠下一次更新加载票的间隔，照搬 1.21.2 原版。
    @Override
    @Unique
    public long futuremc$registerAndUpdateEnderPearlTicket(ThrownEnderpearl enderPearl) {
        if (enderPearl.level() instanceof ServerLevel serverLevel) {
            ChunkPos chunkPos = enderPearl.chunkPosition();
            this.futuremc$registerEnderPearl(enderPearl);
            serverLevel.resetEmptyTime();
            return futuremc$placeEnderPearlTicket(serverLevel, chunkPos) - 1L;
        }
        return 0L;
    }

    // 以珍珠所在区块为中心、半径二放置加载票，使该范围强制加载并 tick。
    // 票超时四十刻后自动过期，照搬 1.21.2 原版 private placeEnderPearlTicket。
    @Unique
    private static long futuremc$placeEnderPearlTicket(ServerLevel level, ChunkPos chunk) {
        level.getChunkSource().addRegionTicket(FutureMCTicketTypes.ENDER_PEARL, chunk, 2, chunk);
        return FutureMCTicketTypes.ENDER_PEARL.timeout();
    }

    // 玩家存档时把飞行中的末影珍珠写入 NBT，键名与 1.21.2 原版一致。
    @Unique
    private void futuremc$saveEnderPearls(CompoundTag tag) {
        if (!this.futuremc$enderPearls.isEmpty()) {
            ListTag list = new ListTag();
            for (ThrownEnderpearl enderPearl : this.futuremc$enderPearls) {
                if (enderPearl.isRemoved()) {
                    futuremc$LOGGER.warn("Trying to save removed ender pearl, skipping");
                } else {
                    CompoundTag pearlTag = new CompoundTag();
                    enderPearl.save(pearlTag);
                    Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, enderPearl.level().dimension())
                        .resultOrPartial(futuremc$LOGGER::error)
                        .ifPresent(value -> pearlTag.put("ender_pearl_dimension", value));
                    list.add(pearlTag);
                }
            }
            tag.put("ender_pearls", list);
        }
    }

    // 玩家读档时恢复飞行中的末影珍珠，并为它们的区块重新放置加载票。
    @Unique
    public void futuremc$loadAndSpawnEnderpearls(Optional<CompoundTag> optionalTag) {
        if (optionalTag.isPresent() && optionalTag.get().contains("ender_pearls", Tag.TAG_LIST)) {
            ListTag list = optionalTag.get().getList("ender_pearls", Tag.TAG_COMPOUND);
            list.forEach(value -> this.futuremc$loadAndSpawnEnderpearl((CompoundTag) value));
        }
    }

    // 恢复单个末影珍珠：按存档维度找到对应世界，重新生成珍珠并放置加载票。
    @Unique
    private void futuremc$loadAndSpawnEnderpearl(CompoundTag pearlTag) {
        Tag dimensionTag = pearlTag.get("ender_pearl_dimension");
        if (dimensionTag != null) {
            Optional<ResourceKey<Level>> levelKey = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, dimensionTag)
                .resultOrPartial(futuremc$LOGGER::error);
            if (levelKey.isPresent()) {
                ServerLevel serverLevel = ((ServerPlayer) (Object) this).server.getLevel(levelKey.get());
                if (serverLevel != null) {
                    Entity pearl = EntityType.loadEntityRecursive(pearlTag, serverLevel, entity -> !serverLevel.addWithUUID(entity) ? null : entity);
                    if (pearl != null) {
                        futuremc$placeEnderPearlTicket(serverLevel, pearl.chunkPosition());
                    } else {
                        futuremc$LOGGER.warn("Failed to spawn player ender pearl in level ({}), skipping", levelKey.get());
                    }
                } else {
                    futuremc$LOGGER.warn("Trying to load ender pearl without level ({}) being loaded, skipping", levelKey.get());
                }
            }
        }
    }

    // 玩家存档时把珍珠列表写入玩家 NBT，注入点在原版存档方法的末尾。
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void futuremc$onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        this.futuremc$saveEnderPearls(tag);
    }

    // 玩家读档时恢复飞行中的末影珍珠，注入点在原版读档方法的末尾。
    // 形参传入整个玩家 NBT，与原版 1.21.2 的 loadAndSpawnEnderpearls 一致。
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void futuremc$onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        this.futuremc$loadAndSpawnEnderpearls(Optional.of(tag));
    }
}
