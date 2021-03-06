package artillects;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class VectorWorld extends Vector3
{
	public World world;

	public VectorWorld(World world, double x, double y, double z)
	{
		super(x, y, z);
		this.world = world;
	}

	public VectorWorld(NBTTagCompound nbt)
	{
		super(nbt);
		this.world = DimensionManager.getWorld(nbt.getInteger("d"));
	}

	public VectorWorld(Entity entity)
	{
		super(entity);
		this.world = entity.worldObj;
	}

	public VectorWorld(TileEntity tile)
	{
		super(tile);
		this.world = tile.worldObj;
	}

	public VectorWorld(World world, Vector3 v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		this.world = world;
	}

	@Override
	public VectorWorld add(double x, double y, double z)
	{
		super.add(x, y, z);
		return this;
	}

	@Override
	public VectorWorld clone()
	{
		return new VectorWorld(world, x, y, z);
	}

	@Override
	public NBTTagCompound save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setInteger("d", this.world.provider.dimensionId);
		return nbt;
	}
}
