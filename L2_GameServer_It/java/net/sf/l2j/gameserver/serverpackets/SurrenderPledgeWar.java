package net.sf.l2j.gameserver.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket
{
    private static final String _S__81_SURRENDERPLEDGEWAR = "[S] 69 SurrenderPledgeWar";
    private String _pledgeName;
    private String _playerName;

    public SurrenderPledgeWar(String pledge, String charName)
    {
        _pledgeName = pledge;
        _playerName = charName;
    }
    
    protected final void writeImpl()
    {
        writeC(0x69);
        writeS(_pledgeName);
        writeS(_playerName);
    }
    
    public String getType()
    {
        return _S__81_SURRENDERPLEDGEWAR;
    }
}