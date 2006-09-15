package net.sf.l2j.gameserver.skills;

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
