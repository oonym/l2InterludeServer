package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author -Nemesiss-
 *
 */
public class EffectPsychicalMute extends L2Effect {

    
    public EffectPsychicalMute(Env env, EffectTemplate template) {
        super(env, template);
    }


    public EffectType getEffectType() {
        return L2Effect.EffectType.PSYCHICAL_MUTE;
    }

    public void onStart() {
        getEffected().startPsychicalMuted();
    }
    
    public boolean onActionTime() {
        // Simply stop the effect
        getEffected().stopPsychicalMuted(this);
        return false;
    }

    public void onExit() {
        getEffected().stopPsychicalMuted(this);
    }
}
