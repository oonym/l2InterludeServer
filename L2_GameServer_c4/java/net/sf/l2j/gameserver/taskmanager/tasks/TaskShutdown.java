/**
 * 
 */
package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 *
 */
public class TaskShutdown extends Task
{
    public static String NAME = "shutdown";
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.taskmanager.Task#getName()
     */
    @Override
    public String getName()
    {
        return NAME;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.taskmanager.Task#onTimeElapsed(net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask)
     */
    @Override
    public void onTimeElapsed(ExecutedTask task)
    {
        Shutdown handler = new Shutdown(Integer.valueOf(task.getParams()[2]),false);
        handler.start();
    }

}
