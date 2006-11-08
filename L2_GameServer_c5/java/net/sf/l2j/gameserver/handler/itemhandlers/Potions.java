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

import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class Potions implements IItemHandler
{
    private static int[] _itemIds = { 65, 725, 726, 727, 728, 734, 735, 1060, 1061, 1062,
                                      1374, 1375, 1539, 1540, 5591, 5592, 6035, 6036, 6652, 
                                      6553, 6554, 6555, 8600,8601,8602, 8603, 8604, 
                                      8605, 8606, 8607, 8608, 8609, 8610, 8611, 8612, 8613, 8614 };
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        L2PcInstance activeChar;
        boolean res = false;
        if (playable instanceof L2PcInstance)
            activeChar = (L2PcInstance)playable;
        else if (playable instanceof L2PetInstance)
            activeChar = ((L2PetInstance)playable).getOwner();
        else
            return;

        if (activeChar.isInOlympiadMode())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
            return;
        }
        
        if (activeChar.isAllSkillsDisabled())
        {
            ActionFailed af = new ActionFailed();
            activeChar.sendPacket(af);
            return;
        }
            
        int itemId = item.getItemId();

        /*
         * Mana potions
         */
        if (itemId == 726) // mana drug, xml: 2003
            res = usePotion(activeChar, 2003, 1); // configurable through xml till handler implemented
        else if (itemId == 728) // mana_potion, xml: 2005
            res = usePotion(activeChar, 2005, 1);
        /*
         * Healing and speed potions
         */
        else if (itemId == 65) // red_potion, xml: 2001
            res = usePotion(activeChar, 2001, 1);
        else if (itemId == 725) // healing_drug, xml: 2002
        {
            if (activeChar.getAllEffects() != null)
            {
                for (L2Effect e : activeChar.getAllEffects())
                {
                    if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                    {
                        SystemMessage sm = new SystemMessage(48);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);
                        return;
                    }
                }
            }
            
            res = usePotion(activeChar, 2002, 1);
        }    
        else if (itemId == 727) // _healing_potion, xml: 2032
        {
            if (activeChar.getAllEffects() != null)
            {
                for (L2Effect e : activeChar.getAllEffects())
                {
                    if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                    {
                        SystemMessage sm = new SystemMessage(48);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);
                        return;
                    }
                }
            }
            
            res = usePotion(activeChar, 2032, 1);
        }    
        else if (itemId == 734) // quick_step_potion, xml: 2011
            res = usePotion(activeChar, 2011, 1);
        else if (itemId == 735) // swift_attack_potion, xml: 2012
            res = usePotion(activeChar, 2012, 1);
        else if (itemId == 1060) // lesser_healing_potion, xml: 2031
        {
            if (activeChar.getAllEffects() != null)
            {
                for (L2Effect e : activeChar.getAllEffects())
                {
                    if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                    {
                        SystemMessage sm = new SystemMessage(48);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);
                        return;
                    }
                }
            }
            
            res = usePotion(activeChar, 2031, 1);
        }    
        else if (itemId == 1061) // healing_potion, xml: 2032
        {
            if (activeChar.getAllEffects() != null)
            {
                for (L2Effect e : activeChar.getAllEffects())
                {
                    if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                    {
                        SystemMessage sm = new SystemMessage(48);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);
                        return;
                    }
                }
            }
            
            res = usePotion(activeChar, 2032, 1);
        }  
        else if (itemId == 1062) // haste_potion, xml: 2033
            res = usePotion(activeChar, 2033, 1);   
        else if (itemId == 1374) // adv_quick_step_potion, xml: 2034
            res = usePotion(activeChar, 2034, 1);
        else if (itemId == 1375) // adv_swift_attack_potion, xml: 2035
            res = usePotion(activeChar, 2035, 1);
        else if (itemId == 1539) // greater_healing_potion, xml: 2037
        {
            if (activeChar.getAllEffects() != null)
            {
                for (L2Effect e : activeChar.getAllEffects())
                {
                    if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                    {
                        SystemMessage sm = new SystemMessage(48);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);
                        return;
                    }
                }
            }
            
            res = usePotion(activeChar, 2037, 1);
        }    
        else if (itemId == 1540) // quick_healing_potion, xml: 2038
        {
            if (activeChar.getAllEffects() != null)
            {
                for (L2Effect e : activeChar.getAllEffects())
                {
                    if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                    {
                        SystemMessage sm = new SystemMessage(48);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);
                        return;
                    }
                }
            }
            
            res = usePotion(activeChar, 2038, 1);
        }    
        else if (itemId == 5591 || itemId == 5592) // CP and Greater CP Potion
        {
            if (activeChar.getAllEffects() != null)
            {
                for (L2Effect e : activeChar.getAllEffects())
                {
                    if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
                    {
                        SystemMessage sm = new SystemMessage(48);
                        sm.addItemName(itemId);
                        activeChar.sendPacket(sm);
                        return;
                    }
                }
            }
            
            MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2166, 1, 1, 0);
            activeChar.broadcastPacket(MSU);
                
            //usePotion(activeChar, 5591, 2);

            int amountCP = (itemId == 5591) ? 50 : 200;
            activeChar.setCurrentCp(activeChar.getCurrentCp() + amountCP);

            res = true;
        }
        else if (itemId == 6035) // Magic Haste Potion, xml: 2169
            res = usePotion(activeChar, 2169, 1);
        else if (itemId == 6036) // Greater Magic Haste Potion, xml: 2169
            res = usePotion(activeChar, 2169, 2);
        
        //Valakas Amulets  
        else if (itemId == 6652) // Amulet Protection of Valakas  
            res = usePotion(activeChar, 2231, 1);  
        else if (itemId == 6653) // Amulet Flames of Valakas  
            res = usePotion(activeChar, 2223, 1);  
        else if (itemId == 6654) // Amulet Flames of Valakas  
            res = usePotion(activeChar, 2233, 1);  
        else if (itemId == 6655) // Amulet Slay Valakas  
            res = usePotion(activeChar, 2232, 1);       
        
        //Herbs
        else if (itemId == 8600) // Herb of Life
            res = usePotion(activeChar, 2278, 1);
        else if (itemId == 8601) //Greater Herb of Life
            res = usePotion(activeChar, 2278, 2);
        else if (itemId == 8602) //Superior Herb of Life
            res = usePotion(activeChar, 2278, 3);      
        else if (itemId == 8603) // Herb of Mana
            res = usePotion(activeChar, 2279, 1);
        else if (itemId == 8604) // Greater Herb of Mane
            res = usePotion(activeChar, 2279, 2);
        else if (itemId == 8605) // Superior Herb of Mane
            res = usePotion(activeChar, 2279, 3);
        else if (itemId == 8606) // Herb of Strength
            res = usePotion(activeChar, 2280, 1);            
        else if (itemId == 8607) // Herb of Magic
            res = usePotion(activeChar, 2281, 1);
        else if (itemId == 8608) // Herb of Atk. Spd.
            res = usePotion(activeChar, 2282, 1);
        else if (itemId == 8609) // Herb of Casting Spd.
            res = usePotion(activeChar, 2283, 1);
        else if (itemId == 8610) // Herb of Critical Attack
            res = usePotion(activeChar, 2284, 1);
        else if (itemId == 8611) // Herb of Speed
            res = usePotion(activeChar, 2285, 1);
        else if (itemId == 8612){ // Herb of Warrior
            res = usePotion(activeChar, 2280, 1);//Herb of Strength
            res = usePotion(activeChar, 2282, 1);//Herb of Atk. Spd
            res = usePotion(activeChar, 2284, 1);//Herb of Critical Attack
            }
       else if (itemId == 8613){ // Herb of Mystic
            res = usePotion(activeChar, 2281, 1);//Herb of Magic
            res = usePotion(activeChar, 2283, 1);//Herb of Casting Spd.
            }            
       else if (itemId == 8614){ // Herb of Warrior
           res = usePotion(activeChar, 2278, 3);//Superior Herb of Life
           res = usePotion(activeChar, 2279, 3);//Superior Herb of Mana 
       }
        if (res)
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
    }
    public boolean usePotion(L2PcInstance activeChar, int magicId,int level) {
        L2Skill skill = SkillTable.getInstance().getInfo(magicId,level);
        if (skill != null) {
            activeChar.doCast(skill);
            if (!(activeChar.isSitting() && !skill.isPotion()))
                return true;
        }
        return false;
    }
    public int[] getItemIds()
    {
        return _itemIds;
    }
}
