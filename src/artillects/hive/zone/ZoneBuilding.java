package artillects.hive.zone;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import artillects.Pair;
import artillects.Vector3;
import artillects.VectorWorld;
import artillects.entity.EntityFabricator;
import artillects.entity.IArtillect;
import artillects.hive.HiveComplex;
import artillects.hive.structure.Structure;

public class ZoneBuilding extends Zone
{
    public ZoneBuilding(HiveComplex complex, VectorWorld start, VectorWorld end)
    {
        super(complex, start, end);
        // TODO Auto-generated constructor stub
    }

    public HashMap<Vector3, Pair<ItemStack, Structure>> buildPosition = new HashMap<Vector3, Pair<ItemStack, Structure>>();

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (ticks % 20 == 0 && complex != null)
        {
            this.buildPosition.clear();
            for (Structure str : complex.damagedPeaces)
            {
                str.loadBuildingRequest(this.buildPosition);
            }
        }
    }

    public Pair<Vector3, ItemStack> getClosestBlock(VectorWorld vec)
    {
        if (!buildPosition.isEmpty() && vec.world == this.start.world)
        {
            Vector3 location = null;
            ItemStack stack = null;

            for (Entry<Vector3, Pair<ItemStack, Structure>> entry : buildPosition.entrySet())
            {
                if (location == null || entry.getKey().distance(vec) < vec.distance(location))
                {
                    location = entry.getKey();
                    stack = entry.getValue().getLeft();
                }
            }
            return new Pair<Vector3, ItemStack>(location, stack);
        }
        return null;
    }

    /** Called by a builder to place a block.
     * 
     * @param location - location in the zone's world
     * @param stack - item stack, stack size is ignored
     * @return true if the block was placed */
    public boolean placeBlock(Vector3 location, ItemStack stack)
    {
        if (this.buildPosition.get(location) != null)
        {
            if (this.buildPosition.get(location).getRight() != null)
            {
                if (this.buildPosition.get(location).getRight().addBlock(location, stack))
                {
                    this.buildPosition.remove(location);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canAssignDrone(IArtillect drone)
    {
        return drone instanceof EntityFabricator;
    }

    @Override
    public boolean doesZoneNeedWorkers()
    {
        return assignedDrones.size() < 3 && !this.buildPosition.isEmpty();
    }
}
