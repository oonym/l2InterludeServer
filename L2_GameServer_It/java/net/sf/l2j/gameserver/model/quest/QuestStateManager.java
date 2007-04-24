package net.sf.l2j.gameserver.model.quest;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class QuestStateManager
{
    // =========================================================
    // Schedule Task
    public class ScheduleTimerTask implements Runnable
    {
        public void run()
        {
            try
            {
                cleanUp();
                ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
            } catch (Throwable t){}
        }
    }

    // =========================================================
    // Data Field
    private static QuestStateManager _Instance;
    private List<QuestState> _QuestStates = new FastList<QuestState>();
    
    // =========================================================
    // Constructor
    public QuestStateManager()
    {
    	ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
    }

    // =========================================================
    // Method - Public
    /**
     * Add QuestState for the specified player instance
     */
    public void addQuestState(Quest quest, L2PcInstance player, State state, boolean completed)
    {
        QuestState qs = getQuestState(player);
        if (qs == null)
            qs = new QuestState(quest, player, state, completed);
    }

    /**
     * Remove all QuestState for all player instance that does not exist
     */
    public void cleanUp()
    {
        for (int i = getQuestStates().size() - 1; i >= 0; i--)
        {
            if (getQuestStates().get(i).getPlayer() == null)
            {
                removeQuestState(getQuestStates().get(i));
                getQuestStates().remove(i);
            }
        }
    }
    
    // =========================================================
    // Method - Private
    /**
     * Remove QuestState instance
     */
    private void removeQuestState(QuestState qs)
    {
        qs = null;
    }

    // =========================================================
    // Property - Public
    public static final QuestStateManager getInstance()
    {
        if (_Instance == null)
            _Instance = new QuestStateManager();
        return _Instance;
    }
    
    /**
     * Return QuestState for specified player instance
     */
    public QuestState getQuestState(L2PcInstance player)
    {
        for (int i = 0; i < getQuestStates().size(); i++)
        {
            if (getQuestStates().get(i).getPlayer() != null && getQuestStates().get(i).getPlayer().getObjectId() == player.getObjectId())
                return getQuestStates().get(i);
                
        }
        
        return null;
    }

    /**
     * Return all QuestState
     */
    public List<QuestState> getQuestStates()
    {
        if (_QuestStates == null)
            _QuestStates = new FastList<QuestState>();
        return _QuestStates;
    }
}
