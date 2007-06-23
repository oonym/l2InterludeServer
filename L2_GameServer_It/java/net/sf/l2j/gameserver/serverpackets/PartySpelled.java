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
public class PartySpelled extends L2GameServerPacket
{
    private static final String _S__EE_PartySpelled = "[S] EE PartySpelled";
    private List<Effect> _effects;
    private L2Character _activeChar;

    private class Effect
    {
    	protected int _skillId;
    	protected int _dat;
    	protected int _duration;

        public Effect(int pSkillId, int pDat, int pDuration)
        {
            this._skillId = pSkillId;
            this._dat = pDat;
            this._duration = pDuration;
        }
    }

    public PartySpelled(L2Character cha)
    {
        _effects = new FastList<Effect>();
        _activeChar = cha;
    }

    protected final void writeImpl()
    {
        if (_activeChar == null) return;
        writeC(0xee);
        writeD(_activeChar instanceof L2Summon ? 2 : 0);
        writeD(_activeChar.getObjectId());
        writeD(_effects.size());
        for (Effect temp : _effects)
        {
            writeD(temp._skillId);
            writeH(temp._dat);
            writeD(temp._duration / 1000);
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
