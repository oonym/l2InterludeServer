package net.sf.l2j.gameserver.serverpackets;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;


public final class BuyListSeed extends L2GameServerPacket
{
    private static final String _S__E8_BUYLISTSEED = "[S] E8 BuyListSeed";
    
    //private static Logger _log = Logger.getLogger(BuyListSeed.class.getName());
    
    private int _listId;
    private List<L2ItemInstance> _list = new FastList<L2ItemInstance>();
    private int _money;
    private int _castle;

    public BuyListSeed(L2TradeList list, int castleId, int currentMoney)
    {
        _money = currentMoney;
        _listId = list.getListId();
        _list = list.getItems();
        _castle = castleId;
    }
    
    
    //;BuyListSeedPacket;ddh(h dddhh [dhhh] d)
    protected final void writeImpl()
    {
        writeC(0xe8);
        
        writeD(_money);                                 // current money
        writeD(_listId);                                // list id
        
        writeH(_list.size());                           // list length

        for (L2ItemInstance item : _list)
        {
            writeH(item.getItem().getType1());          // item->type1
            writeD(item.getObjectId());                 // objectId
            writeD(item.getItemId());                   // item id
            writeD(CastleManager.getInstance().getCastle(_castle).getSeedProduction(item.getItemId())); // items count
            writeH(item.getItem().getType2());          // item->type2
            writeH(0x00);                               // unknown :)
        
            writeD(0x03);                               // price
        }
    }
    
    public String getType()
    {
        return _S__E8_BUYLISTSEED;
    }
}
