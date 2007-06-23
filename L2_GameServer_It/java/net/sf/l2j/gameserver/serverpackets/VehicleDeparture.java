/**
 * 
 */
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author Maktakien
 *
 */
public class VehicleDeparture extends L2GameServerPacket
{

	private L2BoatInstance _boat;
	private int _speed1;
	private int _speed2;//rotation
	private int _x;
	private int _y;
	private int _z;
	/**
	 * @param _boat
	 * @param speed1
	 * @param speed2
	 * @param x
	 * @param y
	 * @param z
	 */
	public VehicleDeparture(L2BoatInstance boat, int speed1, int speed2, int x, int y, int z)
	{
		_boat = boat;
		_speed1 = speed1;
		_speed2 = speed2;
		_x = x;
		_y = y;
		_z = z;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected
	void writeImpl()
	{
		writeC(0x5a);
		writeD(_boat.getObjectId());   
		writeD(_speed1);        
		writeD(_speed2);        
		writeD(_x);
		writeD(_y);    
		writeD(_z);  
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "[S] 5A VehicleDeparture";
	}

}
