package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;

public class Fishing implements ISkillHandler 
{ 
    //private static Logger _log = Logger.getLogger(SiegeFlag.class.getName()); 
	//protected SkillType[] _skillIds = {SkillType.FISHING};
	protected SkillType[] _skillIds = {SkillType.FISHING}; 
    
    public void useSkill(L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance)activeChar;
		
		if (player.isFishing())
		{
			if (player.GetFish() !=null) player.GetFish().DoDie(false);
			else player.EndFishing(false);
			//Cancels fishing			
			player.sendPacket(new SystemMessage(1458));
			return;
		}		
        //if ()
		//{			
			//1456	You can't fish while you are on board			
			//return;
		//}
		if (activeChar.getZ() >= -3700)
		{
            //You can't fish here
			player.sendPacket(new SystemMessage(1457));
			if (!player.isGM())
			return;
		}
		if (activeChar.getZ() <= -3800)
		{
            //You can't fish in water
			player.sendPacket(new SystemMessage(1455));
			if (!player.isGM())
			return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		if ((weaponItem==null || weaponItem.getItemType() != L2WeaponType.ROD))
		{
			//Fishing poles are not installed
			player.sendPacket(new SystemMessage(1453));
			return;
		}		
		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
		    //Not enough bait
		    player.sendPacket(new SystemMessage(1459));
            return;
		}
		//if (!Config.ALLOWFISHING && !player.isGM())
		//{
		//	player.sendMessage("Not Working Yet");
		//	return;
		//}		
		player.SetLure(lure);
		L2ItemInstance lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);

		if (lure2 == null || lure2.getCount() == 0)
		{
			player.sendPacket(new ItemList(player,false));
		}
		else
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(lure2);
			player.sendPacket(iu);
		}
		player.StartFishing();		
		
		
        
        
    } 
    
    public SkillType[] getSkillIds() 
    { 
        return _skillIds; 
    } 
    
}
