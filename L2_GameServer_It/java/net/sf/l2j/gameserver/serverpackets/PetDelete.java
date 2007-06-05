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
 */
public class PetDelete extends L2GameServerPacket
{
	private static final String _S__CF_PETDELETE = "[S] b6 PetDelete";
	private int _petId;
	private int _petnum;
	
	public PetDelete(int petId, int petnum)
	{
		_petId = petId;		// summonType?
		_petnum= petnum;	//objectId
	}
	
	protected final void writeImpl()
	{
		writeC(0xb6);
		writeD(_petId);// dont really know what these two are since i never needed them
		writeD(_petnum);//objectId
	}
	
	public String getType()
	{
		return _S__CF_PETDELETE;
	}
}
