package net.sf.l2j.gameserver.serverpackets;

public class StopPledgeWar extends L2GameServerPacket
{
    private static final String _S__7f_STOPPLEDGEWAR = "[S] 67 StopPledgeWar";
    private String _pledgeName;
    private String _playerName;

    public StopPledgeWar(String pledge, String charName)
    {
        _pledgeName = pledge;
        _playerName = charName;
    }
    
    protected final void writeImpl()
    {
        writeC(0x67);
        writeS(_pledgeName);
        writeS(_playerName);
    }
    
    public String getType()
    {
        return _S__7f_STOPPLEDGEWAR;
    }
}