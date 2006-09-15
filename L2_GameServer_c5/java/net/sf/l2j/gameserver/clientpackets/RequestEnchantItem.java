package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

public class RequestEnchantItem extends ClientBasePacket
{
    protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
    private static final String _C__58_REQUESTENCHANTITEM = "[C] 58 RequestEnchantItem";
    private static final int[] crystalscrolls = {731, 732, 949, 950, 953, 954, 957, 958, 961, 962 };

    private int _objectId;
    /**
     * packet type id 0x58
     * 
     * sample
     * 
     * 58
     * c0 d5 00 10 // objectId
     * 
     * format:      cd
     * @param decrypt
     */
    public RequestEnchantItem(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _objectId = 0;
        try {
        _objectId = readD();
        } catch (Exception e) {}
    }

    void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || _objectId == 0) return;

        L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        L2ItemInstance scroll = activeChar.getActiveEnchantItem();
        activeChar.setActiveEnchantItem(null);
        if (item == null || scroll == null) return;
        
        if(item.isWear())
        {
            Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant a weared Item", IllegalPlayerAction.PUNISH_KICK);
            return;
        }
        int itemType2 = item.getItem().getType2();
        boolean enchantItem = false;
        boolean blessedScroll = false;
        int crystalId = 0;
        
        /** pretty code ;D */
        switch (item.getItem().getCrystalType())
        {
            case L2Item.CRYSTAL_A:
                crystalId = 1461;
                switch(scroll.getItemId())
                {
                    case 729: case 731: case 6569:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 730: case 732: case 6570:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_B:
                crystalId = 1460;
                switch(scroll.getItemId())
                {
                    case 947: case 949: case 6571:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 948: case 950: case 6572:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_C:
                crystalId = 1459;
                switch(scroll.getItemId())
                {
                    case 951: case 953: case 6573:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 952: case 954: case 6574:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_D:
                crystalId = 1458;
                switch(scroll.getItemId())
                {
                    case 955: case 957: case 6575:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 956: case 958: case 6576:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_S:
                crystalId = 1462;
                switch(scroll.getItemId())
                {
                    case 959: case 961: case 6577:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 960: case 962: case 6578:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
        }
        
        if (!enchantItem)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITION));
            return;
        }
        
        // Get the scroll type - Yesod
        if (scroll.getItemId() >= 6569 && scroll.getItemId() <= 6578)
            blessedScroll = true;
        else
            for (int crystalscroll : crystalscrolls)
                if(scroll.getItemId() == crystalscroll)
                {
                    blessedScroll = true; break;
                }
        
        scroll = activeChar.getInventory().destroyItem("Enchant", scroll, activeChar, item);
        if(scroll == null)
        {
            activeChar.sendMessage("You dont have such an enchant scroll");
            Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant with a scroll he doesnt have", Config.DEFAULT_PUNISH);
            return;
        }
        
        SystemMessage sm = new SystemMessage(SystemMessage.ENCHANT_SCROLL_CANCELLED);
        activeChar.sendPacket(sm);

        int chance = Config.ENCHANT_CHANCE;
        if (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX 
                || (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR 
                        && item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL))
            chance = 100;
        
        if (Rnd.get(100) < chance)
        {
            if (item.getEnchantLevel() >= Config.ENCHANT_MAX)
            {
                activeChar.sendMessage("Enchant failed.");
                return;
            }
            if (item.getEnchantLevel() == 0)
            {
                sm = new SystemMessage(SystemMessage.S1_SUCCESSFULLY_ENCHANTED);
                sm.addItemName(item.getItemId());
                activeChar.sendPacket(sm);
            }
            else
            {
                sm = new SystemMessage(SystemMessage.S1_S2_SUCCESSFULLY_ENCHANTED);
                sm.addNumber(item.getEnchantLevel());
                sm.addItemName(item.getItemId());
                activeChar.sendPacket(sm);
            }
            item.setEnchantLevel(item.getEnchantLevel()+1);
            item.updateDatabase();
        }
        else
        {
            if (!blessedScroll)
            {
                if (item.getEnchantLevel() > 0)
                {
                    sm = new SystemMessage(SystemMessage.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item.getItemId());
                    activeChar.sendPacket(sm);
                }
                else
                {
                    sm = new SystemMessage(SystemMessage.ENCHANTMENT_FAILED_S1_EVAPORATED);
                    sm.addItemName(item.getItemId());
                    activeChar.sendPacket(sm);
                }
            }
            else
            {
                sm = new SystemMessage(SystemMessage.BLESSED_ENCHANT_FAILED);
                activeChar.sendPacket(sm);
            }
            
            if (!blessedScroll)
            {
                if (item.getEnchantLevel() > 0)
                {
                    sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item.getItemId());
                    activeChar.sendPacket(sm);
                }
                else
                {
                    sm = new SystemMessage(SystemMessage.S1_DISARMED);
                    sm.addItemName(item.getItemId());
                    activeChar.sendPacket(sm);
                }

                L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
                if (item.isEquipped())
                {
                    InventoryUpdate iu = new InventoryUpdate();
                    for (int i = 0; i < unequiped.length; i++)
                    {
                        iu.addModifiedItem(unequiped[i]);
                    }
                    activeChar.sendPacket(iu);
                
                    activeChar.broadcastUserInfo();
                }
                
                int count = item.getCrystalCount() - (item.getItem().getCrystalCount() +1) / 2;
                if (count < 1) count = 1;
    
                L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
                if (destroyItem == null) return;
                
                L2ItemInstance crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);
            
                sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
                sm.addItemName(crystals.getItemId());
                sm.addNumber(count);
                activeChar.sendPacket(sm);
    
                if (!Config.FORCE_INVENTORY_UPDATE)
                {
                    InventoryUpdate iu = new InventoryUpdate();
                    if (destroyItem.getCount() == 0) iu.addRemovedItem(destroyItem);
                    else iu.addModifiedItem(destroyItem);
                    iu.addItem(crystals);
                    
                    activeChar.sendPacket(iu);
                }
                else activeChar.sendPacket(new ItemList(activeChar, true));
            
                StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
                su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
                activeChar.sendPacket(su);
            
                activeChar.broadcastUserInfo();
            
                L2World world = L2World.getInstance();
                world.removeObject(destroyItem);
            }
            else
            {
                item.setEnchantLevel(0);
                item.updateDatabase();
            }
        }
        StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
        activeChar.sendPacket(su);      
        
        activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); //FIXME i'm really not sure about this...
        activeChar.sendPacket(new ItemList(activeChar, false)); //TODO update only the enchanted item
        activeChar.broadcastUserInfo();
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__58_REQUESTENCHANTITEM;
    }
}
