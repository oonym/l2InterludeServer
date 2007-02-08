package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerHpPercentage extends Condition
{
    private double _p;

    public ConditionPlayerHpPercentage(double p)
    {
        _p = p;
    }

    public boolean testImpl(Env env) {
        return env._player.getCurrentHp() <= env._player.getMaxHp()*_p;    }
}
