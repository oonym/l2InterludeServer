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

/**
 * A Town zone
 *
 * @author  durgus
 */
public class L2TownZone extends L2ZoneType
{
	private String _townName;
	private int _townId;
	private int _redirectTownId;
	private int _taxById;
	private boolean _noPeace;
	private int[] _spawnLoc;
	
	
	public L2TownZone()
	{
		super();
		
		_taxById = 0;
		_spawnLoc = new int[3];
		
		// Default to Giran
		_redirectTownId = 9;
		
		// Default peace zone
		_noPeace = false;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_townName = value;
		}
		else if (name.equals("townId"))
		{
			_townId = Integer.parseInt(value);
		}
		else if (name.equals("redirectTownId"))
		{
			_redirectTownId = Integer.parseInt(value);
		}
		else if (name.equals("taxById"))
		{
			_taxById = Integer.parseInt(value);
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
		else if (name.equals("noPeace"))
		{
			_noPeace = Boolean.parseBoolean(value);
		}
		else super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (!_noPeace) character.setInsideZone(L2Character.ZONE_PEACE, true);
		
		if (character instanceof L2PcInstance)
		{
			//((L2PcInstance)character).sendMessage("You entered "+_townName);
			
			//TODO: check for town pvp zone during siege (Config.ZONE_TOWN != 0 && getCastle().checkIfInZoneTowns(x, y)
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (!_noPeace) character.setInsideZone(L2Character.ZONE_PEACE, false);
		
		if (character instanceof L2PcInstance)
		{
			//((L2PcInstance)character).sendMessage("You left "+_townName);
			
			// TODO: check for town pvp zone during siege
		}
	}
	
	@Override
	protected void onDieInside(L2Character character) {}
	
	@Override
	protected void onReviveInside(L2Character character) {}

	
	/**
	 * Returns this town zones name
	 * @return
	 */
	@Deprecated
	public String getName()
	{
		return _townName;
	}
	
	/**
	 * Returns this zones town id (if any)
	 * @return
	 */
	public int getTownId()
	{
		return _townId;
	}
	
	/**
	 * Gets the id for this town zones redir town
	 * @return
	 */
	@Deprecated
	public int getRedirectTownId()
	{
		return _redirectTownId;
	}
	
	/**
	 * Returns this zones spawn location
	 * @return
	 */
	public final int[] getSpawnLoc()
    {
    	return _spawnLoc;
    }
	
	/**
	 * Returns this town zones castle id
	 * @return
	 */
	public final int getTaxById()
    {
    	return _taxById;
    }
}
