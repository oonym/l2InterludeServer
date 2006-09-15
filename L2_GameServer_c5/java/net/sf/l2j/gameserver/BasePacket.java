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
package net.sf.l2j.gameserver;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * 
 * @author mkizub
 */
public abstract class BasePacket implements Cloneable {

	public static class ItemRequest
	{
		int _objectId;
		int _itemId;
		int _count;
		int _price;

		public ItemRequest(int objectId, int count, int price)
		{
			_objectId = objectId;
			_count = count;
			_price = price;
		}

		public ItemRequest(int objectId, int itemId, int count, int price)
		{
			_objectId = objectId;
			_itemId = itemId;
			_count = count;
			_price = price;
		}

		public int getObjectId(){return _objectId;}
		public int getItemId(){return _itemId;}
		public void setCount(int count){_count = count;}
		public int getCount(){return _count;}
		public int getPrice(){return _price;}
	}

    /**
     * Get all information from L2ItemInstance to generate ItemInfo.<BR><BR>
     * 
     */
	public static class ItemInfo
	{
        /** Identifier of the L2ItemInstance */
        int _objectId;
        
        /** The L2Item template of the L2ItemInstance */
		L2Item _item;
        
        /** The level of enchant on the L2ItemInstance */
		int _enchant;
        
        /** The quantity of L2ItemInstance */
		int _count;
        
        /** The price of the L2ItemInstance */
		int _price;
        
        /** The custom L2ItemInstance types (used loto, race tickets) */
		int _type1;
		int _type2;
        
        /** If True the L2ItemInstance is equipped */
		int _equipped;
        
        /** The action to do clientside (1=ADD, 2=MODIFY, 3=REMOVE) */
		int _change;


        /**
         * Get all information from L2ItemInstance to generate ItemInfo.<BR><BR>
         * 
         */
		public ItemInfo(L2ItemInstance item)
		{
			if (item == null) return;
            
            // Get the Identifier of the L2ItemInstance
			_objectId = item.getObjectId();
            
            // Get the L2Item of the L2ItemInstance
			_item = item.getItem();
            
            // Get the enchant level of the L2ItemInstance
			_enchant = item.getEnchantLevel();
            
            // Get the quantity of the L2ItemInstance
			_count = item.getCount();
            
            // Get custom item types (used loto, race tickets)
			_type1 = item.getCustomType1();
			_type2 = item.getCustomType2();
            
            // Verify if the L2ItemInstance is equipped
			_equipped = item.isEquipped() ? 1 : 0;
            
            // Get the action to do clientside
	        switch (item.getLastChange())
	        {
	            case (L2ItemInstance.ADDED): { _change = 1; break; }
	            case (L2ItemInstance.MODIFIED): { _change = 2; break; }
	            case (L2ItemInstance.REMOVED): { _change = 3; break;}
	        }
		}

		public ItemInfo(L2ItemInstance item, int change)
		{
            if (item == null) return;
            
            // Get the Identifier of the L2ItemInstance
			_objectId = item.getObjectId();
            
            // Get the L2Item of the L2ItemInstance
			_item = item.getItem();
            
            // Get the enchant level of the L2ItemInstance
			_enchant = item.getEnchantLevel();
            
            // Get the quantity of the L2ItemInstance
			_count = item.getCount();
            
            // Get custom item types (used loto, race tickets)
			_type1 = item.getCustomType1();
			_type2 = item.getCustomType2();
            
            // Verify if the L2ItemInstance is equipped
			_equipped = item.isEquipped() ? 1 : 0;
            
            // Get the action to do clientside
			_change = change;
		}
		
        
		public int getObjectId(){return _objectId;}
		public L2Item getItem(){return _item;}
		public int getEnchant(){return _enchant;}
		public int getCount(){return _count;}
		public int getPrice(){return _price;}
		public int getCustomType1(){return _type1;}
		public int getCustomType2(){return _type2;}
		public int getEquipped(){return _equipped;}
		public int getChange(){return _change;}
	}

	/** The connection this packet was received from or to be sent to. */
	Connection _connection;
	
	/** The client this packet was received from or to be sent to. */
	ClientThread _client;
	
	/** Messages are organized into Double-linked lists, maintained by
	 * network and thread scheduler threads
	 */
	BasePacket _prev, _next;
	BasePacketQueue _queue;
	
	/** A field to store ByteBuffer used to decode/encode packet. */
	protected ByteBuffer _buf;
	
	protected BasePacket()
	{
	}

	protected BasePacket(Connection con)
	{
		_connection = con;
		_client = con.getClient();
	}

	protected BasePacket(ClientThread client)
	{
		_connection = client.getConnection();
		_client = client;
	}
	
	public BasePacket setConnection(Connection con)
	{
		if (_connection == null) {
			_connection = con;
			_client = _connection.getClient();
			return this;
		}
        return duplicate(con);
	}
	
	public final Connection getConnection()
	{
		return _connection;
	}
	
	public final ClientThread getClient()
	{
		return _client;
	}
	
	public BasePacket duplicate(Connection con)
	{
		try {
			BasePacket bp = (BasePacket)super.clone();
			bp._connection = con;
			bp._client = con.getClient();
			bp._prev = null;
			bp._next = null;
			bp._queue = null;
			return bp;
		} catch (CloneNotSupportedException e) { return null; /*never happens*/}
	}
    
    public int getLength()
    {
        return (_buf != null) ? _buf.limit() : 0;
    }

	/**
	 * just for information and debug purposes
	 * @return text for trace message
	 */
	public abstract String getType();
}
