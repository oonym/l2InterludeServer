package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Effect;

/**
 * @author -Nemesiss-
 *
 */
public class EffectRemoveTarget extends L2Effect
{
    public EffectRemoveTarget(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    public EffectType getEffectType()
    {
        return EffectType.REMOVE_TARGET;
    }
    
    /** Notify started */
    public void onStart() {
        //just start effect
        onActionTime();
    }
    
    /** Notify exited */
    public void onExit() {
        //nothing
    }
    
    public boolean onActionTime()
    {        
        getEffected().setTarget(null);
        return true;
    }
}
