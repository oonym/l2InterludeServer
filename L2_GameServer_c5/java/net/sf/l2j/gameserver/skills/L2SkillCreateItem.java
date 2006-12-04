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
    private final short[] create_item_id;
    private final int create_item_count;
    private final int random_count;

    public L2SkillCreateItem(StatsSet set)
    {
        super(set);
        create_item_id = set.getShortArray("create_item_id");
        create_item_count = set.getInteger("create_item_count", 0);
        random_count = set.getInteger("random_count", 1);
    }  

    /**
     * @see net.sf.l2j.gameserver.model.L2Skill#useSkill(net.sf.l2j.gameserver.model.L2Character, net.sf.l2j.gameserver.model.L2Object[])
     */
    public void useSkill(L2Character activeChar, L2Object[] targets)
    {
        if (activeChar.isAlikeDead()) return;
        if (create_item_id == null || create_item_count == 0)
        {
            SystemMessage sm = new SystemMessage(SystemMessage.SKILL_NOT_AVAILABLE);
            activeChar.sendPacket(sm);
            return;
        }
        L2PcInstance player = (L2PcInstance) activeChar;
        if (activeChar instanceof L2PcInstance)
        {            
            int rnd = _rnd.nextInt(random_count) + 1;
            int count = create_item_count * rnd;
            int rndid = _rnd.nextInt(create_item_id.length);
            giveItems(player, create_item_id[rndid], count);
        }
    }

    /**
     * @param activeChar
     * @param itemId
     * @param count
     */
    public void giveItems(L2PcInstance activeChar, short itemId, int count)
    {
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
        }
        else
        {
            SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM);
            smsg.addItemName(item.getItemId());
            activeChar.sendPacket(smsg);
        }        
        ItemList il = new ItemList(activeChar, false);
        activeChar.sendPacket(il);
    }
}