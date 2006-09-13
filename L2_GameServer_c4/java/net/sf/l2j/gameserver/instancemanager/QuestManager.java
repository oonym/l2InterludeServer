package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.jython.QuestJython;

public class QuestManager
{
    protected static Logger _log = Logger.getLogger(QuestManager.class.getName());

    // =========================================================
    private static QuestManager _Instance;
    public static final QuestManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing QuestManager");
            _Instance = new QuestManager();
            _Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<Quest> _Quests;
    
    // =========================================================
    // Constructor
    public QuestManager()
    {
    }

    // =========================================================
    // Method - Public
    // NOT WORKING CORRECTLY BECAUSE BSFMANAGER DOEN'T UNLOAD JYTHON SCRIPT
    // NEED TO FIND THE SOLUTION BEFORE THIS WILL WORK CORRECLY
    public final void reload()
    {
    	/* Re-add later just incase problem exist now
        this.getQuests().clear();
        this.load();
        */
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        QuestJython.init();
        System.out.println("Loaded: " + getQuests().size() + " quests");
    }

    // =========================================================
    // Property - Public
    public final Quest getQuest(String name)
    {
        int index = getQuestIndex(name);
        if (index >= 0) return getQuests().get(index);
        return null;
    }

    public final Quest getQuest(int questId)
    {
        int index = getQuestIndex(questId);
        if (index >= 0) return getQuests().get(index);
        return null;
    }
    
    public final int getQuestIndex(String name)
    {
        Quest quest;
        for (int i = 0; i < getQuests().size(); i++)
        {
            quest = getQuests().get(i);
            if (quest != null && quest.getName().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }
    
    public final int getQuestIndex(int questId)
    {
        Quest quest;
        for (int i = 0; i < getQuests().size(); i++)
        {
            quest = getQuests().get(i);
            if (quest != null && quest.getQuestIntId() == questId) return i;
        }
        return -1;
    }

    public final List<Quest> getQuests()
    {
        if (_Quests == null) _Quests = new FastList<Quest>();
        return _Quests;
    }
}
