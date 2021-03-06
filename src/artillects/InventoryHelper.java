package artillects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * @author Calclavia
 * 
 */
public class InventoryHelper
{
	public static boolean decreaseStackSize(IInventory inventory, ItemStack itemStack)
	{
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			if (inventory.getStackInSlot(i) != null)
			{
				if (itemStack.isItemEqual(inventory.getStackInSlot(i)))
				{
					inventory.decrStackSize(i, itemStack.stackSize);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * True when we have ALL of the comparing items in the inventory.
	 * 
	 * @param inventory
	 * @param itemStacks - The comparing items
	 * @return True if so.
	 */
	public static boolean hasItems(IInventory inventory, ItemStack... itemStacks)
	{
		loop:
		for (ItemStack itemStack : itemStacks)
		{
			int itemCount = 0;

			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack stackInSlot = inventory.getStackInSlot(i);

				if (stackInSlot != null && itemStack.isItemEqual(stackInSlot))
				{
					itemCount += stackInSlot.stackSize;
				}
			}

			if (itemCount < itemStack.stackSize)
			{
				return false;
			}
		}

		return true;
	}

	public static boolean hasItem(IInventory inventory, ItemStack... itemStacks)
	{
		return getFirstItemInInventory(inventory, itemStacks) != -1;
	}

	public static int getFirstItemInInventory(IInventory inventory, ItemStack... itemStacks)
	{
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inventory.getStackInSlot(i);

			for (ItemStack itemStack : itemStacks)
			{
				if (stackInSlot != null && itemStack.isItemEqual(stackInSlot))
				{
					return i;
				}
			}
		}

		return -1;
	}

	public static List<ItemStack> getInventoryAsList(IInventory inventory)
	{
		ArrayList<ItemStack> inventoryList = new ArrayList<ItemStack>();

		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			inventoryList.add(inventory.getStackInSlot(i));
		}

		return inventoryList;
	}

	/**
	 * Adds stack to inventory
	 * 
	 * @param stack - The stack to add
	 * @return - The remaining stack.
	 */
	public static ItemStack addStackToInventory(IInventory inventory, ItemStack stack)
	{
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			if (stack != null)
			{
				if (stack.stackSize <= 0)
				{
					return null;
				}

				ItemStack itemStack = inventory.getStackInSlot(i);

				if (itemStack == null)
				{
					inventory.setInventorySlotContents(i, stack);
					stack = null;
				}
				else if (itemStack.isItemEqual(stack))
				{
					int originalStackSize = itemStack.stackSize;
					itemStack.stackSize = Math.min(itemStack.stackSize + stack.stackSize, itemStack.getMaxStackSize());
					stack.stackSize -= itemStack.stackSize - originalStackSize;
				}
			}
			else
			{
				return null;
			}
		}

		return stack;
	}

	public static boolean isInventoryFull(IInventory inventory)
	{
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack itemStack = inventory.getStackInSlot(i);

			if (itemStack != null)
			{
				if (itemStack.stackSize < 64)
				{
					return false;
				}

				continue;
			}

			return false;
		}

		return true;
	}

	public static boolean isInventoryEmpty(IInventory inventory)
	{
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack itemStack = inventory.getStackInSlot(i);

			if (itemStack != null)
			{
				return false;
			}
		}

		return true;
	}

	public static boolean listContainsStack(Set<ItemStack> compareStacks, ItemStack stack)
	{
		return InventoryHelper.getListContainsStack(compareStacks, stack) != null;
	}

	public static ItemStack getListContainsStack(Set<ItemStack> compareStacks, ItemStack stack)
	{
		for (ItemStack checkStack : compareStacks)
		{
			if (checkStack.isItemEqual(stack))
			{
				return stack;
			}
		}
		return null;
	}

	public static boolean listContainsStack(Set<ItemStack> compareStacks, List<ItemStack> checkStacks)
	{
		return InventoryHelper.getListContainsStack(compareStacks, checkStacks) != null;
	}

	public static ItemStack getListContainsStack(Set<ItemStack> compareStacks, List<ItemStack> checkStacks)
	{
		for (ItemStack compareStack : compareStacks)
		{
			for (ItemStack check : checkStacks)
			{
				if (check != null && check.isItemEqual(compareStack))
				{
					return check;
				}
			}
		}
		return null;
	}

}
