/**
 * 
 */
package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

import org.python.util.PythonInterpreter;

/**
 * @author Layane
 *
 */
public class TaskJython extends Task
{
    public static final String NAME = "jython";
    
    private final PythonInterpreter _python = new PythonInterpreter();
    
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
        _python.cleanup();
        _python.exec("import sys");
        _python.execfile("data/jscript/cron/" + task.getParams()[2]);
    }

}
