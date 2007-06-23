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

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.9.2.10 $ $Date: 2005/03/31 09:19:39 $
 */

public class PetSummon implements IItemHandler
{
    protected static final Logger _log = Logger.getLogger(PetSummon.class.getName());
	
	// all the items ids that this handler knowns
	private static final int[] ITEM_IDS = { 2375, 3500, 3501, 3502, 4422, 4423, 4424, 4425, 6648, 6649, 6650 };

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
		int npcId;
        
		if(activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));
			return;
		}
        if (activeChar.isInOlympiadMode())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
            return;
        }
		
		if (activeChar.getPet() != null)
		{
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ALREADY_HAVE_A_PET));
			return;
		}
		
		if ( activeChar.isAttackingNow() )
		{
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}
        
        if (activeChar.isMounted())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ALREADY_HAVE_A_PET));
			return;
        }
        
        if (activeChar.isCursedWeaponEquiped())
        {
        	// You can't mount while weilding a cursed weapon
        	activeChar.sendPacket(new SystemMessage(SystemMessage.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
        }
		
        npcId = L2PetDataTable.getPetIdByItemId(item.getItemId());
        
        if (npcId == 0)
        	return;
        
        if (L2PetDataTable.isWyvern(npcId))
        {
        	if(!activeChar.disarmWeapons()) return;
        	Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, 12621);
            activeChar.sendPacket(mount);
            activeChar.broadcastPacket(mount);
            activeChar.setMountType(mount.getMountType());
            activeChar.setMountObjectID(item.getObjectId());
			return;
        }
		
		
		L2NpcTemplate petTemplate = NpcTable.getInstance().getTemplate(npcId);
		

		L2PetInstance newpet = L2PetInstance.spawnPet(petTemplate, activeChar, item);
		if (newpet == null) return;
		newpet.setTitle(activeChar.getName());
		
		if (!newpet.isRespawned()) {
			newpet.setCurrentHp(newpet.getMaxHp());
			newpet.setCurrentMp(newpet.getMaxMp());
			newpet.getStat().setExp(newpet.getExpForThisLevel());
			newpet.setCurrentFed(newpet.getMaxFed());
		}
		newpet.setRunning();
		if (!newpet.isRespawned()) {
			newpet.store();
		}
		

        activeChar.setPet(newpet);

		MagicSkillUser msk = new MagicSkillUser(activeChar, 2046, 1, 1000, 600000);
		activeChar.sendPacket(msk);
		SystemMessage sm2 = new SystemMessage(SystemMessage.SUMMON_A_PET);
		activeChar.sendPacket (sm2);
        L2World.getInstance().storeObject(newpet);
		newpet.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
        PetInfo pInfo = new PetInfo(newpet);
        activeChar.sendPacket(pInfo);
		newpet.startFeed( false );
		item.setEnchantLevel(newpet.getLevel());
		
		// continue execution in 1 seconds
		ThreadPoolManager.getInstance().scheduleGeneral(new SummonFinalizer(activeChar, newpet), 900);
		
		// if pet died of hunger last time it was summoned
		if ( newpet.getCurrentFed() <= 0 )
		{
			// wait 1 minute for owner to feed
			ThreadPoolManager.getInstance().scheduleGeneral(new FeedWait(activeChar, newpet), 60000);
		}
		else
		{
			// start normal feeding
			newpet.startFeed( false );
		}
	}
	
	static class FeedWait implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _newPet;
		
		FeedWait(L2PcInstance activeChar, L2PetInstance newpet)
		{
			_activeChar = activeChar;
			_newPet = newpet;
		}
		
		public void run()
		{
			try
			{
				// owner didn't feed on time
				if ( _newPet.getCurrentFed() <= 0 )
				{
					_newPet.unSummon(_activeChar);
				}
				else
				{
					// normal feeding
					_newPet.startFeed( false );
				}
			}
			catch (Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}
	
	static class SummonFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _newPet;
		
		SummonFinalizer(L2PcInstance activeChar, L2PetInstance newpet)
		{
			_activeChar = activeChar;
			_newPet = newpet;
		}
		
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar,2046,1));
				
				_newPet.setFollowStatus(true);
				// addVisibleObject created the info packets with summon animation
				// if someone comes into range now, the animation shouldnt show any more
		        _newPet.setShowSummonAnimation(false);
			}
			catch (Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}
	
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
