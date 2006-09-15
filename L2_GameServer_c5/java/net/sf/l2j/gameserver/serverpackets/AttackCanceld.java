/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.serverpackets;


/**
 * This class ...
 * @deprecated not available in C2
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
@Deprecated
public class AttackCanceld extends ServerBasePacket {
	private static final String _S__0A_MAGICSKILLCANCELD = "[S] 0a AttackCanceld";
	
	private int _objectId;

	public AttackCanceld (int objectId) {
		_objectId = objectId; 
	}
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x0a);
		writeD(_objectId);
	}
	
	public String getType()
	{
		return _S__0A_MAGICSKILLCANCELD;
	}
}
