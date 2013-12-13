package artillects.network;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.entity.player.EntityPlayer;

/** @author Calclavia, darkguardsman */
public interface IPacketReceiver
{
    /** @param id - a simple string based id that was used to encode the packet
     * @param data - data encoded into the packet
     * @param player - player that sent or is receiving the packet */
    public void onReceivePacket(String id, ByteArrayDataInput data, EntityPlayer player);
}