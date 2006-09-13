package net.sf.l2j.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.gameserver.ThreadPoolManager;

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
                _QuestState.getQuest().notifyEvent(getName(), _QuestState);
                cancel();
            }
            catch (Throwable t)
            {
            }
        }
    }

    // =========================================================
    // Data Field
    private boolean _IsActive = true;
    private String _Name;
    protected QuestState _QuestState;
    private ScheduledFuture _Schedular;

    // =========================================================
    // Constructor
    public QuestTimer(QuestState qs, String name, long time)
    {
        _Name = name;
        _QuestState = qs;
        _Schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
    }

    // =========================================================
    // Method - Public
    public void cancel()
    {
        _IsActive = false;

        if (_Schedular != null) _Schedular.cancel(true);

        removeQuestTimer();
    }

    // =========================================================
    // Method - Public
    private void removeQuestTimer()
    {
        for (int i = 0; i < getQuestState().getQuestTimers().size(); i++)
        {
            if (getQuestState().getQuestTimers().get(i).getName() == getName())
                getQuestState().getQuestTimers().remove(i);
        }
    }

    // =========================================================
    // Property - Public
    public final boolean getIsActive()
    {
        return _IsActive;
    }

    public final String getName()
    {
        return _Name;
    }

    public final QuestState getQuestState()
    {
        return _QuestState;
    }

    public final String toString()
    {
        return _Name;
    }
}
