package dark.items;

import dark.DroneTab;
import dark.ModDrones;
import net.minecraft.item.Item;

/** Base item for all items in this mod
 * 
 * @author Dark */
public class ItemBase extends Item
{

    public ItemBase(String name)
    {
        super(ModDrones.CONFIGURATION.getItem(name, ModDrones.nextItemID()).getInt());
        this.setUnlocalizedName("name");
        this.setCreativeTab(DroneTab.instance());
    }

}
