package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.pale_oak;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.pale_oak.PaleOakBoat;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.pale_oak.PaleOakChestBoat;

/**
 * 苍白橡木船物品。
 * <p>
 * 行为与原版船物品一致：对水面/方块使用后，在玩家视线击中的位置生成
 * 对应的船实体（普通船或箱子船），并扣减物品数量。默认船模型用原版的
 * 橡木船模型/纹理，这里只换掉实体类型与掉落物。
 */
public class PaleOakBoatItem extends Item {
    /** 用于排除旁观者与不可拾取实体的实体过滤器。 */
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    /** 这个物品要生成的船实体类型。 */
    private final EntityType<? extends Boat> boatType;
    /** 是否为箱子船。 */
    private final boolean hasChest;

    public PaleOakBoatItem(EntityType<? extends Boat> boatType, boolean hasChest, Item.Properties properties) {
        super(properties);
        this.boatType = boatType;
        this.hasChest = hasChest;
    }

    /** 玩家手持船使用：命中水面或方块时生成船。 */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        // 没有命中任何东西（视线打空）时什么都不做。
        if (hitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        }

        // 视线方向 5 格内有可拾取的实体挡路时不允许放置船，避免船直接叠在生物身上。
        Vec3 viewVector = player.getViewVector(1.0F);
        List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(viewVector.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
        if (!list.isEmpty()) {
            Vec3 eyePosition = player.getEyePosition();
            for (Entity entity : list) {
                AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (aabb.contains(eyePosition)) {
                    return InteractionResultHolder.pass(itemstack);
                }
            }
        }

        if (hitresult.getType() == HitResult.Type.BLOCK) {
            // 生成船实体并让它面朝玩家。
            Boat boat = this.getBoat(level, hitresult, itemstack, player);
            boat.setYRot(player.getYRot());
            // 目标位置被占（船体碰撞箱与方块/实体重叠）时放置失败。
            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResultHolder.fail(itemstack);
            }

            if (!level.isClientSide) {
                // 服务端真正生成实体，消耗物品并触发实体放置游戏事件。
                level.addFreshEntity(boat);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, hitresult.getLocation());
                itemstack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }
        return InteractionResultHolder.pass(itemstack);
    }

    /** 在命中点构造船实体，并应用物品栈上的自定义 NBT 配置。 */
    private Boat getBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        Vec3 vec3 = hitResult.getLocation();
        Boat boat = this.hasChest
            ? new PaleOakChestBoat(this.boatType, level, vec3.x, vec3.y, vec3.z)
            : new PaleOakBoat(this.boatType, level, vec3.x, vec3.y, vec3.z);
        if (level instanceof ServerLevel serverlevel) {
            // 原版船物品会在这里套用物品 NBT（如自定名称）到实体。
            EntityType.createDefaultStackConfig(serverlevel, stack, player).accept(boat);
        }
        return boat;
    }
}
