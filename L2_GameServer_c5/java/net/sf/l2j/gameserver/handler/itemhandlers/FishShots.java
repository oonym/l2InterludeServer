package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author -Nemesiss-
 *
 */
public class FishShots implements IItemHandler 
{ 
	// All the item IDs that this handler knows.
	private static int[] _itemIds = { 6535, 6536, 6537, 6538, 6539, 6540 }; 
	private static int[] _skillIds = { 2181, 2182, 2183, 2184, 2185, 2186 };

	/* (non-Javadoc) 
 	* @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance) 
 	*/ 
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
        
		L2PcInstance activeChar = (L2PcInstance)playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem(); 
		
		if (weaponInst == null || weaponItem.getItemType() != L2WeaponType.ROD)
		{		
			return;
		}

		if (weaponInst.getChargedFishshot())
		{
			// spiritshot is already active
			return;
		} 

		int FishshotId = item.getItemId();  
		int grade = weaponItem.getCrystalType();		
		int count = item.getCount(); 		

		if ((grade == L2Item.CRYSTAL_NONE && FishshotId != 6535) ||  
		(grade == L2Item.CRYSTAL_D && FishshotId != 6536) ||  
		(grade == L2Item.CRYSTAL_C && FishshotId != 6537) ||  
		(grade == L2Item.CRYSTAL_B && FishshotId != 6538) ||  
		(grade == L2Item.CRYSTAL_A && FishshotId != 6539) ||  
		(grade == L2Item.CRYSTAL_S && FishshotId != 6540)) 
		{ 
			//1479 - This fishing shot is not fit for the fishing pole crystal.             
			activeChar.sendPacket(new SystemMessage(1479));
			return; 
		} 

		if (count < 1) 
		{ 			
			return; 
		} 

		weaponInst.setChargedFishshot(true);
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
        L2Object oldTarget = activeChar.getTarget();
        activeChar.setTarget(activeChar);

		activeChar.sendPacket(new SystemMessage(SystemMessage.ENABLED_SPIRITSHOT));
        
		MagicSkillUser MSU = new MagicSkillUser(activeChar,_skillIds[grade],1,0,0); 
        Broadcast.toSelfAndKnownPlayers(activeChar, MSU);
        activeChar.setTarget(oldTarget);        
	} 

	public int[] getItemIds() 
	{ 
		return _itemIds; 
	} 
}
