package net.sf.l2j.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskTypes;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * Updates all data for the Seven Signs and Festival of Darkness engines,
 * when time is elapsed.
 * 
 * @author Tempy
 */
public class TaskSevenSignsUpdate extends Task
{
    private static final Logger _log = Logger.getLogger(TaskOlympiadSave.class.getName());
    public static final String NAME = "SevenSignsUpdate";
    
    public String getName()
    {
        return NAME;
    }

    public void onTimeElapsed(ExecutedTask task)
    {
        try {
            SevenSigns.getInstance().saveSevenSignsData(null, true);

            if (!SevenSigns.getInstance().isSealValidationPeriod())
                SevenSignsFestival.getInstance().saveFestivalData(false);
            
            _log.info("SevenSigns: Data updated successfully.");
        }
        catch (Exception e) {
            _log.warning("SevenSigns: Failed to save Seven Signs configuration: " + e);
        }
    }
    
    public void initializate()
    {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "1800000", "");
    }
}
