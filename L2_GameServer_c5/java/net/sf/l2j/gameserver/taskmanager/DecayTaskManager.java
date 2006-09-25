/**
 * 
 */
package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.NoSuchElementException;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;

/**
 * @author la2
 * Lets drink to code!
 */
public class DecayTaskManager
{
    protected Map<L2Character,Long> _decayTasks = new FastMap<L2Character,Long>().setShared(true);

    public static DecayTaskManager _instance;
    
    public DecayTaskManager()
    {
    	ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(),10000,5000);
    }
    
    public static DecayTaskManager getInstance()
    {
        if(_instance == null)
            _instance = new DecayTaskManager();
        
        return _instance;
    }
    
    public void addDecayTask(L2Character actor)
    {
        _decayTasks.put(actor,System.currentTimeMillis());
    }

    public void addDecayTask(L2Character actor, int interval)
    {
        _decayTasks.put(actor,System.currentTimeMillis()+interval);
    }
    
    public void cancelDecayTask(L2Character actor)
    {
    	try
    	{
    		_decayTasks.remove(actor);
    	}
    	catch(NoSuchElementException e){}
    }
    
    private class DecayScheduler implements Runnable
    {
    	protected DecayScheduler()
    	{
    		// Do nothing
    	}
    	
        public void run()
        {
            Long current = System.currentTimeMillis();
            if (_decayTasks != null)
                for(L2Character actor : _decayTasks.keySet())
                {
                    if((current - _decayTasks.get(actor)) > 8500)
                    {
                        actor.onDecay();
                        _decayTasks.remove(actor);
                    }
                }
        }
    }

    public String toString()
    {
        String ret = "============= DecayTask Manager Report ============\r\n";
        ret += "Tasks count: "+_decayTasks.size()+"\r\n";
        ret += "Tasks dump:\r\n";
        
        Long current = System.currentTimeMillis();
        for( L2Character actor : _decayTasks.keySet())
        {
            ret += "Class/Name: "+actor.getClass().getSimpleName()+"/"+actor.getName()
            +" decay timer: "+(current - _decayTasks.get(actor))+"\r\n";
        }
        
        return ret;
    }
}
