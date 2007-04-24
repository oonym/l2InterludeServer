/**
 * 
 */
package net.sf.l2j.gameserver.serverpackets;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class GameGuardQuery extends L2GameServerPacket
{
    private static final String _S__F9_GAMEGUARDQUERY = "[S] F9 GameGuardQuery";

    public GameGuardQuery()
    {
    	
    }
    
    public void runImpl()
    {
        // Lets make user as gg-unauthorized
        // We will set him as ggOK after reply fromclient
        // or kick
        getClient().setGameGuardOk(false);
    }
    
    public void writeImpl()
    {
        writeC(0xf9);
    }
    
    public String getType()
    {
        return _S__F9_GAMEGUARDQUERY;
    }
}
