package net.sf.l2j.gameserver.serverpackets;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends ServerBasePacket
{
    private static final String _S__EE_PartySpelled = "[S] EE PartySpelled";
    private List<Effect> _effects;
    private L2Character _char;

    class Effect
    {
        int skillId;
        int dat;
        int duration;

        public Effect(int pSkillId, int pDat, int pDuration)
        {
            this.skillId = pSkillId;
            this.dat = pDat;
            this.duration = pDuration;
        }
    }

    public PartySpelled(L2Character cha)
    {
        _effects = new FastList<Effect>();
        _char = cha;
    }

    final void runImpl()
    {
        // no long-running tasks
    }

    final void writeImpl()
    {
        if (_char == null) return;
        writeC(0xee);
        writeD(_char instanceof L2Summon ? 2 : 0);
        writeD(_char.getObjectId());
        writeD(_effects.size());
        for (Effect temp : _effects)
        {
            writeD(temp.skillId);
            writeH(temp.dat);
            writeD(temp.duration / 1000);
        }

    }

    public void addPartySpelledEffect(int skillId, int dat, int duration)
    {
        _effects.add(new Effect(skillId, dat, duration));
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__EE_PartySpelled;
    }
}
