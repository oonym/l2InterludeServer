package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class ChristmasTree implements IItemHandler
{
    private static int[] _itemIds = {
                                     5560, // x-mas tree
                                     5561  // Special x-mas tree
                                     };

    private static int[] _npcIds = {
                                    12619, //Christmas tree w. flashing lights and snow
                                    12620
                                    };

    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        L2PcInstance activeChar = (L2PcInstance)playable;
        L2NpcTemplate template1 = null;

        int itemId = item.getItemId();
        for (int i = 0; i < _itemIds.length; i++)
        {
            if (_itemIds[i] == itemId)
            {
                template1 = NpcTable.getInstance().getTemplate(_npcIds[i]);
                break;
            }
        }
        
        if (template1 == null)
            return;

        L2Object target = activeChar.getTarget();
        if (target == null)
            target = activeChar;

        try
        {
            L2Spawn spawn = new L2Spawn(template1);
            spawn.setId(IdFactory.getInstance().getNextId());
            spawn.setLocx(target.getX());
            spawn.setLocy(target.getY());
            spawn.setLocz(target.getZ());
            L2World.getInstance().storeObject(spawn.spawnOne());

            activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

            SystemMessage sm = new SystemMessage(614);
            sm.addString("Created " + template1.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
            activeChar.sendPacket(sm);
        }
        catch (Exception e)
        {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Target is not ingame.");
            activeChar.sendPacket(sm);
        }
    }

    public int[] getItemIds()
    {
        return _itemIds;
    }
}
