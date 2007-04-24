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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Macro;
import net.sf.l2j.gameserver.model.L2Macro.L2MacroCmd;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public final class RequestMakeMacro extends L2GameClientPacket
{
	private L2Macro _macro;
	private int _commands_lenght = 0;
    
	private static final String _C__C1_REQUESTMAKEMACRO = "[C] C1 RequestMakeMacro";
	
	/**
	 * packet type id 0xc1
	 * 
	 * sample
	 * 
	 * c1
	 * d // id
	 * S // macro name
     * S // unknown  desc
     * S // unknown  acronym
     * c // icon
     * c // count
     * 
     * c // entry
     * c // type
     * d // skill id
     * c // shortcut id
     * S // command name
	 * 
	 * format:		cdSSScc (ccdcS)
	 */
	protected void readImpl()
	{
		int _id = readD();
        String _name = readS();
		String _desc = readS();
		String _acronym = readS();
		int _icon = readC();
		int _count = readC();
		L2MacroCmd[] commands = new L2MacroCmd[_count];  
        if (Config.DEBUG) System.out.println("Make macro id:"+_id+"\tname:"+_name+"\tdesc:"+_desc+"\tacronym:"+_acronym+"\ticon:"+_icon+"\tcount:"+_count);
        for (int i = 0; i < _count; i++)
        {
            int entry      = readC();
            int type       = readC(); // 1 = skill, 3 = action, 4 = shortcut
            int d1         = readD(); // skill or page number for shortcuts
            int d2         = readC();
            String command = readS();
            _commands_lenght += command.length();
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
            if (Config.DEBUG) System.out.println("entry:"+entry+"\ttype:"+type+"\td1:"+d1+"\td2:"+d2+"\tcommand:"+command);
        }
		_macro = new L2Macro(_id, _icon, _name, _desc, _acronym, commands);
	}

	protected void runImpl()
	{
		L2PcInstance  player = getClient().getActiveChar(); 
		if (player == null)
		    return;
		if (_commands_lenght > 255)
		{
			//Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(new SystemMessage(810));
			return;
		}
		if (player.getMacroses().getAllMacroses().length > 24)
		{
			//You may create up to 24 macros.
			player.sendPacket(new SystemMessage(797));
			return;
		}
		if (_macro.name.length() == 0)
		{
			//Enter the name of the macro.
			player.sendPacket(new SystemMessage(838));
			return;
		}
		if (_macro.descr.length() > 32)
		{
			//Macro descriptions may contain up to 32 characters.
			player.sendPacket(new SystemMessage(837));
			return;
		}
		player.registerMacro(_macro);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__C1_REQUESTMAKEMACRO;
	}
}