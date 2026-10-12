package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.transmute.TransmuteRecipe;
import org.dpdns.seien.futuremc_neoforge.futuremc.FutureMC;

// 自定义合成配方序列化器的注册类。
// 收纳袋染色移植自 1.21.2 正式版的 crafting_transmute（物质转化）配方，
// 配方类型键是 futuremc:crafting_transmute，每种颜色一个数据包文件
// （data/futuremc/recipe/<颜色>_bundle.json）。
// 配方 JSON 与 1.21.2 原版相同形状：
//   "input": "#futuremc:bundles"            收纳袋类物品（裸字符串标签）
//   "material": "minecraft:orange_dye"      对应颜色的染料（裸字符串物品 id）
//   "material_count": 1                     所需染料数量（可选，默认 1）
//   "result": "futuremc:orange_bundle"      目标颜色收纳袋
public final class ModRecipes {

    // 配方序列化器的延迟注册表，所有序列化器都注册在 futuremc 命名空间下。
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
        Registries.RECIPE_SERIALIZER,
        FutureMC.NAMESPACE
    );

    // 收纳袋染色配方的序列化器（移植 1.21.2 crafting_transmute，采用 26.3
    // 重构后的匹配语义）。读取时绑定到 TransmuteRecipe 上。
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TransmuteRecipe>> CRAFTING_TRANSMUTE = RECIPE_SERIALIZERS.register(
        "crafting_transmute",
        () -> new RecipeSerializer<>() {
            // 配方 JSON 的读取方式：分类 + 收纳袋输入 + 染料材料 + 染料数量 + 目标收纳袋物品。
            @Override
            public MapCodec<TransmuteRecipe> codec() {
                return RecordCodecBuilder.mapCodec(builder -> builder.group(
                    CraftingBookCategory.CODEC.fieldOf("category").forGetter(TransmuteRecipe::category),
                    TransmuteRecipe.FLEXIBLE_INGREDIENT.fieldOf("input").forGetter(TransmuteRecipe::input),
                    TransmuteRecipe.FLEXIBLE_INGREDIENT.fieldOf("material").forGetter(TransmuteRecipe::material),
                    com.mojang.serialization.Codec.INT.optionalFieldOf("material_count", 1).forGetter(TransmuteRecipe::materialCount),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(TransmuteRecipe::result)
                ).apply(builder, TransmuteRecipe::new));
            }

            // 配方同步到客户端时按字段顺序传输，物品用注册表编号。
            @Override
            public StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> streamCodec() {
                return StreamCodec.composite(
                    CraftingBookCategory.STREAM_CODEC, TransmuteRecipe::category,
                    Ingredient.CONTENTS_STREAM_CODEC, TransmuteRecipe::input,
                    Ingredient.CONTENTS_STREAM_CODEC, TransmuteRecipe::material,
                    ByteBufCodecs.VAR_INT, TransmuteRecipe::materialCount,
                    ByteBufCodecs.registry(Registries.ITEM), TransmuteRecipe::result,
                    TransmuteRecipe::new
                );
            }
        }
    );

    // 工具类不允许被实例化。
    private ModRecipes() {
    }
}
