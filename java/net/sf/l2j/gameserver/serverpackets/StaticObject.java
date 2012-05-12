/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObject extends L2GameServerPacket
{
	private static final String _S__99_StaticObjectPacket = "[S] 99 StaticObjectPacket";
	private final L2StaticObjectInstance _staticObject;
	
	/**
	 * [S]0x99 StaticObjectPacket dd
	 * @param StaticObject
	 */
	public StaticObject(L2StaticObjectInstance StaticObject)
	{
		_staticObject = StaticObject; // staticObjectId
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x99);
		writeD(_staticObject.getStaticObjectId()); // staticObjectId
		writeD(_staticObject.getObjectId()); // objectId
	}
	
	@Override
	public String getType()
	{
		return _S__99_StaticObjectPacket;
	}
}
