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
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestMagicSkillUse extends ClientBasePacket
{
	private static final String _C__2F_REQUESTMAGICSKILLUSE = "[C] 2F RequestMagicSkillUse";
	private static Logger _log = Logger.getLogger(RequestMagicSkillUse.class.getName());

	private final int _magicId;
	private final boolean _ctrlPressed;
	private final boolean _shiftPressed;
	
	/**
	 * packet type id 0x2f
	 * format:		cddc
	 * @param rawPacket
	 */
	public RequestMagicSkillUse(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
        
		_magicId      = readD();              // Identifier of the used skill
		_ctrlPressed  = readD() != 0;         // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = readC() != 0;         // True if Shift pressed
        
	}

	void runImpl()
	{
        // Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null)
            return;

        // If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
        if (_magicId == 1050 && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0)
            return;

        // Get the level of the used skill
        int level = activeChar.getSkillLevel(_magicId);
        
        // Get the L2Skill template corresponding to the skillID received from the client
		L2Skill skill = SkillTable.getInstance().getInfo(_magicId, level);
        
        
        // Check the validity of the skill
		if (skill != null)
		{
			// _log.fine("	skill:"+skill.getName() + " level:"+skill.getLevel() + " passive:"+skill.isPassive());
			// _log.fine("	range:"+skill.getCastRange()+" targettype:"+skill.getTargetType()+" optype:"+skill.getOperateType()+" power:"+skill.getPower());
			// _log.fine("	reusedelay:"+skill.getReuseDelay()+" hittime:"+skill.getHitTime());
			// _log.fine("	currentState:"+activeChar.getCurrentState());	//for debug
			
            // activeChar.stopMove();
            activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendPacket(new ActionFailed());
			_log.warning("No skill found!!");
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__2F_REQUESTMAGICSKILLUSE;
	}
}
