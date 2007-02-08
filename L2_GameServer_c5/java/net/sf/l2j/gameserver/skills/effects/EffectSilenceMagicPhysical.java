package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectSilenceMagicPhysical extends L2Effect {

   
    public EffectSilenceMagicPhysical(Env env, EffectTemplate template) {
        super(env, template);
    }

    public EffectType getEffectType() {
        return L2Effect.EffectType.SILENCE_MAGIC_PHYSICAL;
    }

    public void onStart()
    {
        getEffected().startMuted();
        getEffected().startPsychicalMuted();
    }
   
    public boolean onActionTime()
    {
        getEffected().stopMuted(this);
        getEffected().stopPsychicalMuted(this);
        return false;
    }

    public void onExit()
    {
        getEffected().stopMuted(this);
        getEffected().stopPsychicalMuted(this);
    }
}