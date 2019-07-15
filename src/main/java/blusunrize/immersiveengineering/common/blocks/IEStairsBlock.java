/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class IEStairsBlock extends StairsBlock
{
	public boolean hasFlavour = false;
	public boolean isFlammable = false;
	public String name;
	float explosionResistance;
	BlockRenderLayer renderLayer = BlockRenderLayer.SOLID;

	public IEStairsBlock(String name, BlockState state)
	{
		super(state);
		this.name = name;
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.itemGroup);
		this.useNeighborBrightness = true;
		this.explosionResistance = this.blockResistance/5f;
//		ImmersiveEngineering.registerBlock(this, ItemBlockIEStairs.class, name);
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new ItemBlockIEStairs(this));
	}

	public IEStairsBlock setFlammable(boolean b)
	{
		this.isFlammable = b;
		return this;
	}

	public IEStairsBlock setHasFlavour(boolean hasFlavour)
	{
		this.hasFlavour = hasFlavour;
		return this;
	}

	@Override
	public float getExplosionResistance(Entity exploder)
	{
		return explosionResistance;
	}

	public IEStairsBlock setExplosionResistance(float explosionResistance)
	{
		this.explosionResistance = explosionResistance;
		return this;
	}

	@Override
	public boolean doesSideBlockRendering(BlockState state, IBlockAccess world, BlockPos pos, Direction face)
	{
		if(this.renderLayer!=BlockRenderLayer.SOLID)
			return false;
		return super.doesSideBlockRendering(state, world, pos, face);
	}

	public IEStairsBlock setRenderLayer(BlockRenderLayer layer)
	{
		this.renderLayer = layer;
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return renderLayer;
	}
}