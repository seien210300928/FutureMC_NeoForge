package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.pale_oak;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.ModItems;

/**
 * 苍白橡木箱子船实体。
 * <p>
 * 继承原版 ChestBoat（带储物箱），只替换掉落物为苍白橡木箱子船物品。
 */
public class PaleOakChestBoat extends ChestBoat {

    public PaleOakChestBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    /** 带坐标的构造：用于船物品在命中点生成实体。 */
    public PaleOakChestBoat(EntityType<? extends Boat> type, Level level, double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    /** 破坏船时掉落苍白橡木箱子船。 */
    @Override
    public Item getDropItem() {
        return ModItems.PALE_OAK_CHEST_BOAT.get();
    }
}
