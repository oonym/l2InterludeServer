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

import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * ShortCutInit
 * format   d *(1dddd)/(2ddddd)/(3dddd)
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit extends ServerBasePacket
{
	private static final String _S__57_SHORTCUTINIT = "[S] 45 ShortCutInit";
    
	private L2ShortCut[] _shortCuts;
    private L2PcInstance _activeChar;

	public ShortCutInit(L2PcInstance activeChar)
	{
        _activeChar = activeChar;
	}	
	
	final void runImpl()
	{
		if (_activeChar == null)
			return;
        
		_shortCuts = _activeChar.getAllShortCuts();
	}
	
	final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);

		for (int i = 0; i < _shortCuts.length; i++)
		{
		    L2ShortCut sc = _shortCuts[i];
            writeD(sc.getType());
            writeD(sc.getSlot() + sc.getPage() * 12);
            
            switch(sc.getType())
            {
            case L2ShortCut.TYPE_ITEM: //1
            	writeD(sc.getId());
            	break;
            case L2ShortCut.TYPE_SKILL: //2
            	writeD(sc.getId());
            	writeD(sc.getLevel());
            	writeC(0x01); // ?? c5 
            	break;
            case L2ShortCut.TYPE_ACTION: //3
            	writeD(sc.getId());
            	break;
            case L2ShortCut.TYPE_MACRO: //4
            	writeD(sc.getId());
            	break;
            case L2ShortCut.TYPE_RECIPE: //5
            	writeD(sc.getId());
            	break;
            default:
            	writeD(sc.getId());
            }
            
		    writeD(1); //??
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__57_SHORTCUTINIT;
	}
}
