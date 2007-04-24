package net.sf.l2j.gameserver.serverpackets;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.CropProcure;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class SellListProcure extends L2GameServerPacket
{
    private static final String _S__E9_SELLLISTPROCURE = "[S] E9 SellListProcure";
    //private static Logger _log = Logger.getLogger(SellListProcure.class.getName());
    
    private final L2PcInstance _char;
    private int _money;
    private Map<L2ItemInstance,Integer> _sellList = new FastMap<L2ItemInstance,Integer>();
    private List<CropProcure> _procureList = new FastList<CropProcure>();
    private int _castle;
    
    public SellListProcure(L2PcInstance player, int castleId)
    {
        _money = player.getAdena();
        _char = player;
        _castle = castleId;
        _procureList =  CastleManager.getInstance().getCastle(_castle).getCropProcure();
        for(CropProcure c : _procureList)
        {
            L2ItemInstance item = _char.getInventory().getItemByItemId(c.getId());
            if(item != null && c.getAmount() > 0)
            {
                _sellList.put(item,c.getAmount());
            }
        }
    }
    
    protected final void writeImpl()
    {
        writeC(0xE9);
        writeD(_money);         // money
        writeD(0x00);           // lease ?
        writeH(_sellList.size());         // list size
        
        for(L2ItemInstance item : _sellList.keySet())
        {
            writeH(item.getItem().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD(_sellList.get(item));  // count
            writeH(item.getItem().getType2());
            writeH(0);  // unknown
            writeD(0);  // price, u shouldnt get any adena for crops, only raw materials
        }
    }
    
    public String getType()
    {
        return _S__E9_SELLLISTPROCURE;
    }
}
