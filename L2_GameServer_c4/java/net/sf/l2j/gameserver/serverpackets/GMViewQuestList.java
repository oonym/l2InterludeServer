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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestDropInfo;
import net.sf.l2j.gameserver.model.quest.QuestState;

/**
 * Sh (dd) h (dddd)
 * @author Tempy
 */
public class GMViewQuestList extends ServerBasePacket
{
	private static final String _S__AC_GMVIEWQUESTLIST = "[S] ac GMViewQuestList";
	
    private L2PcInstance _activeChar;

	public GMViewQuestList(L2PcInstance cha)
	{
		_activeChar = cha;
	}	

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x93);
		writeS(_activeChar.getName());
        
        Quest[] questList = _activeChar.getAllActiveQuests();
        
        if (questList.length == 0)
        {
            writeC(0);
            writeH(0);
            writeH(0);
            return;
        }
        
        writeH(questList.length); // quest count
        
        for (Quest q : questList)
        {
            writeD(q.getQuestIntId());
            
            QuestState qs = _activeChar.getQuestState(q.getName());
            
            if (qs == null)
            {
                writeD(0);
                continue;
            }
            
            writeD(qs.getInt("cond"));   // stage of quest progress
        }

        //Prepare info about all quests
        List<QuestDropInfo> questDrops = new FastList<QuestDropInfo>();
        int FullCountDropItems = 0;
        
        for (Quest q : questList)
        {
            QuestDropInfo newQDrop = new QuestDropInfo(_activeChar, q.getName());
            //Calculate full count drop items
            FullCountDropItems += newQDrop.dropList.size();
            
            questDrops.add(newQDrop);
        }
        
        writeH(FullCountDropItems);
        
        for (QuestDropInfo currQDropInfo : questDrops)
        {
            for (QuestDropInfo.DropInfo itemDropInfo : currQDropInfo.dropList)
            {
                writeD(itemDropInfo.dropItemObjID);
                writeD(itemDropInfo.dropItemID);
                writeD(itemDropInfo.dropItemCount);
                writeD(5); // ??                        
            }
        }
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__AC_GMVIEWQUESTLIST;
	}
}
