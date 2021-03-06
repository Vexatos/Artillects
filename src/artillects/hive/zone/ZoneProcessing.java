package artillects.hive.zone;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFurnace;
import artillects.Vector3;
import artillects.VectorWorld;
import artillects.entity.EntityWorker;
import artillects.entity.IArtillect;
import artillects.hive.HiveComplex;

public class ZoneProcessing extends Zone
{
    public final List<Vector3> chestPositions = new ArrayList<Vector3>();
    public final List<Vector3> furnacePositions = new ArrayList<Vector3>();

    public ZoneProcessing(HiveComplex complex, VectorWorld start, VectorWorld end)
    {
        super(complex, start, end);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (this.ticks % 10 == 0)
        {
            this.scan();
        }
    }

    public void scan()
    {
        Vector3 start = this.start;
        Vector3 end = this.end;
        this.chestPositions.clear();
        // TODO get chunks within zones. Then get tile Entities in those chunks. Then get valid
        // tiles that we can use to reduce on scanning load
        for (int x = (int) start.x; x < (int) end.x; x++)
        {
            for (int y = (int) start.y; y < (int) end.y; y++)
            {
                for (int z = (int) start.z; z < (int) end.z; z++)
                {
                    int blockID = this.start.world.getBlockId(x, y, z);
                    Block block = Block.blocksList[blockID];

                    if (block != null)
                    {
                        Vector3 position = new Vector3(x, y, z);

                        if (block instanceof BlockChest)
                        {
                            this.chestPositions.add(position);
                        }
                        else if (block instanceof BlockFurnace)
                        {
                            this.furnacePositions.add(position);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canAssignDrone(IArtillect drone)
    {
        return drone instanceof EntityWorker;
    }
}
