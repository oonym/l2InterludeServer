package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.Random;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class Harvester implements IItemHandler
{
    private static int[] _itemIds = { 5125 };
    L2PcInstance player;
    L2MonsterInstance target;
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance _item)
    {
    	if (playable == null) return;
    	
        InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
        player = (L2PcInstance)playable;

        if(!(player.getTarget() instanceof L2MonsterInstance))
        {
            player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return;
        }

        target = (L2MonsterInstance)player.getTarget();
        boolean send = false;
        int total = 0;
        int cropId = 0;
        
        if (target != null && target.isSeeded() && target.isDead() && calcSuccess())
        {
        	L2Attackable.RewardItem[] items = target.takeHarvest();
            if (items != null && items.length > 0)
            {
                for (L2Attackable.RewardItem ritem : items)
                {
                    cropId = ritem.getItemId(); // always got 1 type of crop as reward
                    if (player.isInParty()) player.getParty().distributeItem(player, ritem, true, target);
                    else
                    {
                        L2ItemInstance item = player.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), player, target);
                        if (iu != null) iu.addItem(item);
                        send = true;
                        total += ritem.getCount();
                    }
                }
                if (send)
                {
                    SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
                    smsg.addNumber(total);
                    smsg.addItemName(cropId);
                    player.sendPacket(smsg);

                    if (iu != null) player.sendPacket(iu);
            		else player.sendPacket(new ItemList(player, false));
                }
            }
        }
        else
        {
            player.sendMessage("Target not seeded");
        }

    }
    
    public int[] getItemIds()
    {
        return _itemIds;
    }
    
    private boolean calcSuccess()
    {
        int basicSuccess = 90;
        int levelPlayer = player.getLevel();
        int levelTarget = target.getLevel();

        int diffPlayerTarget = (levelPlayer - levelTarget);
        if(diffPlayerTarget < 0)
            diffPlayerTarget = -diffPlayerTarget;
                
        // apply penalty, target <=> player levels
        // 15% penalty for each level
        if(diffPlayerTarget > 5)
        {
            basicSuccess -= (diffPlayerTarget - 5)*15;
        }
        
        
        // success rate cant be less than 1%
        if(basicSuccess < 1)
            basicSuccess = 1;
        
        Random rnd = new Random();
        int rate = rnd.nextInt(99);
        
        if(rate < basicSuccess)
            return true;
        return false;
    }

}