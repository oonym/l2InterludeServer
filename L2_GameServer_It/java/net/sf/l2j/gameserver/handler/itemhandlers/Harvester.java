/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.Random;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class Harvester implements IItemHandler
{
    private static final int[] ITEM_IDS = { 5125 };
    private L2PcInstance _player;
    private L2MonsterInstance _target;
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance _item)
    {
    	if (playable == null) return;
    	
        InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
        _player = (L2PcInstance)playable;

        if(!(_player.getTarget() instanceof L2MonsterInstance))
        {
            _player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
            return;
        }

        _target = (L2MonsterInstance)_player.getTarget();
        boolean send = false;
        int total = 0;
        int cropId = 0;
        
        if (_target != null && _target.isSeeded() && _target.isDead() && calcSuccess())
        {
        	L2Attackable.RewardItem[] items = _target.takeHarvest();
            if (items != null && items.length > 0)
            {
                for (L2Attackable.RewardItem ritem : items)
                {
                    cropId = ritem.getItemId(); // always got 1 type of crop as reward
                    if (_player.isInParty()) _player.getParty().distributeItem(_player, ritem, true, _target);
                    else
                    {
                        L2ItemInstance item = _player.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), _player, _target);
                        if (iu != null) iu.addItem(item);
                        send = true;
                        total += ritem.getCount();
                    }
                }
                if (send)
                {
                    SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
                    smsg.addNumber(total);
                    smsg.addItemName(cropId);
                    _player.sendPacket(smsg);

                    if (iu != null) _player.sendPacket(iu);
            		else _player.sendPacket(new ItemList(_player, false));
                }
            }
        }
        else
        {
            _player.sendMessage("Target not seeded");
        }

    }
    
    public int[] getItemIds()
    {
        return ITEM_IDS;
    }
    
    private boolean calcSuccess()
    {
        int basicSuccess = 90;
        int levelPlayer = _player.getLevel();
        int levelTarget = _target.getLevel();

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