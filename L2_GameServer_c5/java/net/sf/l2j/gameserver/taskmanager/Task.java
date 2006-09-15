/**
 * 
 */
package net.sf.l2j.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;


/**
 * @author Layane
 *
 */
public abstract class Task
{
    private static Logger _log = Logger.getLogger(Task.class.getName());
    
    public void initializate()
    {
        if (Config.DEBUG)
            _log.info("Task" + getName() + " inializate");
    }
    
    public ScheduledFuture launchSpecial(ExecutedTask instance)
    {
        return null;
    }
    
    public abstract String getName();
    public abstract void onTimeElapsed(ExecutedTask task);
    
    public void onDestroy()
    {
    }
}
