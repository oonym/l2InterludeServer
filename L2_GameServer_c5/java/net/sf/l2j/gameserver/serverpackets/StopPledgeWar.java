package net.sf.l2j.gameserver.serverpackets;

public class StopPledgeWar extends ServerBasePacket
{
    private static final String _S__7f_STOPPLEDGEWAR = "[S] 67 StopPledgeWar";
    private String _pledgeName;
    private String _char;

    public StopPledgeWar(String pledge, String charName)
    {
        _pledgeName = pledge;
        _char = charName;
    }
    
    final void runImpl(){}
    
    final void writeImpl()
    {
        writeC(0x67);
        writeS(_pledgeName);
        writeS(_char);
    }
    
    public String getType()
    {
        return _S__7f_STOPPLEDGEWAR;
    }
}