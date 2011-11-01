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
package net.sf.l2j.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class QuestTimer
{
    // =========================================================
    // Schedule Task
    public class ScheduleTimerTask implements Runnable
    {
        public void run()
        {
            if (this == null || !getIsActive()) return;

            try
            {
            	getQuest().notifyEvent(getName(), getNpc(), getPlayer());
                cancel();
            }
            catch (Throwable t)
            {
            }
        }
    }

    // =========================================================
    // Data Field
    private boolean _isActive = true;
    private String _name;
    private Quest _quest;
    private L2NpcInstance _npc;
    private L2PcInstance _player;
    private ScheduledFuture<?> _schedular;

    // =========================================================
    // Constructor
    public QuestTimer(Quest quest, String name, long time, L2NpcInstance npc, L2PcInstance player)
    {
        _name = name;
        _quest = quest;
        _player = player;
        _npc = npc;
        _schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
    }

    public QuestTimer(QuestState qs, String name, long time)
    {
        _name = name;
        _quest = qs.getQuest();
        _player = qs.getPlayer();
        _npc = null;
        _schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
    }

    // =========================================================
    // Method - Public
    public void cancel()
    {
        _isActive = false;

        if (_schedular != null) _schedular.cancel(true);

        getQuest().removeQuestTimer(this);
    }

    // public method to compare if this timer matches with the key attributes passed.
    // a quest and a name are required.
    // null npc or player act as wildcards for the match
    public boolean isMatch(Quest quest, String name, L2NpcInstance npc, L2PcInstance player)
    {
    	if ((quest == null) || (name == null))
    		return false;
    	if ( (quest != getQuest()) || name.compareToIgnoreCase(getName())!=0 )
    		return false;
    	return (( npc==null || getNpc()==null || npc==getNpc() ) && ( player==null || getPlayer()==null || player==getPlayer() ));
    }

    // =========================================================
    // Property - Public
    public final boolean getIsActive()
    {
        return _isActive;
    }

    public final Quest getQuest()
    {
    	return _quest;
    }

    public final String getName()
    {
        return _name;
    }

    public final L2NpcInstance getNpc()
    {
    	return _npc;
    }

    public final L2PcInstance getPlayer()
    {
    	return _player;
    }

    @Override
	public final String toString()
    {
        return _name;
    }
}
