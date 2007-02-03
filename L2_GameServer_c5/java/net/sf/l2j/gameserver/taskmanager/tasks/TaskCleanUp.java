/**
 * 
 */
package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Tempy
 *
 */
public final class TaskCleanUp extends Task
{
    public static String NAME = "CleanUp";
    
    public String getName()
    {
        return NAME;
    }

    public void onTimeElapsed(ExecutedTask task)
    {
        System.runFinalization();
        System.gc();
    }
}
