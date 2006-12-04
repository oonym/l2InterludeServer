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
package net.sf.l2j.gameserver.templates;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2Henna
{

	public final int symbol_id;
	public final String symbol_name;
	public final short dye;
	public final int price;
	public final byte amount;
	public final byte stat_INT;
	public final byte stat_STR;
	public final byte stat_CON;
	public final byte stat_MEM;
	public final byte stat_DEX;
	public final byte stat_WIT;
	
	public L2Henna(StatsSet set)
	{
		
		symbol_id          = set.getInteger("symbol_id");
		symbol_name		   = ""; //set.getString("symbol_name");
		dye                = set.getShort("dye");
		price              = set.getInteger("price");
		amount			   = set.getByte("amount");   
		stat_INT           = set.getByte("stat_INT");
		stat_STR           = set.getByte("stat_STR");
		stat_CON           = set.getByte("stat_CON");
		stat_MEM           = set.getByte("stat_MEM");
		stat_DEX	       = set.getByte("stat_DEX");
		stat_WIT           = set.getByte("stat_WIT");
		
	}
	
	public int getSymbolId()
	{
		return symbol_id;
	}
	/**
	 * @return
	 */
	public short getDyeId()
	{
		return dye;
	}
	/**
	 * @return
	 */
	public int getPrice()
	{
		return price;
	}
	/**
	 * @return
	 */
	public byte getAmountDyeRequire()
	{
		return amount;
	}
	/**
	 * @return
	 */
	public byte getStatINT()
	{
		return stat_INT;
	}
	/**
	 * @return
	 */
	public byte getStatSTR()
	{
		return stat_STR;
	}
	/**
	 * @return
	 */
	public byte getStatCON()
	{
		return stat_CON;
	}
	/**
	 * @return
	 */
	public byte getStatMEM()
	{
		return stat_MEM;
	}
	/**
	 * @return
	 */
	public byte getStatDEX()
	{
		return stat_DEX;
	}
	/**
	 * @return
	 */
	public byte getStatWIT()
	{
		return stat_WIT;
	}
	/**
	 * @return
	 */
}
