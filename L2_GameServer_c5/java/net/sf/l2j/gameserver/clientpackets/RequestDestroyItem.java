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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestDestroyItem extends ClientBasePacket
{

	private static final String _C__59_REQUESTDESTROYITEM = "[C] 59 RequestDestroyItem";
	private static Logger _log = Logger.getLogger(RequestDestroyItem.class.getName());

	private int _objectId;
	private int _count;
	/**
	 * packet type id 0x1f
	 * 
	 * sample
	 * 
	 * 59 
	 * 0b 00 00 40		// object id 
	 * 01 00 00 00		// count ??
	 * 
	 * 
	 * format:		cdd  
	 * @param decrypt
	 */
	public RequestDestroyItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = 0;
		_count = 0;

	    try {
			_objectId = readD();
			_count = readD();
	    } catch (Exception e) {}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		
		if(_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar,"[RequestDestroyItem] count <= 0! ban! oid: "+_objectId+" owner: "+activeChar.getName(),Config.DEFAULT_PUNISH);
			return;
		}
		
		int count = _count;
		
        if (activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            return;
        }
        
		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
        
		// Cannot discard item that the skill is consumming
		if (activeChar.isCastingNow())
		{
			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
			{
	            activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_THIS_ITEM));
	            return;
			}
		}

		if (itemToRemove == null || itemToRemove.isWear()) return;
        
        int itemId = itemToRemove.getItemId();
        
        if ((itemId >= 6611 && itemId <= 6621) || itemId == 6842)
            return;
        
        // Cursed Weapons cannot be destroyed
        if (CursedWeaponsManager.getInstance().isCursed(itemId))
        	return;
        
        if(!itemToRemove.isStackable() && count > 1)
        {
            Util.handleIllegalPlayerAction(activeChar,"[RequestDestroyItem] count > 1 but item is not stackable! oid: "+_objectId+" owner: "+activeChar.getName(),Config.DEFAULT_PUNISH);
            return;
        }
        
		if (_count > itemToRemove.getCount())
			count = itemToRemove.getCount();
		
		
		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped =
				activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot()); 
			InventoryUpdate iu = new InventoryUpdate();
			for (int i = 0; i < unequiped.length; i++)
			{
				activeChar.checkSSMatch(null, unequiped[i]);
				
				iu.addModifiedItem(unequiped[i]);
			}
			activeChar.sendPacket(iu);
		}
        
		if (L2PetDataTable.isPetItem(itemId))
		{
			int petObjectId = 0;
			java.sql.Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					petObjectId = rset.getInt("objId");
				}
				rset.close();
				statement.close();
				
				if (activeChar.getPet() != null && activeChar.getPet().getObjectId() == petObjectId)
				{
					activeChar.getPet().unSummon(activeChar);
				}
				
				// if it's a pet control item, delete the pet
				statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "could not delete pet objectid: ", e);
			}
			finally
			{
				try { con.close(); } catch (Exception e) {}
			}
		}
		
		L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", _objectId, count, activeChar, null);
		
		if(removedItem == null)
			return;
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0) iu.addRemovedItem(removedItem);
			else iu.addModifiedItem(removedItem);
	
			//client.getConnection().sendPacket(iu);
			activeChar.sendPacket(iu);
		}
		else sendPacket(new ItemList(activeChar, true));		
		
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

		activeChar.broadcastUserInfo();

		L2World world = L2World.getInstance();
		world.removeObject(removedItem);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__59_REQUESTDESTROYITEM;
	}
}
