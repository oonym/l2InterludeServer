/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;
/**
 *
 * @author  Steuf
 */
public class ClanHallDecoration extends L2GameServerPacket
{
	private static final String _S__F7_AGITDECOINFO = "[S] F7 AgitDecoInfo";
	private ClanHall clanHall;
	private ClanHallFunction Function;
	public ClanHallDecoration(ClanHall ClanHall){
		clanHall = ClanHall;
	}
	/*
	 * Packet send, must be confirmed
	 	writeC(0xf7);
		writeD(0); // clanhall id
		writeC(0); // FUNC_RESTORE_HP (Fireplace)
		writeC(0); // FUNC_RESTORE_MP (Carpet)
		writeC(0); // FUNC_RESTORE_MP (Statue)
		writeC(0); // FUNC_RESTORE_EXP (Chandelier)
		writeC(0); // FUNC_TELEPORT (Mirror)
		writeC(0); // Crytal
		writeC(0); // Curtain
		writeC(0); // FUNC_ITEM_CREATE (Magic Curtain)
		writeC(0); // FUNC_SUPPORT 
		writeC(0); // FUNC_SUPPORT (Flag)
		writeC(0); // Front Platform
		writeC(0); // FUNC_ITEM_CREATE
		writeD(0);
		writeD(0); 
	 */
	protected final void writeImpl(){
		writeC(0xf7);
		writeD(clanHall.getId()); // clanhall id
		//FUNC_RESTORE_HP
		Function = clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
		if(Function == null || Function.getLvl() == 0)
			writeC(0);
		else if((clanHall.getGrade() == 0 && Function.getLvl() < 220) || (clanHall.getGrade() == 1 && Function.getLvl() < 160) ||
			(clanHall.getGrade() == 2 && Function.getLvl() < 260) || (clanHall.getGrade() == 3 && Function.getLvl() < 300))
			writeC(1);
		else
			writeC(2);
		//FUNC_RESTORE_MP
		Function = clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
		if(Function == null || Function.getLvl() == 0){
			writeC(0);
			writeC(0);
		}else if(((clanHall.getGrade() == 0 || clanHall.getGrade() == 1) && Function.getLvl() < 25) ||
				(clanHall.getGrade() == 2 && Function.getLvl() < 30) || (clanHall.getGrade() == 3 && Function.getLvl() < 40)){
			writeC(1);
			writeC(1);
		}else {
			writeC(2);
			writeC(2);
		}
		//FUNC_RESTORE_EXP
		Function = clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
		if(Function == null || Function.getLvl() == 0)
			writeC(0);
		else if((clanHall.getGrade() == 0 && Function.getLvl() < 25) || (clanHall.getGrade() == 1 && Function.getLvl() < 30) ||
				(clanHall.getGrade() == 2 && Function.getLvl() < 40) || (clanHall.getGrade() == 3 && Function.getLvl() < 50))
			writeC(1);
		else
			writeC(2);
		// FUNC_TELEPORT
		Function = clanHall.getFunction(ClanHall.FUNC_TELEPORT);
		if(Function == null || Function.getLvl() == 0)
			writeC(0);
		else if(Function.getLvl() < 2)
			writeC(1);
		else
			writeC(2);
		writeC(0); 
		//CURTAINS
		Function = clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
		if(Function == null || Function.getLvl() == 0)
			writeC(0);
		else if(Function.getLvl() <= 1)
			writeC(1);
		else
			writeC(2);
		//FUNC_ITEM_CREATE
		Function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if(Function == null || Function.getLvl() == 0)
			writeC(0);
		else if((clanHall.getGrade() == 0 && Function.getLvl() < 2) || Function.getLvl() < 3)
			writeC(1);
		else
			writeC(2);
		// FUNC_SUPPORT 
		Function = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
		if(Function == null || Function.getLvl() == 0){
			writeC(0);
			writeC(0);
		}else if((clanHall.getGrade() == 0 && Function.getLvl() < 2) || (clanHall.getGrade() == 1 && Function.getLvl() < 4) ||
				(clanHall.getGrade() == 2 && Function.getLvl() < 5) || (clanHall.getGrade() == 3 && Function.getLvl() < 8)){
			writeC(1);
			writeC(1);
		}else{
			writeC(2);
			writeC(2);
		}
		//Front Plateform
		Function = clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
		if(Function == null || Function.getLvl() == 0)
			writeC(0);
		else if(Function.getLvl() <= 1)
			writeC(1);
		else
			writeC(2);
		//FUNC_ITEM_CREATE
		Function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if(Function == null || Function.getLvl() == 0)
			writeC(0);
		else if((clanHall.getGrade() == 0 && Function.getLvl() < 2) || Function.getLvl() < 3)
			writeC(1);
		else
			writeC(2);
		writeD(0);
		writeD(0); 
	}
	public String getType()
	{
		return _S__F7_AGITDECOINFO;
	}
}
