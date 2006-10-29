package net.sf.l2j.gameserver.skills;

import java.util.Random;

import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * @author Nemesiss
 *
 */
public class L2SkillCreateItem extends L2Skill
{
    private static final Random _rnd = new Random();
    private final int need_item_id;
    private final int need_item_count;
    private final int create_item_id;
    private final int create_item_count;
    private final int random_count;

    public L2SkillCreateItem(StatsSet set)
    {
        super(set);

        need_item_id = set.getInteger("need_item_id", 0);
        need_item_count = set.getInteger("need_item_count", 0);
        create_item_id = set.getInteger("create_item_id", 0);
        create_item_count = set.getInteger("create_item_count", 0);
        random_count = set.getInteger("random_count", 0);

    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.l2j.gameserver.model.L2Skill#checkCondition(net.sf.l2j.gameserver.mo
     del.L2Character)
     */
    public boolean checkCondition(L2Character activeChar)
    {
        if (activeChar instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) activeChar;
            {
                //boolean have_item = false;
                if (!CheckItems(player, need_item_id, need_item_count))
                {
                    SystemMessage sm = new SystemMessage(701);
                    activeChar.sendPacket(sm);
                    return false;
                }

            }

        }
        return super.checkCondition(activeChar, false);

    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.l2j.gameserver.model.L2Skill#useSkill(net.sf.l2j.gameserver.model.L2
     Character,
     * net.sf.l2j.gameserver.model.L2Object[])
     */
    public void useSkill(L2Character activeChar, L2Object[] targets)
    {
        if (activeChar.isAlikeDead()) return;

        if (need_item_id == 0 || create_item_id == 0 || create_item_count == 0 || need_item_count == 0)
        {
            SystemMessage sm = new SystemMessage(SystemMessage.SKILL_NOT_AVAILABLE);
            activeChar.sendPacket(sm);
            return;
        }
        L2PcInstance player = (L2PcInstance) activeChar;
        if (activeChar instanceof L2PcInstance)
        {
            int number = 0;
            int check = _rnd.nextInt(random_count) + 1;
            number = create_item_count * check;
            takeItems(player, need_item_id, need_item_count);
            giveItems(player, create_item_id, number);
        }

    }

    /**
     * @param activeChar
     * @param itemId
     * @param count
     * @return
     */
    public boolean CheckItems(L2PcInstance activeChar, int itemId, int count)
    {
        if (activeChar.getInventory().getItemByItemId(itemId) != null)
        {
            boolean check = false;
            int item_count = activeChar.getInventory().getItemByItemId(itemId).getCount();
            if (item_count >= count)
            {
                check = true;
            }
            else
            {
                check = false;
            }
            return check;
        }
        else
        {
            return false;
        }

    }

    /**
     * @param activeChar
     * @param itemId
     * @param count
     */
    public void giveItems(L2PcInstance activeChar, int itemId, int count)
    {
        //L2Item item = ItemTable.getInstance().getTemplate(itemId);
        L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
        if (item == null) return;
        item.setCount(count);
        activeChar.getInventory().addItem("Skill", item, activeChar, activeChar);
        
        if (count > 1)
        {
            SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
            smsg.addItemName(item.getItemId());
            smsg.addNumber(count);
            activeChar.sendPacket(smsg);
        } else
        {
            SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM);
            smsg.addItemName(item.getItemId());
            activeChar.sendPacket(smsg);
        }
        
        ItemList il = new ItemList(activeChar, false);
        activeChar.sendPacket(il);
    }

    public void takeItems(L2PcInstance player, int itemId, int count)
    {
        L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
        if (item == null) return;
        player.destroyItemByItemId("Skill", itemId, count, player, false);
        SystemMessage smsg;
        smsg = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
        smsg.addItemName(itemId);
        player.sendPacket(smsg);
        ItemList il = new ItemList(player, false);
        player.sendPacket(il);
    }
}