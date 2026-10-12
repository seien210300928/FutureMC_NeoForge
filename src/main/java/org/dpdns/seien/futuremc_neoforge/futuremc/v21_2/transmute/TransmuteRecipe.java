package org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.transmute;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_2.ModRecipes;

import java.util.function.Function;

// 收纳袋染色配方，移植 1.21.2 正式版的 crafting_transmute（物质转化）配方。
// 1.21.2 的收纳袋染色 JSON 形如
//   { "input": "#minecraft:bundles", "material": "minecraft:orange_dye", "result": "minecraft:orange_bundle" }
// 即 "1 个收纳袋（任意颜色）+ 1 种对应颜色的染料 -> 对应颜色的收纳袋变种"，
// 收纳袋之间可以跨颜色互染，包内物品原样保留。
// matches/assemble 语义采用 26.3 重构后的版本：合成格必须恰好有 1 个 input
// （收纳袋标签内的物品）加 materialCount 个 material（对应颜色的染料，默认 1），
// 组件从原收纳袋整体复制，同色互染（同物品同组件）不成立。
// 1.21.1 没有 crafting_transmute 配方类型，这里用 CustomRecipe 实现相同语义，
// 配方类型键是 futuremc:crafting_transmute。
// 配方 JSON 与 1.21.2 相同形状：input/material 支持 1.21.2 起的数据格式
// （裸字符串物品 id 或 "#标签"），同时兼容 1.21.1 的对象/数组形式。
public class TransmuteRecipe extends CustomRecipe {

    // 26.3 重构后最多允许 8 个材料，这里一并保留这个上限。
    private static final int MAX_MATERIAL_COUNT = 8;

    // 配方 JSON 的 input 字段：收纳袋类物品（通常为 #futuremc:bundles）。
    private final Ingredient input;
    // 配方 JSON 的 material 字段：对应颜色的染料（例如 minecraft:orange_dye）。
    private final Ingredient material;
    // 所需染料数量，26.3 重构后支持 1-8；收纳袋染色固定为 1。
    private final int materialCount;
    // 配方 JSON 的 result 字段：目标颜色收纳袋物品（例如 futuremc:orange_bundle）。
    private final Item result;

    // Ingredient 的兼容编解码器：1.21.2 起配方 JSON 允许直接写裸字符串物品 id
    // 或 "#标签"（26.3 重构后 Ingredient 底层是 HolderSet，同样接受这种写法），
    // 而 1.21.1 的 Ingredient.CODEC 只接受对象/数组形式，这里先按 1.21.1 解析，
    // 失败再按裸字符串解析，让配方 JSON 可以照抄 1.21.2 的形状。
    public static final Codec<Ingredient> FLEXIBLE_INGREDIENT = Codec.either(Ingredient.CODEC, Codec.STRING)
        .xmap(
            either -> either.map(Function.identity(), TransmuteRecipe::fromBareString),
            Either::left
        );

    // 把裸字符串解析成 Ingredient："#标签" 按物品标签解析，否则按物品 id 解析。
    private static Ingredient fromBareString(String value) {
        if (value.startsWith("#")) {
            return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(value.substring(1))));
        }
        ResourceLocation id = ResourceLocation.parse(value);
        Item item = BuiltInRegistries.ITEM.getOptional(id)
            .orElseThrow(() -> new IllegalArgumentException("Unknown item in transmute recipe: " + value));
        return Ingredient.of(item);
    }

    // 构造函数接收配方在 JSON 里的分类、收纳袋输入、染料材料、染料数量与目标收纳袋物品。
    public TransmuteRecipe(CraftingBookCategory category, Ingredient input, Ingredient material, int materialCount, Item result) {
        super(category);
        this.input = input;
        this.material = material;
        this.materialCount = materialCount;
        this.result = result;
    }

    // 返回配方 JSON 里的收纳袋输入，供序列化器编解码使用。
    public Ingredient input() {
        return this.input;
    }

    // 返回配方 JSON 里指定的染料材料，供序列化器编解码使用。
    public Ingredient material() {
        return this.material;
    }

    // 返回配方 JSON 里指定的染料数量，供序列化器编解码使用。
    public int materialCount() {
        return this.materialCount;
    }

    // 返回配方 JSON 里指定的目标收纳袋物品，供序列化器编解码使用。
    public Item result() {
        return this.result;
    }

    // 合成格必须至少能放下收纳袋加所需数量的染料。
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.materialCount + 1;
    }

    // 判断合成格里的材料是否符合 "1 个收纳袋 + 本配方对应的那一种染料" 的组合。
    @Override
    public boolean matches(CraftingInput input, Level level) {
        // 26.3 重构后的匹配语义：非空格子数必须恰好是 materialCount + 1。
        if (input.ingredientCount() < this.materialCount + 1 || input.ingredientCount() > this.materialCount + 1) {
            return false;
        }
        // 记录是否已经找到了收纳袋。
        ItemStack foundInput = ItemStack.EMPTY;
        // 统计已经找到的本配方染料数量。
        int materialCount = 0;
        // 逐个检查合成格里的物品。
        for (ItemStack stack : input.items()) {
            // 空格位直接跳过。
            if (stack.isEmpty()) {
                continue;
            }
            // 收纳袋必须匹配 input（futuremc:bundles 标签里的物品），且只能出现一个。
            if (this.input.test(stack)) {
                if (!foundInput.isEmpty()) {
                    return false;
                }
                foundInput = stack;
            } else {
                // 其余非空物品必须都是本配方指定的染料，且数量不能超过上限。
                if (!this.material.test(stack)) {
                    return false;
                }
                if (++materialCount > MAX_MATERIAL_COUNT) {
                    return false;
                }
            }
        }
        // 必须恰好凑齐 1 个收纳袋和 materialCount 个染料。
        if (foundInput.isEmpty() || materialCount != this.materialCount) {
            return false;
        }
        // 与 26.3 重构后的 TransmuteRecipe 相同：如果目标颜色就是当前颜色
        // （同物品、同组件），配方不成立，避免无意义的同色染色。
        ItemStack result = new ItemStack(this.result);
        result.applyComponents(foundInput.getComponentsPatch());
        return !ItemStack.isSameItemSameComponents(foundInput, result);
    }

    // 执行合成：得到目标颜色的收纳袋，原收纳袋里的所有组件（包内物品等）全部保留。
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        // 从合成格里找到收纳袋。
        ItemStack foundInput = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty() && this.input.test(stack)) {
                foundInput = stack;
                break;
            }
        }
        // 材料不齐时返回空物品，正常流程下不会走到这里。
        if (foundInput.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // 创建目标颜色的收纳袋物品，并把原收纳袋的组件整体复制过去。
        ItemStack result = new ItemStack(this.result);
        result.applyComponents(foundInput.getComponentsPatch());
        return result;
    }

    // 返回这个配方使用的序列化器，也就是 futuremc:crafting_transmute。
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRAFTING_TRANSMUTE.get();
    }
}
