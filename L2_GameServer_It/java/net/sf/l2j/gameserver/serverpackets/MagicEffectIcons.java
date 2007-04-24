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
 * 
 * MagicEffectIcons
 * format   h (dhd)
 * 
 * @version $Revision: 1.3.2.1.2.6 $ $Date: 2005/04/05 19:41:08 $
 */
public class MagicEffectIcons extends L2GameServerPacket
{
	private static final String _S__97_MAGICEFFECTICONS = "[S] 7f MagicEffectIcons";
	private List<Effect> _effects;
	
	class Effect
	{
		int _skillId;
		int _level;
		int _duration;
		
		public Effect(int skillId, int level, int duration)
		{
			_skillId = skillId;
			_level = level;
			_duration = duration;	
		}
	}
	
	public MagicEffectIcons()
	{
		_effects = new FastList<Effect>();
	}
	
	public void addEffect(int skillId, int level, int duration)
	{
//		System.out.println("Adding effect icon id="+skillId+", level="+dat+", duration="+duration);
		_effects.add(new Effect(skillId, level, duration));		
	}
	
	protected final void writeImpl()
	{
		writeC(0x7f);
//		System.out.println("Sending "+_effects.size()+" icons");
		
		writeH(_effects.size());
	
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
            
            if (temp._duration == -1)
                writeD(-1);
            else
			    writeD(temp._duration / 1000);
		}		
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__97_MAGICEFFECTICONS;
	}
}
