package artillects.hive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import artillects.Vector3;

public class ZoneMining extends Zone
{
	public final HashMap<Block, HashSet<Vector3>> scannedSortedPositions = new HashMap<Block, HashSet<Vector3>>();

	// List of blocks sorted based on distance. Closer the block, the lower the get-ID.
	public final List<Vector3> scannedBlocks = new ArrayList<Vector3>();

	public ZoneMining(World world, Vector3 start, Vector3 end)
	{
		super(world, start, end);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (ticks % 10 == 0)
		{
			this.scan();
		}
	}

	public void scan()
	{
		Vector3 start = this.start;
		Vector3 end = this.end;
		this.scannedBlocks.clear();
		this.scannedSortedPositions.clear();

		for (int x = (int) start.x; x < (int) end.x; x++)
		{
			for (int y = (int) start.y; y < (int) end.y; y++)
			{
				for (int z = (int) start.z; z < (int) end.z; z++)
				{
					int blockID = this.world.getBlockId(x, y, z);
					Block block = Block.blocksList[blockID];

					if (block != null && this.canMine(block))
					{
						Vector3 position = new Vector3(x, y, z);

						HashSet<Vector3> vecs = this.scannedSortedPositions.get(block);

						if (vecs == null)
						{
							vecs = new HashSet<Vector3>();
						}

						this.scannedBlocks.add(position);
						vecs.add(position);

						this.scannedSortedPositions.put(block, vecs);
					}
				}
			}
		}
	}

	public boolean canMine(Block block)
	{
		return block.blockID == Block.oreIron.blockID;
	}
}
