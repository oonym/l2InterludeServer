
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;
/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ValidateLocationInVehicle extends ServerBasePacket
{
    private static final String _S__73_ValidateLocationInVehicle = "[S] 73 ValidateLocationInVehicle";
    private L2Character _player;


    /**
     * 0x73 ValidateLocationInVehicle         hdd 
     * @param _characters
     */
    public ValidateLocationInVehicle(L2Character player)
    {
        _player = player;
    }


    final void runImpl()
    {
        // no long-running tasks
    }
    
    final void writeImpl()
    {
        writeC(0x73);
        writeD(_player.getObjectId());
        writeD(1343225858); //TODO verify vehicle object id ??
        writeD(_player.getX());
        writeD(_player.getY());
        writeD(_player.getZ());
        writeD(_player.getHeading());
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__73_ValidateLocationInVehicle;
    }
}
