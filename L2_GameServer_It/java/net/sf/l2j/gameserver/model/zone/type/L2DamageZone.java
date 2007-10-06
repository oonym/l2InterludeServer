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
 * Bighead zones give entering players big heads
 *
 * @author  durgus
 */
public class L2DamageZone extends L2ZoneType
{
	private int _damagePerSec;
	
	public L2DamageZone()
	{
		super();
		
		// Setup default damage
		_damagePerSec = 100;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("dmgSec"))
		{
			_damagePerSec = Integer.parseInt(value);
		}
		else super.setParameter(name, value);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		//TODO have a task that runs over all character in this zone & deactivates if everyone is gone
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).sendMessage("Not done yet! ("+_damagePerSec+"dps)");
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		//TODO
	}
}
