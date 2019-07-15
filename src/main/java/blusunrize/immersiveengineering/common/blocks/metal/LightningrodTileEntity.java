/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockLightningrod;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LightningrodTileEntity extends MultiblockPartTileEntity<LightningrodTileEntity> implements IIEInternalFluxHandler
{
	public static TileEntityType<LightningrodTileEntity> TYPE;

	FluxStorage energyStorage = new FluxStorage(IEConfig.Machines.lightning_output);

	@Nullable
	private List<BlockPos> fenceNet = null;
	private int height;

	public LightningrodTileEntity()
	{
		super(MultiblockLightningrod.instance, TYPE, false);
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(!world.isRemote&&formed&&posInMultiblock==13)
		{
			if(energyStorage.getEnergyStored() > 0)
			{
				TileEntity tileEntity;
				for(Direction f : Direction.BY_HORIZONTAL_INDEX)
				{
					tileEntity = Utils.getExistingTileEntity(world, getPos().offset(f, 2));
					int output = EnergyHelper.insertFlux(tileEntity, f.getOpposite(), energyStorage.getLimitExtract(), true);
					output = energyStorage.extractEnergy(output, false);
					EnergyHelper.insertFlux(tileEntity, f.getOpposite(), output, false);
				}
			}

			if(world.getGameTime()%256==((getPos().getX()^getPos().getZ())&255))
				fenceNet = null;
			if(fenceNet==null)
				fenceNet = this.getFenceNet();
			if(fenceNet!=null&&fenceNet.size() > 0
					&&world.getGameTime()%128==((getPos().getX()^getPos().getZ())&127)
					&&(world.isThundering()||(world.isRaining()&&Utils.RAND.nextInt(10)==0)))
			{
				int i = this.height+this.fenceNet.size();
				if(Utils.RAND.nextInt(4096*world.getHeight()) < i*(getPos().getY()+i))
				{
					this.energyStorage.setEnergy(IEConfig.Machines.lightning_output);
					BlockPos pos = fenceNet.get(Utils.RAND.nextInt(fenceNet.size()));
					LightningBoltEntity entityLightningBolt = new LightningBoltEntity(world, pos.getX(), pos.getY(), pos.getZ(), true);
					world.addWeatherEffect(entityLightningBolt);
					world.spawnEntity(entityLightningBolt);
				}
			}
		}
	}

	@Nullable
	private List<BlockPos> getFenceNet()
	{
		this.height = 0;
		boolean broken = false;
		for(int i = getPos().getY()+2; i < world.getHeight()-1; i++)
		{
			BlockPos pos = new BlockPos(getPos().getX(), i, getPos().getZ());
			if(!broken&&isFence(pos))
				this.height++;
			else if(!world.isAirBlock(pos))
				return null;
			else
			{
				if(!broken)
					broken = true;
			}
		}

		ArrayList<BlockPos> openList = new ArrayList<>();
		ArrayList<BlockPos> closedList = new ArrayList<>();
		openList.add(getPos().add(0, height, 0));
		while(!openList.isEmpty()&&closedList.size() < 256)
		{
			BlockPos next = openList.get(0);
			if(!closedList.contains(next)&&isFence(next))
			{
				closedList.add(next);
				openList.add(next.offset(Direction.WEST));
				openList.add(next.offset(Direction.EAST));
				openList.add(next.offset(Direction.NORTH));
				openList.add(next.offset(Direction.SOUTH));
				openList.add(next.offset(Direction.UP));
			}
			openList.remove(0);
		}
		return closedList;
	}

	private boolean isFence(BlockPos pos)
	{
		return Utils.isBlockAt(world, pos, IEContent.blockSteelFence);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock==22)
			return new float[]{-.125f, 0, -.125f, 1.125f, 1, 1.125f};
		if(posInMultiblock%9==4||(posInMultiblock < 18&&posInMultiblock%9%2==1))
			return new float[]{0, 0, 0, 1, 1, 1};
		if(posInMultiblock < 9)
			return new float[]{0, 0, 0, 1, .5f, 1};
		float xMin = 0;
		float xMax = 1;
		float yMin = 0;
		float yMax = 1;
		float zMin = 0;
		float zMax = 1;
		if(posInMultiblock%9==0||posInMultiblock%9==2||posInMultiblock%9==6||posInMultiblock%9==8)
		{
			if(posInMultiblock < 18)
			{
				yMin = -.5f;
				yMax = 1.25f;
				xMin = (facing.getAxis()==Axis.X?(posInMultiblock%9 > 2^facing==Direction.EAST): (posInMultiblock%3==2^facing==Direction.NORTH))?.8125f: .4375f;
				xMax = (facing.getAxis()==Axis.X?(posInMultiblock%9 < 3^facing==Direction.EAST): (posInMultiblock%3==0^facing==Direction.NORTH))?.1875f: .5625f;
				zMin = (facing.getAxis()==Axis.X?(posInMultiblock%3==2^facing==Direction.EAST): (posInMultiblock%9 < 3^facing==Direction.NORTH))?.8125f: .4375f;
				zMax = (facing.getAxis()==Axis.X?(posInMultiblock%3==0^facing==Direction.EAST): (posInMultiblock%9 > 2^facing==Direction.NORTH))?.1875f: .5625f;
			}
			else
			{
				yMin = .25f;
				yMax = .75f;
				xMin = (facing.getAxis()==Axis.X?(posInMultiblock%9 > 2^facing==Direction.EAST): (posInMultiblock%3==2^facing==Direction.NORTH))?1: .625f;
				xMax = (facing.getAxis()==Axis.X?(posInMultiblock%9 < 3^facing==Direction.EAST): (posInMultiblock%3==0^facing==Direction.NORTH))?0: .375f;
				zMin = (facing.getAxis()==Axis.X?(posInMultiblock%3==2^facing==Direction.EAST): (posInMultiblock%9 < 3^facing==Direction.NORTH))?1: .625f;
				zMax = (facing.getAxis()==Axis.X?(posInMultiblock%3==0^facing==Direction.EAST): (posInMultiblock%9 > 2^facing==Direction.NORTH))?0: .375f;
			}
		}
		else if(posInMultiblock > 17)
		{
			yMin = .25f;
			yMax = .75f;
			xMin = offset[0] < 0?.375f: 0;
			xMax = offset[0] > 0?.625f: 1;
			zMin = offset[2] < 0?.375f: 0;
			zMax = offset[2] > 0?.625f: 1;
		}
		return new float[]{xMin, yMin, zMin, xMax, yMax, zMax};
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(posInMultiblock==4)
				renderAABB = new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 5, 2));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		LightningrodTileEntity master = this.master();
		if(master!=null)
			return master.energyStorage;
		return energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return this.formed&&this.isEnergyPos()?SideConfig.OUTPUT: SideConfig.NONE;
	}

	private IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(this.formed&&this.isEnergyPos())
			return wrapper;
		return null;
	}

	private boolean isEnergyPos()
	{
		return posInMultiblock==10||posInMultiblock==12||posInMultiblock==14||posInMultiblock==16;
	}
}