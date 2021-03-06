package artillects.entity.ai;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import artillects.InventoryHelper;
import artillects.Vector3;
import artillects.entity.EntityArtillectBase;
import artillects.entity.EntityWorker;
import artillects.hive.ArtillectType;
import artillects.hive.zone.ZoneProcessing;

public class EntityAIBlacksmith extends EntityArtillectAIBase
{
	private EntityWorker entity;

	private int idleTime = 0;
	private final int maxIdleTime = 20;

	private final HashSet<ItemStack> stacksToSmelt = new HashSet<ItemStack>();
	private final HashSet<ItemStack> stacksForFuel = new HashSet<ItemStack>();
	private final HashSet<ItemStack> stacksToReturn = new HashSet<ItemStack>();

	public EntityAIBlacksmith(EntityWorker entity, double par2)
	{
		super(entity.worldObj, par2);
		this.entity = entity;
		this.setMutexBits(4);

		stacksForFuel.add(new ItemStack(Item.coal));

		stacksToSmelt.add(new ItemStack(Block.oreIron));
		stacksToSmelt.add(new ItemStack(Block.oreGold));

		stacksToReturn.add(new ItemStack(Item.ingotIron));
		stacksToReturn.add(new ItemStack(Item.ingotGold));
	}

	@Override
	public void startExecuting()
	{

	}

	/** Returns whether the EntityAIBase should begin execution. */
	@Override
	public boolean shouldExecute()
	{
		return this.entity.getType() == ArtillectType.BLACKSMITH && entity.getZone() instanceof ZoneProcessing;
	}

	/** Returns whether an in-progress EntityAIBase should continue executing */
	@Override
	public boolean continueExecuting()
	{
		return this.shouldExecute();
	}

	/** Resets the task */
	@Override
	public void resetTask()
	{
	}

	/** Updates the task */
	@Override
	public void updateTask()
	{
		if (this.lastUseChest != null)
		{
			this.lastUseChest.closeChest();
			this.lastUseChest = null;
		}

		if (((ZoneProcessing) entity.getZone()).chestPositions.size() > 0 && ((ZoneProcessing) entity.getZone()).furnacePositions.size() > 0)
		{
			this.idleTime--;
			if (this.idleTime <= 0)
			{
				boolean doDump = false;

				if (this.entity.isInventoryEmpty())
				{
					if (!this.takeResources())
					{
						doDump = true;
					}
				}
				else
				{
					doDump = true;
				}

				if (doDump)
				{
					if (!this.dumpToBeProcessed())
					{
						this.dumpInventoryToChest();
					}
				}

				this.idleTime = this.maxIdleTime;
			}
		}
	}

	private boolean dumpToBeProcessed()
	{
		boolean containsInput = InventoryHelper.listContainsStack(this.stacksToSmelt, this.entity.getInventoryAsList());
		boolean containsFuel = InventoryHelper.listContainsStack(this.stacksForFuel, this.entity.getInventoryAsList());

		for (Vector3 furnacePosition : ((ZoneProcessing) entity.getZone()).furnacePositions)
		{
			boolean didDump = false;
			TileEntity tileEntity = this.world.getBlockTileEntity((int) furnacePosition.x, (int) furnacePosition.y, (int) furnacePosition.z);

			if (tileEntity instanceof TileEntityFurnace)
			{
				TileEntityFurnace furnace = ((TileEntityFurnace) tileEntity);

				if (containsInput || containsFuel)
				{
					if (containsInput)
					{
						didDump = this.placeIntoSlot(furnace, furnacePosition, this.stacksToSmelt, 0);
					}

					if (containsFuel)
					{
						didDump = this.placeIntoSlot(furnace, furnacePosition, this.stacksForFuel, 1);
					}
				}

				ItemStack outputSlot = furnace.getStackInSlot(2);

				if (outputSlot != null)
				{
					furnace.setInventorySlotContents(2, this.entity.increaseStackSize(outputSlot));
				}

				return didDump;
			}
		}

		return false;
	}

	private boolean takeResources()
	{
		for (Vector3 chestPosition : ((ZoneProcessing) entity.getZone()).chestPositions)
		{
			TileEntity tileEntity = this.world.getBlockTileEntity((int) chestPosition.x, (int) chestPosition.y, (int) chestPosition.z);

			if (tileEntity instanceof TileEntityChest)
			{
				TileEntityChest chest = ((TileEntityChest) tileEntity);

				for (int i = 0; i < chest.getSizeInventory(); i++)
				{
					ItemStack itemStack = chest.getStackInSlot(i);

					if (itemStack != null)
					{
						if (InventoryHelper.listContainsStack(this.stacksToSmelt, itemStack) || InventoryHelper.listContainsStack(this.stacksForFuel, itemStack))
						{
							if (this.entity.tryToWalkNextTo(chestPosition, this.moveSpeed))
							{
								if (new Vector3(this.entity).distance(chestPosition.clone().add(0.5)) <= EntityArtillectBase.interactionDistance)
								{
									this.entity.getNavigator().clearPathEntity();

									this.entity.increaseStackSize(itemStack.splitStack(1));

									if (itemStack.stackSize <= 0)
									{
										chest.setInventorySlotContents(i, null);
									}

									chest.openChest();
									this.lastUseChest = chest;
								}

								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean placeIntoSlot(IInventory inventory, Vector3 position, Set checkStacks, int slotID)
	{
		ItemStack tentativeStack = inventory.getStackInSlot(slotID);

		if (tentativeStack == null || (InventoryHelper.listContainsStack(checkStacks, tentativeStack) && tentativeStack.stackSize < tentativeStack.getMaxStackSize()))
		{
			if (this.entity.tryToWalkNextTo(position, this.moveSpeed))
			{
				if (new Vector3(this.entity).distance(position.clone().add(0.5)) <= EntityArtillectBase.interactionDistance)
				{
					this.entity.getNavigator().clearPathEntity();
					ItemStack stackToSet = InventoryHelper.getListContainsStack(checkStacks, this.entity.getInventoryAsList());
					this.entity.decreaseStackSize(stackToSet);

					if (tentativeStack != null && tentativeStack.isItemEqual(stackToSet))
					{
						stackToSet.stackSize += tentativeStack.stackSize;
					}

					inventory.setInventorySlotContents(slotID, stackToSet);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public EntityArtillectBase getArtillect()
	{
		return this.entity;
	}
}
