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
package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.templates.L2Henna;

/**
 * This class represents a Non-Player-Character in the world. it can be 
 * a monster or a friendly character.
 * it also uses a template to fetch some static values.
 * the templates are hardcoded in the client, so we can rely on them.
 * 
 * @version $Revision$ $Date$
 */

public class L2HennaInstance
{
	//private static Logger _log = Logger.getLogger(L2HennaInstance.class.getName());
	
	private L2Henna _template;
	private int _symbolId;
	private short _itemIdDye;
	private int _price;
	private byte _statINT;
	private byte _statSTR;
	private byte _statCON;
	private byte _statMEM;
	private byte _statDEX;
	private byte _statWIT;
	private byte _amountDyeRequire;
	
	public L2HennaInstance(L2Henna template)
	{
		_template = template;
		_symbolId = _template.symbol_id;
		_itemIdDye = _template.dye;
		_amountDyeRequire = _template.amount;
		_price = _template.price;
		_statINT = _template.stat_INT;
		_statSTR = _template.stat_STR;
		_statCON = _template.stat_CON;
		_statMEM = _template.stat_MEM;
		_statDEX = _template.stat_DEX;
		_statWIT = _template.stat_WIT;
	}
	
	public String getName(){
		String res = "";
		if (_statINT>0)res = res + "INT +"+_statINT;
		else if (_statSTR>0)res = res + "STR +"+_statSTR;
		else if (_statCON>0)res = res + "CON +"+_statCON;
		else if (_statMEM>0)res = res + "MEN +"+_statMEM;
		else if (_statDEX>0)res = res + "DEX +"+_statDEX;
		else if (_statWIT>0)res = res + "WIT +"+_statWIT;
		
		if (_statINT<0)res = res + ", INT "+_statINT;
		else if (_statSTR<0)res = res + ", STR "+_statSTR;
		else if (_statCON<0)res = res + ", CON "+_statCON;
		else if (_statMEM<0)res = res + ", MEN "+_statMEM;
		else if (_statDEX<0)res = res + ", DEX "+_statDEX;
		else if (_statWIT<0)res = res + ", WIT "+_statWIT;
		
		return res;
	}
	
	public L2Henna getTemplate()
	{
		return _template;
	}
	
	
    public int getSymbolId()
    {
        return _symbolId;
    }
	
	public void setSymbolId(int SymbolId)
	{
		_symbolId = SymbolId;
	}
	
	public short getItemIdDye()
    {
        return _itemIdDye;
    }
	
	public void setItemIdDye(short ItemIdDye)
	{
		_itemIdDye = ItemIdDye;
	}
	
	
	public byte getAmountDyeRequire()
	{
		return _amountDyeRequire;
	}
	
	public void setAmountDyeRequire(byte AmountDyeRequire)
	{
		_amountDyeRequire = AmountDyeRequire;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public void setPrice(int Price)
	{
		_price = Price;
	}
	
	
	public byte getStatINT()
	{
		return _statINT;
	}
	
	public void setStatINT(byte StatINT)
	{
		_statINT = StatINT;
	}

	public byte getStatSTR()
	{
		return _statSTR;
	}
	
	public void setStatSTR(byte StatSTR)
	{
		_statSTR = StatSTR;
	}
	
	public byte getStatCON()
	{
		return _statCON;
	}
	
	public void setStatCON(byte StatCON)
	{
		_statCON = StatCON;
	}
	
	public byte getStatMEM()
	{
		return _statMEM;
	}
	
	public void setStatMEM(byte StatMEM)
	{
		_statMEM = StatMEM;
	}
	
	public byte getStatDEX()
	{
		return _statDEX;
	}
	
	public void setStatDEX(byte StatDEX)
	{
		_statDEX = StatDEX;
	}
	
	public byte getStatWIT()
	{
		return _statWIT;
	}
	
	public void setStatWIT(byte StatWIT)
	{
		_statWIT = StatWIT;
	}
	
		
}
