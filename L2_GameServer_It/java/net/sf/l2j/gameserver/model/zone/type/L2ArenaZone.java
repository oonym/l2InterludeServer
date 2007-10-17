/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * An arena
 *
 * @author  durgus
 */
public class L2ArenaZone extends L2ZoneType
{
	private String _arenaName;
	private int[] _spawnLoc;
	
	public L2ArenaZone()
	{
		super();
		
		_spawnLoc = new int[3];
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_arenaName = value;
		}
		else if (name.equals("spawnX"))
		{
			_spawnLoc[0] = Integer.parseInt(value);
		}
		else if (name.equals("spawnY"))
		{
			_spawnLoc[1] = Integer.parseInt(value);
		}
		else if (name.equals("spawnZ"))
		{
			_spawnLoc[2] = Integer.parseInt(value);
		}
		else super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, true);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, false);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}
	}
	
	public final int[] getSpawnLoc()
    {
    	return _spawnLoc;
    }
}
