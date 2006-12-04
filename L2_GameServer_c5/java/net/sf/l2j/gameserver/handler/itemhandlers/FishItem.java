package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.List;

import net.sf.l2j.gameserver.FishTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.FishDropData;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author -Nemesiss-
 *
 */
public class FishItem implements IItemHandler
{
	private static short[] _itemIds = null;
	public FishItem()
	{
		FishTable ft = FishTable.getInstance();
        _itemIds = new short[ft.GetFishItemCount()];
        for (int i = 0; i < ft.GetFishItemCount(); i++)
        {
            _itemIds[i] = ft.getFishIdfromList(i);
        }
    }
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
        L2PcInstance activeChar = (L2PcInstance)playable;
		List<FishDropData> rewards = FishTable.getInstance().GetFishRreward(item.getItemId());
		int chance = Rnd.get(100);
		int count = 0;
		takeItems(activeChar, item.getItemId());
		for (FishDropData d: rewards)
		{
			if (chance >= d.getMinChance() && chance <= d.getMaxChance())
			{
				giveItems(activeChar, d.getRewardItemId(), d.getCount());
				count++;
				break;
			}

		}
		if (count == 0)
		{
			//send msg
			activeChar.sendMessage("Fish failed to open!");
		}
	}
	public void giveItems(L2PcInstance activeChar, short itemId, int count)
    {
        activeChar.addItem("FishItem", itemId, count, null, false);
        
        if (count > 1)
        {
            SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
            smsg.addItemName(itemId);
            smsg.addNumber(count);
            activeChar.sendPacket(smsg);
        } else
        {
            SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM);
            smsg.addItemName(itemId);
            activeChar.sendPacket(smsg);
        }
    }

    public void takeItems(L2PcInstance player, int itemId)
    {
		player.destroyItemByItemId("FishItem", itemId, 1, null, false);
    }
	public short[] getItemIds()
	{
		return _itemIds;
	}
}
