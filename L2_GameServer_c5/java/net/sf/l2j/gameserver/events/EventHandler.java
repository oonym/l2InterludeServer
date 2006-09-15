/**
 * 
 */
package net.sf.l2j.gameserver.events;

/**
 * @author Layane
 *
 */
public abstract class EventHandler
{
    private Object _owner;
    
    public EventHandler(Object owner)
    {
        _owner = owner;
    }
    
    public final Object getOwner()
    {
        return _owner;
    }
    
    public final boolean equals(Object object)
    {
        if (object instanceof EventHandler && _owner == ((EventHandler)object)._owner)
            return true;
        return false;
    }
    
    public abstract void handler(Object trigger, IEventParams params);
    
}
