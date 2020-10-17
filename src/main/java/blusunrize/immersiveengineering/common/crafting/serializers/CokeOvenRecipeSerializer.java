/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.utils.IEPacketBuffer;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CokeOvenRecipeSerializer extends IERecipeSerializer<CokeOvenRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.cokeOven);
	}

	@Override
	public CokeOvenRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		int time = JSONUtils.getInt(json, "time");
		int oil = JSONUtils.getInt(json, "creosote");
		return new CokeOvenRecipe(recipeId, output, input, time, oil);
	}

	@Nullable
	@Override
	public CokeOvenRecipe read(ResourceLocation recipeId, @Nonnull IEPacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int time = buffer.readVarInt();
		int oil = buffer.readVarInt();
		return new CokeOvenRecipe(recipeId, output, input, time, oil);
	}

	@Override
	public void write(@Nonnull IEPacketBuffer buffer, CokeOvenRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		recipe.input.write(buffer);
		buffer.writeVarInt(recipe.time);
		buffer.writeVarInt(recipe.creosoteOutput);
	}
}
