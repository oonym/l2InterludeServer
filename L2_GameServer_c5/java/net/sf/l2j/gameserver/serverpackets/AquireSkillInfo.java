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

import java.util.List;

import javolution.util.FastList;

/**
 * <code>
 * sample 
 * 
 * a4
 * 4d000000 01000000 98030000 			Attack Aura, level 1, sp cost
 * 01000000 							number of requirements
 * 05000000 47040000 0100000 000000000	   1 x spellbook advanced ATTACK                                                 .
 * </code>
 * 
 * format   ddd d (dddd)
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class AquireSkillInfo extends ServerBasePacket
{
	private static final String _S__A4_AQUIRESKILLINFO = "[S] 8b AquireSkillInfo";
	private List<Req> _reqs;
	private int _id, _level, _spCost, _mode;	
	
	private class Req
	{
		public int _itemId;
		public int _count;
		public int _type;
		public int _unk;
		
		public Req(int type, int itemId, int count, int unk)
		{
			_itemId = itemId;
			_type = type;
			_count = count;
			_unk = unk;
		}
	}

	public AquireSkillInfo(int id, int level, int spCost, int mode)
	{
		_reqs = new FastList<Req>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_mode = mode;
	}	
	
	public void addRequirement(int type, int id, int count, int unk)
	{
		_reqs.add(new Req(type, id, count, unk));
	}
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x8b);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_mode); //c4
		
		writeD(_reqs.size());

		for (Req temp : _reqs)
		{
			writeD(temp._type);
			writeD(temp._itemId);
			writeD(temp._count);
			writeD(temp._unk);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__A4_AQUIRESKILLINFO;
	}
	
}
